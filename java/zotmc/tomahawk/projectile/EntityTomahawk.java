package zotmc.tomahawk.projectile;

import static net.minecraft.entity.SharedMonsterAttributes.attackDamage;
import static zotmc.tomahawk.api.PickUpType.CREATIVE;
import static zotmc.tomahawk.api.PickUpType.SURVIVAL;
import static zotmc.tomahawk.data.ReflData.EntityArrows.TICKS_IN_AIR;
import static zotmc.tomahawk.data.ReflData.EntityArrows.TICKS_IN_GROUND;
import static zotmc.tomahawk.data.ReflData.EntityArrows.X_TILE;
import static zotmc.tomahawk.data.ReflData.EntityArrows.Y_TILE;
import static zotmc.tomahawk.data.ReflData.EntityArrows.Z_TILE;
import static zotmc.tomahawk.projectile.EntityTomahawk.State.IN_AIR;

import java.lang.ref.WeakReference;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.dispenser.IPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.apache.logging.log4j.Logger;

import zotmc.tomahawk.api.ItemHandler.PlaybackType;
import zotmc.tomahawk.api.PickUpType;
import zotmc.tomahawk.api.Pointable;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.api.WeaponDispenseEvent;
import zotmc.tomahawk.api.WeaponLaunchEvent;
import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.core.LogTomahawk;
import zotmc.tomahawk.core.PlayerTracker;
import zotmc.tomahawk.core.TomahawkImpls;
import zotmc.tomahawk.util.Fields;
import zotmc.tomahawk.util.IdentityBlockMeta;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.geometry.AbsCylindricalVec3d.DelegationHandler;
import zotmc.tomahawk.util.geometry.Angle;
import zotmc.tomahawk.util.geometry.Angle.Unit;
import zotmc.tomahawk.util.geometry.EntityGeometry;
import zotmc.tomahawk.util.geometry.HybridVec3d;
import zotmc.tomahawk.util.geometry.Vec3d;
import zotmc.tomahawk.util.geometry.Vec3i;
import zotmc.tomahawk.util.prop.BooleanProp;
import zotmc.tomahawk.util.prop.ByteProp;
import zotmc.tomahawk.util.prop.FloatProp;
import zotmc.tomahawk.util.prop.IntProp;
import zotmc.tomahawk.util.prop.Prop;
import zotmc.tomahawk.util.prop.Props;

public class EntityTomahawk extends EntityArrow implements Pointable {
	
	public enum State {
		IN_AIR,
		IN_GROUND,
		NO_REBOUNCE,
		ON_GROUND;
		
		public boolean isStationary() {
			return (ordinal() & 1) == 1;
		}
	}
	
	
	public final Vec3d entityMotion = EntityGeometry.getMotion(this);
	public final Angle entityRotationYaw = EntityGeometry.getRotationYaw(this);
	public final Vec3d pos = EntityGeometry.getPos(this);
	public IdentityBlockMeta inTile = IdentityBlockMeta.AIR;
	public final Vec3i inTilePos = Fields.asVec3i(this, X_TILE, Y_TILE, Z_TILE);
	protected final Prop<Integer> ticksInGround = Fields.referTo(this, TICKS_IN_GROUND).ofType(int.class);
	protected final Prop<Integer> ticksInAir = Fields.referTo(this, TICKS_IN_AIR).ofType(int.class);
	
	public PickUpType pickUpType = PickUpType.SURVIVAL;
	public float damageAttr;
	public int knockbackStr;
	public int sideHit = -1;
	public final Runnable ticker = createTicker();
	private WeakReference<FakePlayerTomahawk> fakePlayer = new WeakReference<>(null);
	
	public final HybridVec3d projectileMotion = new HybridVec3d() {
		@Override protected boolean normalize() {
			if (super.normalize()) {
				Props.toggle(isForwardSpin);
				return true;
			}
			return false;
		}
	};
	public final Vec3d spin = projectileMotion.derive(new DelegationHandler() {
		@Override public double getY(double y) {
			return 0;
		}
		@Override public double getRho(double rho) {
			return getSpinStrength();
		}
		@Override public int getPhi(int phi) {
			return phi + (Integer.MIN_VALUE / -2) * Props.toSignum(isForwardSpin);
		}
	});
	
	
	
	public EntityTomahawk(World world) {
		super(world);
	}
	
	public EntityTomahawk(World world, IPosition pos, ItemStack item) {
		super(world, pos.getX(), pos.getY(), pos.getZ());
		setSize(0.6F, 0.6F);
		
		projectileInit(item);
		
		if (!world.isRemote) {
			BaseAttributeMap attrs = new ServersideAttributeMap();
			attrs.registerAttribute(attackDamage);
			attrs.applyAttributeModifiers(item.getAttributeModifiers());
			damageAttr = (float) attrs.getAttributeInstance(attackDamage).getAttributeValue();
		}
	}
	
