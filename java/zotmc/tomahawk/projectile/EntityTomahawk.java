package zotmc.tomahawk.projectile;

import static net.minecraft.enchantment.Enchantment.fireAspect;
import static net.minecraft.enchantment.Enchantment.knockback;
import static net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel;
import static net.minecraft.entity.SharedMonsterAttributes.attackDamage;
import static net.minecraft.util.MathHelper.cos;
import static net.minecraft.util.MathHelper.sin;
import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static zotmc.tomahawk.LogTomahawk.pro4j;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.IN_AIR;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.NO_REBOUNCE;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.ON_GROUND;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.CREATIVE;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.SURVIVAL;
import static zotmc.tomahawk.util.Utils.PI;
import static zotmc.tomahawk.util.Utils.atan;
import static zotmc.tomahawk.util.Utils.sqrt;

import java.lang.ref.WeakReference;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import zotmc.tomahawk.Enchs;
import zotmc.tomahawk.TomahawkRegistry;
import zotmc.tomahawk.util.Utils;

import com.google.common.collect.Range;

public class EntityTomahawk extends AbstractTomahawk {
	
	private static final Random RAND = new Random();
	
	public enum PickUpType {
		SURVIVAL,
		CREATIVE,
		ENCH;
		
		public boolean canBePickedUpBy(PickUpType type) {
			switch (type) {
			case SURVIVAL:
				return this == SURVIVAL;
			case CREATIVE:
				return this != ENCH;
			case ENCH:
				return this != CREATIVE;
			}
			return false;
		}
		
	}
	
	
	public final float aRoll = worldObj.isRemote ? (float) RAND.nextGaussian() * 15/2F : 0;
	public final float bRoll = worldObj.isRemote ? (float) RAND.nextGaussian() * 1/2F : 0;
	
	public PickUpType pickUpType = SURVIVAL;
	
	float damageAttr;
	int knockbackStr;
	
	public WeakReference<PlayerTomahawk> fakePlayer;
	
