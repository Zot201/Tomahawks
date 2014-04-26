package zotmc.tomahawk;

import static net.minecraft.enchantment.Enchantment.fireAspect;
import static net.minecraft.enchantment.Enchantment.knockback;
import static net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel;
import static net.minecraft.entity.SharedMonsterAttributes.attackDamage;
import static net.minecraft.util.DamageSource.causeThrownDamage;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityTomahawk extends AbstractTomahawk {

	private static final Random RAND = new Random();
	final float deltaYaw = worldObj.isRemote ? (float) RAND.nextGaussian() * 8 : 0;
	private float damageAttr;
	private int knockbackStr;
	public boolean canBePickedUp = true;
	
	public EntityTomahawk(World world) {
		super(world);
	}
	public EntityTomahawk(World world, EntityPlayer thrower, ItemStack item) {
		super(world, thrower);
		
		setItem(item);
		
		
		damageAttr = (float) thrower.getEntityAttribute(attackDamage).getAttributeValue();
		
		if (thrower.isSprinting()) {
			knockbackStr = 1;
			thrower.setSprinting(false);
		}
		
		if (thrower.fallDistance > 0.0F && !thrower.onGround && !thrower.isOnLadder()
				&& !thrower.isInWater() && !thrower.isPotionActive(Potion.blindness)
				&& thrower.ridingEntity == null)
			setIsCritical(true);
		
		thrower.addExhaustion(0.3F);
		
	}
	
	
	@Override protected void entityInit() {
		super.entityInit();
		getDataWatcher().addObject(2, (float) 0);
		getDataWatcher().addObject(3, (byte) 0);
		getDataWatcher().addObjectByDataType(10, 5);
		getDataWatcher().addObject(16, (byte) 0);
	}
	
	public void setRotation(float value) {
		getDataWatcher().updateObject(2, value);
	}
	public float getRotation() {
		return getDataWatcher().getWatchableObjectFloat(2);
	}
	
	public void setAfterHit(boolean value) {
		getDataWatcher().updateObject(3, value ? (byte) 1 : (byte) 0);
	}
	public boolean getAfterHit() {
		return (getDataWatcher().getWatchableObjectByte(3) & 1) != 0;
	}
	
	public void setItem(ItemStack value) {
		getDataWatcher().updateObject(10, value);
		getDataWatcher().setObjectWatched(10);
	}
	public ItemStack getItem() {
		return getDataWatcher().getWatchableObjectItemStack(10);
	}
	
	public void setIsCritical(boolean value) {
		getDataWatcher().updateObject(16, value ? (byte) 1 : (byte) 0);
	}
	public boolean getIsCritical() {
		return (getDataWatcher().getWatchableObjectByte(16) & 1) != 0;
	}
	
	@Override public int getLifespan() {
		if (canBePickedUp) {
			ItemStack item = getItem();
			return item.getItem().getEntityLifespan(item, worldObj);
		}
		return super.getLifespan();
	}
	
	
	
	@Override public void writeEntityToNBT(NBTTagCompound tags) {
		super.writeEntityToNBT(tags);
		
		tags.setFloat("rotation", getRotation());
		tags.setBoolean("afterHit", getAfterHit());
		tags.setTag("item", getItem().writeToNBT(new NBTTagCompound()));
		tags.setFloat("damageAttr", damageAttr);
		tags.setShort("knockbackStr", (short) knockbackStr);
		tags.setBoolean("canBePickedUp", canBePickedUp);
		
	}
	
	@Override public void readEntityFromNBT(NBTTagCompound tags) {
		super.readEntityFromNBT(tags);
		
		setRotation(tags.getFloat("rotation"));
		setAfterHit(tags.getBoolean("afterHit"));
		setItem(ItemStack.loadItemStackFromNBT(tags.getCompoundTag("item")));
		damageAttr = tags.getFloat("damageAttr");
		knockbackStr = tags.getShort("knockbackStr");
		canBePickedUp = tags.getBoolean("canBePickedUp");
		
	}
	
	
	
	@Override public void onUpdate() {
		super.onUpdate();
		
		if (!inGround) {
			boolean afterHit = getAfterHit();
			
			float rotation = getRotation() + (afterHit ? 32 : 72);
			if (rotation >= 180)
				rotation -= 360;
			setRotation(rotation);
			
			if (!afterHit) {
				if (ticksInAir.get() % 7 == 6)
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
		}
		
	}
	
	@Override public void onCollideWithPlayer(EntityPlayer player) {
		if (!worldObj.isRemote && (inGround || getAfterHit()) && throwableShake <= 0) {
			boolean flag = canBePickedUp || !canBePickedUp && player.capabilities.isCreativeMode;
			
			ItemStack item = getItem();
			if (canBePickedUp && !player.inventory.addItemStackToInventory(item))
				flag = false;
			
			if (flag) {
				playSound("random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1) * 2);
				player.onItemPickup(this, item.stackSize);
				setDead();
			}
		}
	}
	
	@Override protected void onImpact(MovingObjectPosition mop) {
		if (!getAfterHit() && mop.entityHit != null
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
				
				
				EntityLivingBase thrower = getThrower();
				
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
					
					try {
						Enchs.applyEnchantmentDamageIterator(thrower, item, mop.entityHit);
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
						
						if (item.stackSize <= 0)
							setDead();
						
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
			
			
			double nX = posX - mop.hitVec.xCoord;
			double nY = posY - mop.hitVec.yCoord;
			double nZ = posZ - mop.hitVec.zCoord;
			double n = Math.sqrt(nX * nX + nY * nY + nZ * nZ);
			nX /= n;
			nY /= n;
			nZ /= n;
			
			posX += motionX;
			posY += motionY;
			posZ += motionZ;
			
			double r = 2 * (motionX * nX + motionY * nY + motionZ * nZ);
			motionX = 0.25 * (motionX - r * nX);
			motionY = 0.25 * (motionY - r * nY);
			motionZ = 0.25 * (motionZ - r * nZ);
			motionY += 0.1;
			
			setAfterHit(true);
		}
		else
			super.onImpact(mop);
	}
	

}
