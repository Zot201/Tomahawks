package zotmc.tomahawk.projectile;

import static java.util.concurrent.ThreadLocalRandom.current;
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

import zotmc.tomahawk.api.PickUpType;
import zotmc.tomahawk.api.Pointable;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.api.WeaponDispenseEvent;
import zotmc.tomahawk.api.WeaponLaunchEvent;
import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.core.LogTomahawk;
import zotmc.tomahawk.core.PositionTracker;
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
import zotmc.tomahawk.util.prop.IntProp;
import zotmc.tomahawk.util.prop.Prop;
import zotmc.tomahawk.util.prop.Props;

public class EntityTomahawk extends EntityArrow implements Pointable {
	
	public enum State {
		IN_AIR,
		IN_GROUND,
		NO_REBOUNCE,
		ON_GROUND,
		ON_RELEASE;
		
		public boolean isStationary() {
			return (ordinal() & 1) == 1;
		}
	}
	
	
	
	public final float aRoll = worldObj.isRemote ? (float) current().nextGaussian() * 15/2F : 0;
	public final float bRoll = worldObj.isRemote ? (float) current().nextGaussian() * 1/2F : 0;
	
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
		
		initItem(item);
		
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
		
		if (!world.isRemote) {
			if (thrower instanceof EntityPlayer)
				PositionTracker.get((EntityPlayer) thrower).getCurrentMotion().addTo(entityMotion);
			else
				EntityGeometry.getMotion(thrower).addTo(entityMotion);
			
			setThrowableHeading(motionX, motionY, motionZ, initalSpeed, 1);
		}
		
		entityRotationYaw.setRadians(entityMotion.yaw());
		
		initItem(item);
		
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
	
	protected void initItem(ItemStack item) {
		this.item.set(item);
		if (Config.current().igniteFireRespect.get() && Utils.getEnchLevel(Enchantment.fireAspect, item) > 0)
			setFire(100);
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
		getDataWatcher().addObjectByDataType(10, 5);
	}
	
	public final Angle rotation = Props.ofAngle(Unit.DEGREE, this, 2);
	
	public final IntProp afterHit = Props.ofInt(this, 3);
	
	public final BooleanProp isForwardSpin = Props.ofBoolean(this, 4, 0);
	public final BooleanProp isRolled = Props.ofBoolean(this, 4, 1);
	public final BooleanProp isFixed = Props.ofBoolean(this, 4, 2);
	public final BooleanProp isFragile = Props.ofBoolean(this, 4, 3);
	public final BooleanProp isBreaking = Props.ofBoolean(this, 4, 4);
	
	private ByteProp stateByte = Props.ofByte(this, 5);
	public Prop<State> state = Props.ofEnum(State.class, stateByte);
	
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
	}
	
	
	
	
	@Override public void onUpdate() {
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;
		
		super.onEntityUpdate();
		
		if (!worldObj.isRemote) {
			phy4j().debug("====On Update====");
			phy4j().debug("Entity ID:    %d", getEntityId());
			phy4j().debug("Tick Existed: %d", ticksExisted);
			phy4j().debug("State:        %s", state.get());
			phy4j().debug("In Tile Pos:   %s", inTilePos);
			phy4j().debug("Pos:          %.1s", pos);
			phy4j().debug("Motion:       %.3s", entityMotion);
			phy4j().debug("Rotation Yaw: %#.1s", entityRotationYaw);
			phy4j().debug("Rotation:     %#.1s", rotation);
		}
		
		ticker.run();
		
		if (!worldObj.isRemote)
			phy4j().debug("");
		
		setPosition(posX, posY, posZ);
	}
	
	@Override public boolean canAttackWithItem() {
		return true;
	}
	
	@Override public boolean hitByEntity(Entity entity) {
		return entity instanceof EntityPlayer && onLeftClick((EntityPlayer) entity);
	}
	
	@Override public void onCollideWithPlayer(EntityPlayer player) {
		if (!worldObj.isRemote && !isFixed.get() && readyForPickUp()) {
			boolean creative = player.capabilities.isCreativeMode;
			
			if (pickUpType.canBePickedUpBy(creative ? CREATIVE : SURVIVAL)) {
				ItemStack item = this.item.get();
				
				if (pickUpType.canBePickedUpBy(SURVIVAL)) {
					if (player.getCurrentEquippedItem() == null)
						player.setCurrentItemOrArmor(0, item);
					else if (!player.inventory.addItemStackToInventory(item))
						return;
				}
				
				playSound("random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1) * 2);
				player.onItemPickup(this, item.stackSize);
				setDead();
			}
		}
	}
	
	public boolean onLeftClick(EntityPlayer player) {
		if (!worldObj.isRemote && (!isFixed.get() || player.isSneaking()) && state.get().isStationary())
			attemptPickup(player);
		
		return true; // return true to indicate no further process
	}
	
	public boolean attemptPickup(EntityPlayer player) {
		boolean creative = player.capabilities.isCreativeMode;
		
		if (pickUpType.canBePickedUpBy(creative ? CREATIVE : SURVIVAL)) {
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
			
			return true;
		}
		
		return false;
	}
	
	@Override public boolean interactFirst(EntityPlayer player) {
		if (state.get().isStationary() && player.isSneaking()
				&& pickUpType.canBePickedUpBy(player.capabilities.isCreativeMode ? CREATIVE : SURVIVAL)) {
			Props.toggle(isFixed);
			player.swingItem();
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
		if (!worldObj.isRemote)
			playSound("random.bow",
					Utils.closed(0, 1, (float) v2 * 16),
					1 / (rand.nextFloat() * 0.4F + 1.2F) + 0.5F
			);
	}
	
	protected void playHitSound(boolean isStationary, Block block, float hardness) {
		if (!worldObj.isRemote) {
			ItemStack item = this.item.get();
			SoundType hitSound = TomahawkRegistry.getItemHandler(item).getHitSound(item);
			
			if (hitSound != null)
				playSound(hitSound.soundName, hitSound.getVolume(), hitSound.getPitch());
			else if (hardness < 1) {
				if (!worldObj.isRemote)
					playSound("random.bowhit",
							1.4F, (1.2F / rand.nextFloat() * 0.2F + 0.9F) * 3/5F
					);
			}
			else {
				hitSound = block.stepSound;
				float vol = hitSound.getVolume() * 28 / hardness;
				float pit = hitSound.getPitch() * (1.2F / rand.nextFloat() * 0.2F + 0.9F) * 2/5F;
				
				if (!isStationary)
					vol /= 6;
				
				int n = (int) vol;
				for (int i = 0; i < n; i++)
					playSound(hitSound.getBreakSound(), 1, pit);
				
				playSound(hitSound.getBreakSound(), vol - n, pit);
				
			}
		}
	}
	
	
	
	public boolean isPersistenceRequired() {
		return isFixed.get() || (pickUpType != CREATIVE && item.get().hasDisplayName());
	}
	
	public int getLifespan() {
		switch (pickUpType) {
		case SURVIVAL:
			ItemStack item = this.item.get();
			return item == null ? 120 : item.getItem().getEntityLifespan(item, worldObj);
		case ENCH:
			return 120;
		default:
			return 1200;
		}
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
		return 0.27;
	}
	
	protected double getBlockRestitutionFactor() {
		return 0.24;
	}
	
}
