package zotmc.tomahawk;

import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public abstract class AbstractTomahawk extends EntityThrowable {
	
	private static final Field
	X = findField("field_145788_c"),
	Y = findField("field_145786_d"),
	Z = findField("field_145787_e"),
	TICKS_IN_GROUND = findField("ticksInGround", "field_70194_h"),
	TICKS_IN_AIR = findField("ticksInAir", "field_70195_i"),
	GROUND = findField("field_145785_f");
	
	protected final FieldAccess<Integer>
	x = FieldAccess.of(X, this),
	y = FieldAccess.of(Y, this),
	z = FieldAccess.of(Z, this),
	ticksInGround = FieldAccess.of(TICKS_IN_GROUND, this),
	ticksInAir = FieldAccess.of(TICKS_IN_AIR, this);
	protected final FieldAccess<Block>
	ground = FieldAccess.of(GROUND, this);
	
	private static Field findField(String... names) {
		return Tomahawk.findField(EntityThrowable.class, names);
	}
	
	
	
	public AbstractTomahawk(World world) {
		super(world);
	}
	
	public AbstractTomahawk(World world, EntityPlayer thrower) {
		super(world, thrower);
		setSize(0.5F, 0.5F);
	}

	
	protected float getDragFactor() {
		return 0.015F;
	}
	
	@Override protected float getGravityVelocity() {
		return 0.09F;
	}
	
	protected int getLifespan() {
		return 1200;
	}
	
	
	
	@Override public void onUpdate() {
		onEntityUpdate();
	}
	
	@Override public void onEntityUpdate() {
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;
		
		super.onEntityUpdate();

		if (throwableShake > 0)
			--throwableShake;
		
		
		if (inGround) {
			if (worldObj.getBlock(x.get(), y.get(), z.get()) == ground.get()) {
				ticksInGround.set(ticksInGround.get() + 1);

				if (ticksInGround.get() == getLifespan())
					this.setDead();

				return;
			}

			inGround = false;
			motionX *= rand.nextFloat() * 0.2F;
			motionY *= rand.nextFloat() * 0.2F;
			motionZ *= rand.nextFloat() * 0.2F;
			ticksInGround.set(0);
			ticksInAir.set(0);
		}
		else
			ticksInAir.set(ticksInAir.get() + 1);
		
		
		
		Vec3 pos = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
		Vec3 pos1 = worldObj.getWorldVec3Pool().getVecFromPool(
				posX + motionX, posY + motionY, posZ + motionZ);
		
		MovingObjectPosition mop = this.worldObj.rayTraceBlocks(pos, pos1);
		
		pos = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
		pos1 = worldObj.getWorldVec3Pool().getVecFromPool(
				posX + motionX, posY + motionY, posZ + motionZ);

		if (mop != null)
			pos1 = worldObj.getWorldVec3Pool().getVecFromPool(
					mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
		

		if (!worldObj.isRemote) {
			
			Entity nearest = null;
			
			@SuppressWarnings("unchecked")
			List<Entity> onTrack = worldObj.getEntitiesWithinAABBExcludingEntity(
					this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1, 1, 1));
			
			double min = 0;
			EntityLivingBase thrower = getThrower();

			for (int i = 0; i < onTrack.size(); i++) {
				Entity candidate = onTrack.get(i);

				if (candidate.canBeCollidedWith() && (candidate != thrower || ticksInAir.get() >= 5)) {
					float f = 0.3F;
					AxisAlignedBB aabb = candidate.boundingBox.expand(f, f, f);
					MovingObjectPosition intersect = aabb.calculateIntercept(pos, pos1);

					if (intersect != null) {
						double d = pos.distanceTo(intersect.hitVec);
						
						if (d < min || min == 0) {
							nearest = candidate;
							min = d;
						}
					}
				}
			}

			if (nearest != null)
				mop = new MovingObjectPosition(nearest);
		}

		if (mop != null)
			if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
					&& worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ) == Blocks.portal)
				setInPortal();
			else
				onImpact(mop);
		

		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		
		float hv = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
		rotationYaw = (float) (Math.atan2(motionX, motionZ) * 180 / Math.PI);

		for (rotationPitch = (float) (Math.atan2(motionY, hv) * 180 / Math.PI);
				rotationPitch - prevRotationPitch < -180;
				prevRotationPitch -= 360.0F) { }

		while (rotationPitch - prevRotationPitch >= 180)
			prevRotationPitch += 360;

		while (rotationYaw - prevRotationYaw < -180)
			prevRotationYaw -= 360;

		while (rotationYaw - prevRotationYaw >= 180)
			prevRotationYaw += 360;

		rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
		rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
		
		
		float v = MathHelper.sqrt_double(hv * hv + motionY * motionY);
		float r = MathHelper.clamp_float(1 - getDragFactor() / v, 0, 1);
		float g = getGravityVelocity();

		if (isInWater()) {
			for (int i = 0; i < 4; ++i) {
				float tail = 0.25F;
				worldObj.spawnParticle("bubble",
						posX - motionX * tail, posY - motionY * tail, posZ - motionZ * tail,
						motionX, motionY, motionZ);
			}

			r *= 0.8F;
		}

		motionX *= r;
		motionY *= r;
		motionZ *= r;
		motionY -= g;
		setPosition(posX, posY, posZ);
		
	}



	@Override protected void onImpact(MovingObjectPosition mop) {
		if (mop.typeOfHit == BLOCK) {
			x.set(mop.blockX);
			y.set(mop.blockY);
			z.set(mop.blockZ);
			ground.set(worldObj.getBlock(x.get(), y.get(), z.get()));
			
			motionX = mop.hitVec.xCoord - posX;
			motionY = mop.hitVec.yCoord - posY;
			motionZ = mop.hitVec.zCoord - posZ;
			float v = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
			posX -= motionX / v * 0.05;
			posY -= motionY / v * 0.05;
			posZ -= motionZ / v * 0.05;
			
			playSound("random.bowhit", 1, 1 / (rand.nextFloat() * 0.2F + 0.9F));
			inGround = true;
	
			if (ground.get().getMaterial() != Material.air)
				ground.get().onEntityCollidedWithBlock(this.worldObj, x.get(), y.get(), z.get(), this);
		}
		
	}
	
	
}