	public EntityTomahawk(World world, EntityLivingBase thrower, float initalSpeed, ItemStack item) {
		super(world, thrower, initalSpeed / 1.5F);
		setSize(0.6F, 0.6F);
        //setPosition(thrower.posX, thrower.posY + thrower.getEyeHeight(), thrower.posZ);
		
		{
			GeometryHelper.motion(thrower).addTo(entityMotion);
			setThrowableHeading(motionX, motionY, motionZ, initalSpeed, 1);
		}
		
		entityRotationYaw.setRadians(entityMotion.yaw());
		
		projectileInit(item);
		
		if (!world.isRemote) {
			damageAttr = (float) thrower.getEntityAttribute(attackDamage).getAttributeValue();
			if (thrower.isSprinting())
				knockbackStr = 1;
			if (thrower.fallDistance > 0.0F && !thrower.onGround && !thrower.isOnLadder()
					&& !thrower.isInWater() && !thrower.isPotionActive(Potion.blindness)
					&& thrower.ridingEntity == null)
				setIsCritical(true);
		}
	}
	
	public EntityTomahawk(WeaponLaunchEvent event) {
		this(event.entity.worldObj, event.entityLiving, event.initialSpeed, event.item);
		
		isForwardSpin.set(event.isForwardSpin);
		isRolled.set(!event.isForwardSpin);
		pickUpType = event.getPickUpType();
		isFragile.set(event.isFragile);
	}
	
	public EntityTomahawk(WeaponDispenseEvent event) {
		this(event.world, event.getPosition(), event.item);
		
		isForwardSpin.set(event.isForwardSpin);
		isRolled.set(!event.isForwardSpin);
		pickUpType = event.getPickUpType();
		isFragile.set(event.isFragile);
		
		EnumFacing facing = event.getFacing();
		setThrowableHeading(
				facing.getFrontOffsetX(),
				facing.getFrontOffsetY() + 0.1F,
				facing.getFrontOffsetZ(),
				event.initialSpeed, event.deviation
		);
	}
	
	protected void projectileInit(ItemStack item) {
		this.item.set(item);
		if (Config.current().igniteFireRespect.get() && Utils.getEnchLevel(Enchantment.fireAspect, item) > 0)
			setFire(100);
		
		aRoll.set((float) rand.nextGaussian() * 15/2F);
		bRoll.set((float) rand.nextGaussian()  * 1/2F);
	}
	
	protected Runnable createTicker() {
		return new TickerTomahawk(this);
	}
	
	
	
	@Override protected void entityInit() {
		super.entityInit();
		getDataWatcher().addObject(2, (float) 0);
		getDataWatcher().addObject(3, (int) -1);
		getDataWatcher().addObject(4, (byte) 0b1);
		getDataWatcher().addObject(5, (byte) 0);
		getDataWatcher().addObject(6, (float) 0);
		getDataWatcher().addObject(7, (float) 0);
		getDataWatcher().addObjectByDataType(10, 5);
	}
	
	public final Angle rotation = Props.ofAngle(Unit.DEGREE, this, 2);
	
	public final IntProp afterHit = Props.ofInt(this, 3);
	
	public final BooleanProp isForwardSpin = Props.ofBoolean(this, 4, 0);
	public final BooleanProp isRolled = Props.ofBoolean(this, 4, 1);
	public final BooleanProp isFixed = Props.ofBoolean(this, 4, 2);
	public final BooleanProp isFragile = Props.ofBoolean(this, 4, 3);
	public final BooleanProp isBreaking = Props.ofBoolean(this, 4, 4);
	public final BooleanProp isInclined = Props.ofBoolean(this, 4, 5);
	
	private ByteProp stateByte = Props.ofByte(this, 5);
	public Prop<State> state = Props.ofEnum(State.class, stateByte);
	
	public final FloatProp aRoll = Props.ofFloat(this, 6);
	public final FloatProp bRoll = Props.ofFloat(this, 7);
	
