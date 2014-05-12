package zotmc.tomahawk.ai;

import static net.minecraft.util.MathHelper.clamp_float;
import static net.minecraft.util.MathHelper.floor_float;
import static net.minecraft.util.MathHelper.sqrt_double;
import static zotmc.tomahawk.LogTomahawk.mob4j;
import static zotmc.tomahawk.Tomahawk.axetomahawk;
import static zotmc.tomahawk.TomahawkRegistry.isThrowableAxe;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.ENCH;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.projectile.EntityTomahawk.PickUpType;

import com.google.common.base.Supplier;

public class AITomahawkThrowing extends EntityAIBase {

    private final boolean isForwardSpin;
    private final EntityLiving attacker;
    private final Supplier<? extends EntityLivingBase> targetFactory;
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
    		EntityLiving attacker, Supplier<? extends EntityLivingBase> targetFactory) {
    	this(isForwardSpin, attacker, targetFactory, 1, 12, 18, 12);
    }
    public AITomahawkThrowing(boolean isForwardSpin,
    		EntityLiving attacker, Supplier<? extends EntityLivingBase> targetFactory,
    		double movementSpeed, int minAttackTime, int maxAttackTime, float maxRange) {
    	
        this.attacker = attacker;
        this.targetFactory = targetFactory;
        this.isForwardSpin = isForwardSpin;
        
        this.movementSpeed = movementSpeed;
        this.minAttackTime = minAttackTime;
        this.maxAttackTime = maxAttackTime;
        this.maxRange = maxRange;
        maxRangeSq = maxRange * maxRange;
        setMutexBits(3);
    }

    public boolean shouldExecute() {
    	if (isThrowableAxe(attacker.getHeldItem())) {
	    	
	        EntityLivingBase target = targetFactory != null ?
	        		targetFactory.get() : attacker.getAttackTarget();
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
            if (distSq > maxRangeSq || !canSee)
                return;

            rangeFraction = sqrt_double(distSq) / maxRange;

            throwTomahawk(clamp_float(rangeFraction, 0.1F, 1.0F));
            attackTime = floor_float(rangeFraction * (maxAttackTime - minAttackTime) + minAttackTime);
        }
        else if (attackTime < 0) {
        	
            rangeFraction = sqrt_double(distSq) / maxRange;
            attackTime = floor_float(rangeFraction * (maxAttackTime - minAttackTime) + minAttackTime);
        }
        
    }
    
    public void throwTomahawk(float rangeFraction) {
    	attacker.faceEntity(target, 180, 180);
    	attacker.rotationPitch -= 8 * rangeFraction;
    	
    	ItemStack toThrow = attacker.getHeldItem();
    	PickUpType pick = ENCH;
    	axetomahawk.throwTomahawk(
    			attacker, toThrow, attacker.worldObj, isForwardSpin, pick);
    	
    	mob4j().debug("%s %s has thrown an EntityTomahawk %s [PickUpType: %s]",
    			attacker.getClass().getSimpleName(), attacker.getEntityId(), toThrow, pick);
    }

}