	public EntityTomahawk(World world) {
		super(world);
	}
	public EntityTomahawk(World world, double x, double y, double z, ItemStack item) {
		super(world, x, y, z, INITIAL_SPEED);
		setItem(item);
		
		if (!world.isRemote) {
			BaseAttributeMap attrs = new ServersideAttributeMap();
			attrs.registerAttribute(attackDamage);
			attrs.applyAttributeModifiers(item.getAttributeModifiers());
			damageAttr = (float) attrs.getAttributeInstance(attackDamage).getAttributeValue();
		}
		
	}
	public EntityTomahawk(World world, EntityLivingBase thrower, ItemStack item) {
		super(world, thrower, INITIAL_SPEED);
		setItem(item);

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
	
	
	
	@Override protected void entityInit() {
		super.entityInit();
		getDataWatcher().addObject(2, (float) 0);
		getDataWatcher().addObject(3, (int) -1);
		getDataWatcher().addObject(4, (byte) 1);
		getDataWatcher().addObjectByDataType(10, 5);
	}
	
	public void setRotation(float value) {
		getDataWatcher().updateObject(2, value);
	}
	public float getRotation() {
		return getDataWatcher().getWatchableObjectFloat(2);
	}
	
	public void setAfterHit(int value) {
		getDataWatcher().updateObject(3, value);
	}
	public int getAfterHit() {
		return getDataWatcher().getWatchableObjectInt(3);
	}
	
	public void setIsForwardSpin(boolean value) {
		getDataWatcher().updateObject(4, value ? (byte) 1 : (byte) 0);
	}
	public boolean getIsForwardSpin() {
		return (getDataWatcher().getWatchableObjectByte(4) & 1) != 0;
	}
	
	public void setItem(ItemStack value) {
		getDataWatcher().updateObject(10, value);
		getDataWatcher().setObjectWatched(10);
	}
	public ItemStack getItem() {
		return getDataWatcher().getWatchableObjectItemStack(10);
	}
	
	
	@Override public void writeEntityToNBT(NBTTagCompound tags) {
		super.writeEntityToNBT(tags);
		
		tags.setFloat("rotation", getRotation());
		tags.setInteger("ticksAfterHit", getAfterHit());
		tags.setBoolean("isForwardSpin", getIsForwardSpin());
		tags.setByte("pickUpType", (byte) pickUpType.ordinal());
		tags.setTag("item", getItem().writeToNBT(new NBTTagCompound()));
		tags.setFloat("damageAttr", damageAttr);
		tags.setShort("knockbackStr", (short) knockbackStr);
		
	}
	
	@Override public void readEntityFromNBT(NBTTagCompound tags) {
		super.readEntityFromNBT(tags);
		
		setRotation(tags.getFloat("rotation"));
		setAfterHit(tags.getInteger("ticksAfterHit"));
		setIsForwardSpin(tags.getBoolean("isForwardSpin"));
		pickUpType = PickUpType.values()[tags.getByte("pickUpType")];
		setItem(ItemStack.loadItemStackFromNBT(tags.getCompoundTag("item")));
		damageAttr = tags.getFloat("damageAttr");
		knockbackStr = tags.getShort("knockbackStr");
		
	}
	
	
	
	@Override public int getLifespan() {
		switch (pickUpType) {
		case SURVIVAL:
			ItemStack item = getItem();
			return item != null ? item.getItem().getEntityLifespan(item, worldObj) : 0;
		case ENCH:
			return 120;
		default:
			return super.getLifespan();
		}
	}
	
	
	
	public boolean readyForPickUp() {
		return getState() != IN_AIR || getAfterHit() >= 8;
	}
	
	@Override public void onCollideWithPlayer(EntityPlayer player) {
		if (!worldObj.isRemote && readyForPickUp()) {
			boolean pickedUp = pickUpType.canBePickedUpBy(SURVIVAL)
					|| player.capabilities.isCreativeMode && pickUpType.canBePickedUpBy(CREATIVE);
			
			ItemStack item = getItem();
			if (pickUpType.canBePickedUpBy(SURVIVAL)
					&& !player.inventory.addItemStackToInventory(item))
				pickedUp = false;
			
			if (pickedUp) {
				playSound("random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1) * 2);
				player.onItemPickup(this, item.stackSize);
				setDead();
				
				pro4j().debug("An EntityTomahawk %s [PickUpType: %s] has been picked up by EntityPlayer %s",
						getItem(), pickUpType, player.getEntityId());
			}
		}
	}
	
	@Override public void onUpdate() {
		/*
		pro4j().debug("EntityTomahawk.%s [%s at (%.0f, %.0f, %.0f)]",
				"onUpdate", getItem(), posX, posY, posZ);
		*/
		
		super.onUpdate();
		
		if (getState() == IN_AIR || getState() == NO_REBOUNCE) {
			setRotation(modAngle(getRotation() + 56F * getSpinFactor(false)));
			
			float p = 7 / getSpinFactor(false);
			float t = ticksInAir.get() % p;
			if (t >= p - 1)
				playInAirSound(motionX * motionX + motionY * motionY + motionZ * motionZ);
			
			
			if (getAfterHit() < 0) {
				if (getIsCritical())
					for (int i = 0; i < 4; i++)
						worldObj.spawnParticle("crit",
								posX + motionX * i / 4.0, 
								posY + motionY * i / 4.0,
								posZ + motionZ * i / 4.0,
								-motionX, -motionY + 0.2D, -motionZ);
				
				if (worldObj.isRemote && getItem().isItemEnchanted())
					for (int i = 0; i < 3; i++)
						if (RAND.nextInt(4) == 0)
							worldObj.spawnParticle("magicCrit",
									posX + motionX * i / 3.0, 
									posY + motionY * i / 3.0,
									posZ + motionZ * i / 3.0,
									-motionX, -motionY + 0.2D, -motionZ);
			}
			else
				setAfterHit(getAfterHit() + 1);
		}
		
	}
	
	@Override protected boolean onLifespanTick(int lifespan) {
		if (super.onLifespanTick(lifespan)) {
			pro4j().debug("An EntityTomahawk %s [State: %s][Pos: %.0f %.0f %.0f] has its lifespan %s expired",
					getItem(), getState(), posX, posY, posZ, lifespan);
			
			return true;
		}
		return false;
	}
	
	@Override protected void onImpact(MovingObjectPosition mop) {
		if (getAfterHit() < 0 && mop.entityHit != null) {
			
			ItemStack item = getItem();
			
			boolean flag = worldObj.isRemote;
			if (!flag && worldObj instanceof WorldServer) {
				PlayerTomahawk fakePlayer = new PlayerTomahawk((WorldServer) worldObj, this);
				
				flag = !MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(fakePlayer, mop.entityHit))
						&& !item.getItem().onLeftClickEntity(item, fakePlayer, mop.entityHit);
				
				this.fakePlayer = new WeakReference<PlayerTomahawk>(fakePlayer);
			}
			
			if (flag && mop.entityHit.canAttackWithItem()
					&& !mop.entityHit.hitByEntity(this)) {
				
				float damage = damageAttr;
				float enchCrit = 0;
				int knock = knockbackStr;
				if (mop.entityHit instanceof EntityLivingBase) {
					enchCrit = Enchs.getEnchantmentModifierLiving(item, (EntityLivingBase) mop.entityHit);
					knock += getEnchantmentLevel(knockback.effectId, item);
				}
				
				if (damage > 0 || enchCrit > 0) {
					boolean critical = getIsCritical() && mop.entityHit instanceof EntityLivingBase;
					if (critical && damage > 0)
						damage *= 1.5F;
					
					damage += enchCrit;
				
					boolean setFire = false;
					int fire = getEnchantmentLevel(fireAspect.effectId, item);
					if (fire > 0 && mop.entityHit instanceof EntityLivingBase && !mop.entityHit.isBurning()) {
						setFire = true;
						mop.entityHit.setFire(1);
					}
					
					
					Entity thrower = getThrower();
					
					boolean attacked = mop.entityHit.attackEntityFrom(
							new TomahawkDamage(this, thrower != null ? thrower : this),
							damage);
					
					if (attacked) {
						if (knock > 0) {
							float hv = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
							mop.entityHit.addVelocity(
									(float) motionX / hv * knock * 0.5F,
									0.1,
									(float) motionZ / hv * knock * 0.5F);
							motionX *= 0.6;
							motionZ *= 0.6;
						}
						
						if (critical)
							Utils.onCritical(this, mop.entityHit);
						
						if (enchCrit > 0)
							Utils.onEnchantmentCritical(this, mop.entityHit);
						
						/*
						if (damage >= 18 && thrower instanceof EntityPlayer)
							((EntityPlayer) thrower).triggerAchievement(AchievementList.overkill);
						*/
						//setLastAttacker?
						//hurt player i.e. thorns
						
						if (thrower instanceof EntityLivingBase)
							try {
								Enchs.applyEnchantmentDamageIterator(
										(EntityLivingBase) thrower, item, mop.entityHit);
							} catch (Throwable ignored) { }
						
						Entity entity = mop.entityHit;
						if (mop.entityHit instanceof EntityDragonPart) {
							IEntityMultiPart dragon = ((EntityDragonPart) mop.entityHit).entityDragonObj;
							if (dragon != null && dragon instanceof EntityLivingBase)
								entity = (Entity) dragon;
						}
						
						if (item != null && entity instanceof EntityLivingBase) {
							if (thrower instanceof EntityPlayer)
								item.hitEntity((EntityLivingBase) entity, (EntityPlayer) thrower);
							else
								item.attemptDamageItem(2, rand);
							
							if (item.stackSize <= 0) {
								setDead();
								
								playSound("random.break", 0.8F, 0.8F + worldObj.rand.nextFloat() * 0.4F);
								
								pro4j().debug("An EntityTomahawk %s has been destroyed while hitting %s %s",
										getItem(), entity.getClass().getSimpleName(), entity.getEntityId());
							}
							
						}
						
						if (mop.entityHit instanceof EntityLivingBase) {
							//addStat
							
							if (fire > 0)
								mop.entityHit.setFire(fire * 4);
						}
						
					}
					else if (setFire)
						mop.entityHit.extinguish();
					
				}
			}
			
			rebounce(mop, getReactFactor(), true);
			
			setAfterHit(0);
		}
		else {
			boolean setInGround = getState() != ON_GROUND;
			
			if (setInGround) {
				float angle = getRotation() + 45;
				if (mop.sideHit == DOWN.ordinal())
					angle += 90 * (getIsForwardSpin() ? 1 : -1);
				else if (mop.sideHit == UP.ordinal())
					angle -= 90 * (getIsForwardSpin() ? 1 : -1);
				
				setInGround = ANGLE_RANGE.contains(modAngle(angle));
			}
			
			if (setInGround) {
				super.onImpact(mop);
				setIsCritical(false);
			}
			else {
				ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
				mop.hitInfo = worldObj.getWorldVec3Pool().getVecFromPool(
						dir.offsetX, dir.offsetY, dir.offsetZ);
				
				rebounce(mop, getReactFactorOnBlock(), false);
				
				double v2 = motionX * motionX + motionY * motionY + motionZ * motionZ;
				if (v2 < 1/9D) {
					setState(NO_REBOUNCE);
					setIsCritical(false);
				}
				
				Block block = worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
				playHitSound(false, block,
						block.getBlockHardness(worldObj, mop.blockX, mop.blockY, mop.blockZ));
			}
		}
	}

	private static final Range<Float> ANGLE_RANGE = Range.closed(
			atan(4/9D) * 180 / PI - 45, 180 - atan(2) * 180 / PI);
	
	@Override protected void onMotionTick(float vH, float v, float resistance) {
		super.onMotionTick(vH, v, resistance);

		if (getState() != ON_GROUND) {
			float k = getSpinMotionFactor() * getSpinFactor(true);
			double wH = -k * motionY;
			
			motionX += sin(rotationYaw * (PI / 180)) * wH;
			motionY += k * vH;
			motionZ += -cos(rotationYaw * (PI / 180)) * wH;
				
		}
		
	}
	
	@Override protected void rebounce(MovingObjectPosition mop, double reactFactor, boolean onEntity) {
		float p = rotationYaw;
		super.rebounce(mop, reactFactor, onEntity);
		if (modAngle(rotationYaw - p - 90) >= 0)
			setIsForwardSpin(!getIsForwardSpin());
	}
	
	@Override protected void onRebounce(MovingObjectPosition mop, double nY, double n, double react) {
		double w = react * getSpinReactFactor() * getSpinFactor(true);
		double wH = w * nY / n;
		double wY = w * sqrt(n * n - nY * nY) / n;
		
		double wX = sin(rotationYaw * (PI / 180)) * wH;
		double wZ = cos(rotationYaw * (PI / 180)) * wH;
		motionX += wX;
		motionY += wY;
		motionZ += wZ;
		
		if (mop.entityHit != null) {
			mop.entityHit.motionX -= 0.02 * wX;
			mop.entityHit.motionY -= 0.02 * wY;
			mop.entityHit.motionZ -= 0.02 * wZ;
		}
		
		setRotation(modAngle(getRotation() + 180));
	}
	
	
	@Override protected void playHitSound(boolean isStationary, Block block, float hardness) {
		if (!worldObj.isRemote) {
			SoundType sound = TomahawkRegistry.getHitSound(getItem().getItem());
			
			if (sound != null)
				playSound(sound.soundName, sound.getVolume(), sound.getPitch());
			else if (hardness < 1)
				super.playHitSound(isStationary, block, hardness);
			else {
				sound = block.stepSound;
				float vol = sound.getVolume() * 28 / hardness;
				float pit = sound.getPitch() * (1.2F / rand.nextFloat() * 0.2F + 0.9F) * 2/5F;
				
				if (!isStationary)
					vol /= 6;
				
				int n = (int) vol;
				for (int i = 0; i < n; i++)
					playSound(sound.getBreakSound(), 1, pit);
				
				playSound(sound.getBreakSound(), vol - n, pit);
			}
		}
	}
	
	
	
	protected float getSpinFactor(boolean isSignApplicable) {
		return (getAfterHit() >= 0 ? 55/72F : (getIsCritical() ? 7/6F : 1))
				* (!isSignApplicable || getIsForwardSpin() ? 1 : -1);
	}
	
	protected float getSpinMotionFactor() {
		return 0.03F;
	}
	
	protected double getReactFactor() {
		return 0.31;
	}
	
	protected double getReactFactorOnBlock() {
		return 0.24;
	}
	
	protected double getSpinReactFactor() {
		return 3.45;
	}
	
	protected static final float INITIAL_SPEED = 2.11F;

}
