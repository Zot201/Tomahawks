package zotmc.tomahawk.mob;

import static net.minecraft.util.MathHelper.clamp_float;
import static net.minecraft.util.MathHelper.floor_float;
import static net.minecraft.util.MathHelper.sqrt_double;
import static zotmc.tomahawk.LogTomahawk.mob4j;
import static zotmc.tomahawk.Tomahawk.axetomahawk;
import static zotmc.tomahawk.mob.EventListener.damaging;
import static zotmc.tomahawk.mob.EventListener.getStackTagCompound;
import static zotmc.tomahawk.mob.MobStorage.DAMAGING_DROP;
import static zotmc.tomahawk.mob.MobStorage.DROP_MODE;
import static zotmc.tomahawk.mob.MobStorage.NO_DROP;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.MOB;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.SURVIVAL;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import zotmc.tomahawk.projectile.EntityTomahawk.PickUpType;

import com.google.common.base.Supplier;

public class AITomahawkThrowing extends EntityAIBase {

    private final boolean isForwardSpin;
    private final EntityLiving attacker;
    private final Supplier<? extends EntityLivingBase> targets;
    private EntityLivingBase target;
    private int attackTime = -1;
    private double movementSpeed;
    private int ticksProceed;
    private int minAttackTime;
    private int maxAttackTime;
    private float maxRange;
    private float maxRangeSq;

    public AITomahawkThrowing(boolean isForwardSpin, EntityLiving attacker) {
    	this(isForwardSpin, attacker, null);
    }
    public AITomahawkThrowing(boolean isForwardSpin,
    		EntityLiving attacker, Supplier<? extends EntityLivingBase> targets) {
    	this(isForwardSpin, attacker, targets, 1, 12, 18, 12);
    }
    public AITomahawkThrowing(boolean isForwardSpin,
    		EntityLiving attacker, Supplier<? extends EntityLivingBase> targets,
    		double movementSpeed, int minAttackTime, int maxAttackTime, float maxRange) {
    	
        this.attacker = attacker;
        this.targets = targets;
        this.isForwardSpin = isForwardSpin;
        
        this.movementSpeed = movementSpeed;
        this.minAttackTime = minAttackTime;
        this.maxAttackTime = maxAttackTime;
        this.maxRange = maxRange;
        maxRangeSq = maxRange * maxRange;
        setMutexBits(3);
    }

    public boolean shouldExecute() {
    	if (MobStorage.size(attacker) > 0) {
	    	
	        EntityLivingBase target = targets != null ? targets.get() : attacker.getAttackTarget();
	        if (target != null && target.isEntityAlive()) {
	            this.target = target;
	            return true;
	        }
    	}
        
        return false;
    }

    public boolean continueExecuting() {
        return shouldExecute() || !attacker.getNavigator().noPath();
    }

    public void resetTask() {
        target = null;
        ticksProceed = 0;
        attackTime = -1;
    }

    public void updateTask() {
        double distSq = attacker.getDistanceSq(
        		target.posX, target.boundingBox.minY, target.posZ);
        boolean canSee = attacker.getEntitySenses().canSee(target);
        
        ticksProceed = canSee ? ticksProceed + 1 : 0;
        
        if (distSq <= maxRangeSq && ticksProceed >= 20)
        	attacker.getNavigator().clearPathEntity();
        else
            attacker.getNavigator().tryMoveToEntityLiving(target, movementSpeed);

        attacker.getLookHelper().setLookPositionWithEntity(target, 30, 30);
        
        float rangeFraction;
        if (--attackTime == 0) {
            if (distSq > (double)this.maxRangeSq || !canSee)
                return;

            rangeFraction = sqrt_double(distSq) / this.maxRange;

            throwTomahawk(clamp_float(rangeFraction, 0.1F, 1.0F));
            attackTime = floor_float(rangeFraction * (maxAttackTime - minAttackTime) + minAttackTime);
        }
        else if (attackTime < 0) {
        	
            rangeFraction = sqrt_double(distSq) / maxRange;
            attackTime = floor_float(rangeFraction * (maxAttackTime - minAttackTime) + minAttackTime);
        }
        
    }
    
    public void throwTomahawk(float rangeFraction) {
    	MobStorage storage = MobStorage.getStorage(attacker);
    	if (storage.size() > 0) {
	    	attacker.faceEntity(target, 180, 180);
	    	attacker.rotationPitch -= 8 * rangeFraction;
	    	
	    	ItemStack toThrow = storage.pollLast();
	    	
	    	NBTTagCompound tags = getStackTagCompound(toThrow);
	    	int dropMode = tags.getByte(DROP_MODE);
	    	tags.removeTag(DROP_MODE);
	    	
	    	if (dropMode == DAMAGING_DROP)
	    		toThrow = damaging(toThrow, attacker.getRNG());
	    	
	    	PickUpType pick = dropMode == NO_DROP ? MOB : SURVIVAL;
	    	axetomahawk.throwTomahawk(attacker, toThrow, attacker.worldObj, isForwardSpin, pick);
	    	
	    	mob4j().debug("%s %s has thrown an EntityTomahawk %s [PickUpType: %s]",
	    			attacker.getClass().getSimpleName(), attacker.getEntityId(), toThrow, pick);
    	}
    }

}