	public final Prop<ItemStack> item = Props.ofItemStack(this, 10);
	
	
	@Override public void writeEntityToNBT(NBTTagCompound tags) {
		super.writeEntityToNBT(tags);
		tags.setByte("state", stateByte.get());
		tags.setFloat("rotation", rotation.toDegrees());
		tags.setInteger("ticksAfterHit", afterHit.get());
		tags.setBoolean("isForwardSpin", isForwardSpin.get());
		tags.setByte("pickUpType", (byte) pickUpType.ordinal());
		tags.setTag("item", item.get().writeToNBT(new NBTTagCompound()));
		tags.setFloat("damageAttr", damageAttr);
		tags.setShort("knockbackStr", (short) knockbackStr);
		tags.setTag("projectileMotion", projectileMotion.writeToNBT());
		tags.setBoolean("isRolled", isRolled.get());
		tags.setBoolean("isFixed", isFixed.get());
		tags.setByte("sideHit", (byte) sideHit);
		tags.setTag("inTileMeta", inTile.toNBT());
		tags.setBoolean("isInclined", isInclined.get());
		tags.setFloat("aRoll", aRoll.get());
		tags.setFloat("bRoll", bRoll.get());
	}
	
	@Override public void readEntityFromNBT(NBTTagCompound tags) {
		super.readEntityFromNBT(tags);
		stateByte.set(tags.getByte("state"));
		rotation.setDegrees(tags.getFloat("rotation"));
		afterHit.set(tags.getInteger("ticksAfterHit"));
		isForwardSpin.set(tags.getBoolean("isForwardSpin"));
		pickUpType = PickUpType.values()[tags.getByte("pickUpType")];
		item.set(ItemStack.loadItemStackFromNBT(tags.getCompoundTag("item")));
		damageAttr = tags.getFloat("damageAttr");
		knockbackStr = tags.getShort("knockbackStr");
		projectileMotion.readFromNBT(tags.getCompoundTag("projectileMotion"));
		isRolled.set(tags.getBoolean("isRolled"));
		isFixed.set(tags.getBoolean("isFixed"));
		sideHit = tags.getByte("sideHit");
		inTile = IdentityBlockMeta.readFromNBT(tags.getCompoundTag("inTileMeta"));
		isInclined.set(tags.getBoolean("isInclined"));
		aRoll.set(tags.getFloat("aRoll"));
		bRoll.set(tags.getFloat("bRoll"));
	}
	
	
	
	
	@Override public void onUpdate() {
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;
		
		super.onEntityUpdate();
		
		/*if (!worldObj.isRemote) {
			phy4j().debug("====On Update====");
			phy4j().debug("Entity ID:    %d", getEntityId());
			phy4j().debug("Tick Existed: %d", ticksExisted);
			phy4j().debug("State:        %s", state.get());
			phy4j().debug("In Tile Pos:   %s", inTilePos);
			phy4j().debug("Pos:          %.1s", pos);
			phy4j().debug("Motion:       %.3s", entityMotion);
			phy4j().debug("Rotation Yaw: %#.1s", entityRotationYaw);
			phy4j().debug("Rotation:     %#.1s", rotation);
		}*/
		
		ticker.run();
		
		/*if (!worldObj.isRemote)
			phy4j().debug("");*/
		
		setPosition(posX, posY, posZ);
	}
	
	@Override public boolean canAttackWithItem() {
		return true;
	}
	
	@Override public boolean hitByEntity(Entity entity) {
		return entity instanceof EntityPlayer && onLeftClick((EntityPlayer) entity);
	}
	
	@Override public void onCollideWithPlayer(EntityPlayer player) {
		if (!worldObj.isRemote && !isFixed.get() && readyForPickUp())
			attemptPickingUp(player, player == shootingEntity);
	}
	
	public boolean onLeftClick(EntityPlayer player) {
		if (!worldObj.isRemote && (!isFixed.get() || player.isSneaking()) && state.get().isStationary())
			attemptPickingUp(player, true);
		
		return true; // return true to indicate no further process
	}
	
