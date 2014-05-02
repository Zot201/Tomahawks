package zotmc.tomahawk.projectile;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static net.minecraft.enchantment.Enchantment.fireAspect;
import static net.minecraft.enchantment.Enchantment.knockback;
import static net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel;
import static net.minecraft.entity.SharedMonsterAttributes.attackDamage;
import static net.minecraft.util.DamageSource.causeThrownDamage;
import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static zotmc.tomahawk.LogTomahawk.pro4j;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.IN_AIR;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.NO_REBOUNCE;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.ON_GROUND;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.CREATIVE;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.SURVIVAL;

import java.util.Random;

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
import net.minecraftforge.common.util.ForgeDirection;
import zotmc.tomahawk.Enchs;
import zotmc.tomahawk.Tomahawk;

import com.google.common.collect.Range;

public class EntityTomahawk extends AbstractTomahawk {
	
	private static final Random RAND = new Random();
	
	public enum PickUpType {
		SURVIVAL,
		CREATIVE,
		MOB;
		
		public boolean canBePickedUpBy(PickUpType type) {
			switch (type) {
			case SURVIVAL:
				return this == SURVIVAL;
			case CREATIVE:
				return this != MOB;
			case MOB:
				return this != CREATIVE;
			}
			return false;
		}
		
	}
	
	
	public final float aRoll = worldObj.isRemote ? (float) RAND.nextGaussian() * 15/2F : 0;
	public final float bRoll = worldObj.isRemote ? (float) RAND.nextGaussian() * 1/2F : 0;
	
	public PickUpType pickUpType = SURVIVAL;
	
	private float damageAttr;
	private int knockbackStr;
	
	public EntityTomahawk(World world) {
		super(world);
	}
	public EntityTomahawk(World world, double x, double y, double z, ItemStack item) {
		super(world, x, y, z);
		setItem(item);
		
		BaseAttributeMap attrs = new ServersideAttributeMap();
		attrs.registerAttribute(attackDamage);
		attrs.applyAttributeModifiers(item.getAttributeModifiers());
		damageAttr = (float) attrs.getAttributeInstance(attackDamage).getAttributeValue();
		
	}
	public EntityTomahawk(World world, EntityLivingBase thrower, ItemStack item) {
		super(world, thrower);
		setItem(item);
		
		damageAttr = (float) thrower.getEntityAttribute(attackDamage).getAttributeValue();
		if (thrower.isSprinting())
			knockbackStr = 1;
		if (thrower.fallDistance > 0.0F && !thrower.onGround && !thrower.isOnLadder()
				&& !thrower.isInWater() && !thrower.isPotionActive(Potion.blindness)
				&& thrower.ridingEntity == null)
			setIsCritical(true);
		
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
		if (pickUpType.canBePickedUpBy(SURVIVAL)) {
			ItemStack item = getItem();
			return item != null ? item.getItem().getEntityLifespan(item, worldObj) : 0;
		}
		return super.getLifespan();
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
	
	private static float modAngle(float rotation) {
		if (rotation < 0)
			for (; rotation < -180; rotation += 360);
		else
			for (; rotation >= 180; rotation -= 360);
		return rotation;
	}
	
	@Override public void onUpdate() {
		/*
		pro4j().debug("EntityTomahawk.%s [%s at (%.0f, %.0f, %.0f)]",
				"onUpdate", getItem(), posX, posY, posZ);
		*/
		
		super.onUpdate();
		
		if (getState() == IN_AIR || getState() == NO_REBOUNCE) {
			setRotation(modAngle(getRotation() + 71 * getSpinFactor(false)));
			
			if (getAfterHit() < 0) {
				if (getIsCritical() ?
						ticksInAir.get() % 6 == 5
						: ticksInAir.get() % 7 == 6)
					worldObj.playSoundAtEntity(this, "random.bow", 1,
							1 / (rand.nextFloat() * 0.4F + 1.2F) + 0.5F);
				
				if (getIsCritical())
					for (int i = 0; i < 4; i++)
						worldObj.spawnParticle("crit",
								posX + motionX * i / 4.0, 
								posY + motionY * i / 4.0,
								posZ + motionZ * i / 4.0,
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
		if (getAfterHit() < 0 && mop.entityHit != null
				&& mop.entityHit.canAttackWithItem()
				&& !mop.entityHit.hitByEntity(this)) {
			
			ItemStack item = getItem();
			
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
						thrower != null ? causeThrownDamage(this, thrower) : causeThrownDamage(this, this),
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
						Tomahawk.onCritical(this, mop.entityHit);
					
					if (enchCrit > 0)
						Tomahawk.onEnchantmentCritical(this, mop.entityHit);
					
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
			
			rebounce(mop, 0.21, true);
		}
		else {
			float angle = getRotation() + 45;
			if (mop.sideHit == DOWN.ordinal())
				angle += 90 * (getIsForwardSpin() ? 1 : -1);
			else if (mop.sideHit == UP.ordinal())
				angle -= 90 * (getIsForwardSpin() ? 1 : -1);
			
			if (ANGLE_RANGE.contains(modAngle(angle)))
				super.onImpact(mop);
			else {
				ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
				mop.hitInfo = worldObj.getWorldVec3Pool().getVecFromPool(
						dir.offsetX, dir.offsetY, dir.offsetZ);
				
				rebounce(mop, 0.09, false);
				
				double v2 = motionX * motionX + motionY * motionY + motionZ * motionZ;
				if (v2 < 1/9D)
					setState(NO_REBOUNCE);
				
				playHitSound(v2);
			}
		}
		
		setAfterHit(0);
	}

	private static final Range<Float> ANGLE_RANGE = Range.closed(
			(float) (atan(4/9D) * 180 / PI) - 45, 180 - (float) (atan(2) * 180 / PI));
	
	
	protected float getSpinFactor(boolean isSignApplicable) {
		return (getAfterHit() >= 0 ? 55/72F : (getIsCritical() ? 7/6F : 1))
				* (!isSignApplicable || getIsForwardSpin() ? 1 : -1);
	}
	
	@Override protected void onMotionTick(float hv, float v, float resistance) {
		super.onMotionTick(hv, v, resistance);
		
		if (getState() != ON_GROUND) {
			float r = 0.018F * getSpinFactor(true);
			
			double hr = 1 - r * motionY / v;
			motionX *= hr;
			motionY *= 1 + Math.signum(motionY) * r * hv / v;
			motionZ *= hr;
		}
		
	}
	
	@Override protected void onRebounce(MovingObjectPosition mop, double nY, double n, double react) {
		
		double r = react * 39 * getSpinFactor(true);
		
		double hr = 1 - r * nY / n;
		double vr = 1 + Math.signum(motionY) * r * Math.sqrt(n * n - nY * nY) / n;
		motionX *= hr;
		motionY *= vr;
		motionZ *= hr;
		
		if (mop.entityHit != null) {
			double har = -0.04 * (hr - 1) / hr;
			
			mop.entityHit.motionX += har * motionX;
			mop.entityHit.motionY += -0.04 * (vr - 1) / vr * motionY;
			mop.entityHit.motionZ += har * motionZ;
		}
		
		setIsForwardSpin(!getIsForwardSpin());
		setRotation(modAngle(getRotation() + 180));
	}

}