	public boolean attemptPickingUp(EntityPlayer player, boolean flag) {
		boolean creative = player.capabilities.isCreativeMode;
		
		if (pickUpType.canBePickedUpBy(creative ? CREATIVE : SURVIVAL)) {
			PlayerTracker tracker = null;
			
			flag = flag || Config.current().freeRetrieval.get();
			if (!flag) {
				int t = (tracker = PlayerTracker.get(player)).getAfterInteract();
				flag = t >= 0 && t < 1200;
			}
			
			if (flag) {
				ItemStack item = this.item.get();
				
				if (pickUpType.canBePickedUpBy(SURVIVAL)) {
					if (player.getCurrentEquippedItem() == null)
						player.setCurrentItemOrArmor(0, item);
					else if (!player.inventory.addItemStackToInventory(item))
						return false;
				}
				
				playSound("random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1) * 2);
				player.onItemPickup(this, item.stackSize);
				setDead();
				
				(tracker != null ? tracker : PlayerTracker.get(player)).onInteract();
				return true;
			}
		}
		
		return false;
	}
	
	@Override public boolean interactFirst(EntityPlayer player) {
		if (state.get().isStationary() && player.isSneaking()
				&& pickUpType.canBePickedUpBy(player.capabilities.isCreativeMode ? CREATIVE : SURVIVAL)) {
			Props.toggle(isFixed);
			player.swingItem();
			PlayerTracker.get(player).onInteract();
			return true;
		}
		return false;
	}
	
	
	
	
	public FakePlayerTomahawk getFakePlayer(WorldServer world) {
		FakePlayerTomahawk fakePlayer = this.fakePlayer.get();
		return fakePlayer != null ? fakePlayer : createFakePlayer(world);
	}
	
	public FakePlayerTomahawk createFakePlayer(WorldServer world) {
		FakePlayerTomahawk fakePlayer = new FakePlayerTomahawk(world, this);
		this.fakePlayer = new WeakReference<>(fakePlayer);
		return fakePlayer;
	}
	
	Random rand() {
		return rand;
	}
	
	private static final Logger phy4j() {
		return LogTomahawk.phy4j();
	}
	
	@Override public void func_145775_I() {
		super.func_145775_I();
	}
	
	protected void playInAirSound(double v2) {
		ItemStack item = this.item.get();
		SoundType hitSound = TomahawkRegistry.getItemHandler(item).getSound(item, PlaybackType.IN_AIR);
		
		if (hitSound != null)
			playSound(hitSound.soundName, Utils.closed(0, 1, (float) v2 * 16), hitSound.getPitch());
	}
	
	public void playEntityHitSound() {
		ItemStack item = this.item.get();
		SoundType hitSound = TomahawkRegistry.getItemHandler(item).getSound(item, PlaybackType.HIT_ENTITY);
		
		if (hitSound != null)
			playSound(hitSound.soundName, hitSound.getVolume(), hitSound.getPitch());
	}
	
	public void playBlockHitSound(boolean isStationary, Block block, Vec3i position) {
		float hardness = position.getBlockHardness(worldObj, block);
		ItemStack item = this.item.get();
		SoundType hitSound = TomahawkRegistry.getItemHandler(item)
				.getSound(item, hardness < 1 ? PlaybackType.HIT_BLOCK_WEAK : PlaybackType.HIT_BLOCK);

		if (hitSound != null)
			playSound(hitSound.soundName, hitSound.getVolume(), hitSound.getPitch());
		else if (hardness >= 1) {
			
			SoundType s = block.stepSound;
			float vol = s.getVolume() * 28 / hardness;
			float pit = s.getPitch() * (1.2F / rand.nextFloat() * 0.2F + 0.9F) * 2/5F;
			
			if (!isStationary)
				vol /= 6;
			
			int n = (int) vol;
			for (int i = 0; i < n; i++)
				playSound(s.getBreakSound(), 1, pit);
			
			playSound(s.getBreakSound(), vol - n, pit);
		}
	}
	
	
	
	public boolean isPersistenceRequired() {
		return isFixed.get() || (pickUpType != CREATIVE && item.get().hasDisplayName());
	}
	
	public int getLifespan() {
		if (pickUpType == SURVIVAL) {
			ItemStack item = this.item.get();
			if (item != null)
				return item.getItem().getEntityLifespan(item, worldObj);
		}
		
		return 1200;
	}
	
	public boolean readyForPickUp() {
		return state.get() != IN_AIR || afterHit.get() >= 5;
	}
	
	public void onRelease(Vec3d motion) {
		isFixed.set(false);
		ticksInGround.set(0);
		ticksInAir.set(0);
		afterHit.set(-1);
		motion.multiplyValues(rand, 0.2F);
	}
	
	public void onBroken() {
		TomahawkImpls.renderBrokenItemStack(this, item.get(), rand);
		setDead();
	}
	
	public void startBreaking() {
		isBreaking.set(true);
	}
	
	
	
	protected float getDragFactor() {
		return 0.06F;
	}
	
	protected float getGravity() {
		return 0.12F;
	}
	
	protected float getSpinStrength() {
		return afterHit.get() >= 0 ? 55/72F : getIsCritical() ? 7/6F : 1;
	}
	
	protected float getSpinMagnusFactor() {
		return 0.02F;
	}
	
	protected double getSpinFrictionFactor() {
		return 2.51;
	}
	
	protected double getEntityRestitutionFactor() {
		return 0.27 * (Config.current().reduceEntityRestitution.get() ? 1/6D : 1);
	}
	
	protected double getBlockRestitutionFactor() {
		return 0.24;
	}
	
}
