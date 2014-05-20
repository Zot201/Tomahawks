package zotmc.tomahawk.projectile;

import static net.minecraft.util.MathHelper.clamp_float;
import static net.minecraft.util.MovingObjectPosition.MovingObjectType.BLOCK;
import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.WEST;
import static zotmc.tomahawk.LogTomahawk.phy4j;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.IN_AIR;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.IN_GROUND;
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.ON_GROUND;
import static zotmc.tomahawk.util.Utils.PI;
import static zotmc.tomahawk.util.Utils.atan2;
import static zotmc.tomahawk.util.Utils.floor;
import static zotmc.tomahawk.util.Utils.sqrt;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import zotmc.tomahawk.PositionTracker;
import zotmc.tomahawk.util.FieldAccess;
import zotmc.tomahawk.util.Obfs;

public abstract class AbstractTomahawk extends EntityArrow {
	
	private static final Field
	X = findField("field_145791_d"),
	Y = findField("field_145792_e"),
	Z = findField("field_145789_f"),
	TICKS_IN_GROUND = findField("ticksInGround", "field_70252_j"),
	TICKS_IN_AIR = findField("ticksInAir", "field_70257_an"),
	GROUND = findField("field_145790_g"),
	THROWER = findField("shootingEntity", "field_70250_c");
	
	protected final FieldAccess<Integer>
	x = FieldAccess.of(X, this),
	y = FieldAccess.of(Y, this),
	z = FieldAccess.of(Z, this),
	ticksInGround = FieldAccess.of(TICKS_IN_GROUND, this),
	ticksInAir = FieldAccess.of(TICKS_IN_AIR, this);
	protected final FieldAccess<Block>
	ground = FieldAccess.of(GROUND, this);
	protected final FieldAccess<Entity>
	thrower = FieldAccess.of(THROWER, this);
	
	private static Field findField(String... names) {
		return Obfs.findField(EntityArrow.class, names);
	}
	
	
	
	public enum State {
		IN_AIR,
		IN_GROUND,
		NO_REBOUNCE,
		ON_GROUND;
	}

	public AbstractTomahawk(World world) {
		super(world);
	}
	public AbstractTomahawk(World world, double x, double y, double z, float initialSpeed) {
		super(world, x, y, z);
		setSize(0.8F, 1);
		setThrowableHeading(motionX, motionY, motionZ, initialSpeed, 1);
	}
	public AbstractTomahawk(World world, EntityLivingBase thrower, float initialSpeed) {
		super(world, thrower, initialSpeed / 1.5F);

		setSize(0.8F, 1);
        setPosition(thrower.posX, thrower.posY + thrower.getEyeHeight(), thrower.posZ);
		
		if (!world.isRemote) {
			phy4j().debug("thrower: %s %s %s",
					thrower.posX, thrower.posY, thrower.posZ);
			phy4j().debug("axe: %s %s %s",
					posX, posY, posZ);
		}
		
		if (!world.isRemote) {
			if (thrower instanceof EntityPlayer) {
				double[] m = PositionTracker
						.get((EntityPlayer) thrower)
						.getCurrentMotion();
				
				motionX += m[0];
				motionY += m[1];
				motionZ += m[2];
			}
			else {
				motionX += thrower.motionX;
				motionY += thrower.motionY;
				motionZ += thrower.motionZ;
			}
			
			setThrowableHeading(motionX, motionY, motionZ, initialSpeed, 1);
		}
		
		updateRotationYaw();
		
		
	}
	
	@Override protected void entityInit() {
		super.entityInit();
		
		getDataWatcher().addObject(5, (byte) 0);
	}
	
	protected void setState(State state) {
		getDataWatcher().updateObject(5, (byte) state.ordinal());
	}
	private void setState(byte state) {
		getDataWatcher().updateObject(5, state);
	}
	protected State getState() {
		return State.values()[getDataWatcher().getWatchableObjectByte(5)];
	}
	private byte getStateByte() {
		return getDataWatcher().getWatchableObjectByte(5);
	}
	
	@Override public void writeEntityToNBT(NBTTagCompound tags) {
		super.writeEntityToNBT(tags);
		
		tags.setByte("state", getStateByte());
	}
	@Override public void readEntityFromNBT(NBTTagCompound tags) {
		super.readEntityFromNBT(tags);
		
		setState(tags.getByte("state"));
	}
	
	public Entity getThrower() {
		return thrower.get();
	}
	
	protected int getLifespan() {
		return 1200;
	}
	
	
	
	@Override public void onUpdate() {
		onEntityUpdate();
		setPosition(posX, posY, posZ);
		
	}
	
	protected boolean onLifespanTick(int lifespan) {
		if (ticksInGround.get() >= lifespan) {
			setDead();
			
			return true;
		}
		return false;
	}
	
	protected void releaseToAir() {
		setState(IN_AIR);
		motionX *= rand.nextFloat() * 0.2F;
		motionY *= rand.nextFloat() * 0.2F;
		motionZ *= rand.nextFloat() * 0.2F;
		ticksInGround.set(0);
		ticksInAir.set(0);
		
		updateRotationYaw();
	}
	
	@Override public void onEntityUpdate() {
		lastTickPosX = posX;
		lastTickPosY = posY;
		lastTickPosZ = posZ;
		
		super.onEntityUpdate();
		
		
		switch (getState()) {
		case IN_GROUND:
			if (worldObj.getBlock(x.get(), y.get(), z.get()) == ground.get()) {
				ticksInGround.set(ticksInGround.get() + 1);
				
				onLifespanTick(getLifespan());
				
				return;
			}
			
			releaseToAir();
			break;

		case ON_GROUND:
			Block block = worldObj.getBlock(floor(posX), floor(posY) - 1, floor(posZ));
			if (block.getMaterial() != Material.air) {
				
				/*if (motionX != 0 || motionZ != 0) {
					MovingObjectPosition mop = getBlockCollision(worldObj, posX, posY, posZ, motionX, 0, motionZ);
					if (mop != null)
						onImpact(mop);
					
					posX += motionX;
					posZ += motionZ;
					
					double vH2 = motionX * motionX + motionZ * motionZ;
					double v2 = vH2 + motionY * motionY;
					onMotionTick(sqrt(vH2), sqrt(v2), block.slipperiness * 0.98F);
					motionY = 0;
				}
				else*/ {
					ticksInGround.set(ticksInGround.get() + 1);
					onLifespanTick(getLifespan());
				}
				
				return;
			}

			releaseToAir();
			break;
			
		case NO_REBOUNCE:
			ticksInAir.set(ticksInAir.get() + 1);

			MovingObjectPosition mop = getBlockCollision(
					worldObj, posX, posY, posZ, motionX, motionY, motionZ);
			if (mop != null) {
				posX = mop.hitVec.xCoord;
				posY = mop.hitVec.yCoord;
				posZ = mop.hitVec.zCoord;
				
				switch(ForgeDirection.getOrientation(mop.sideHit)) {
				case UP:
					setState(ON_GROUND);
				case DOWN:
					motionY = 0;
					break;
					
				case EAST:
				case WEST:
					motionX = 0;
					break;
					
				case NORTH:
				case SOUTH:
					motionZ = 0;
					break;
					
				default:
					break;
				}
				
				if (Math.abs(motionX) < 0.1 && Math.abs(motionY) < 0.1 && Math.abs(motionZ) < 0.1)
					setState(ON_GROUND);
				
				return;
			}
			
			posX += motionX;
			posY += motionY;
			posZ += motionZ;
			
			double vH2 = motionX * motionX + motionZ * motionZ;
			double v2 = vH2 + motionY * motionY;
			onMotionTick(sqrt(vH2), sqrt(v2), getDragFactor());

			setPosition(posX, posY, posZ);
			func_145775_I();
			
			return;
			
		default:
			ticksInAir.set(ticksInAir.get() + 1);
			
		}
		
		
		MovingObjectPosition mop = getBlockCollision(
				worldObj, posX, posY, posZ, motionX, motionY, motionZ);

		Vec3 pos = worldObj.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
		Vec3 pos1 = mop != null ?
				worldObj.getWorldVec3Pool().getVecFromPool(
						mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord) :
				worldObj.getWorldVec3Pool().getVecFromPool(
						posX + motionX, posY + motionY, posZ + motionZ);

		if (!worldObj.isRemote) {
			
			Entity nearest = null;
			MovingObjectPosition nearestIntercept = null;
			
			@SuppressWarnings("unchecked")
			List<Entity> onTrack = worldObj.getEntitiesWithinAABBExcludingEntity(
					this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.25, 1.5, 1.25));
			
			double min = 0;
			Entity thrower = getThrower();

			for (int i = 0; i < onTrack.size(); i++) {
				Entity candidate = onTrack.get(i);

				if (candidate.isEntityAlive() && candidate.canBeCollidedWith()
						&& (candidate != thrower || ticksInAir.get() >= 5)) {
					
					AxisAlignedBB aabb = candidate.boundingBox.expand(0.4, 0.5, 0.4);
					MovingObjectPosition intersect = calculateInterceptAdjusted(worldObj, aabb, pos, pos1);
					
					if (!(candidate instanceof EntityPlayer))
						phy4j().debug("%s, %s",
								aabb.isVecInside(pos),
								intersect != null);
					
					if (intersect != null) {
						double d = pos.distanceTo(intersect.hitVec);
						
						if (d < min || min == 0) {
							nearest = candidate;
							nearestIntercept = intersect;
							min = d;
						}
					}
				}
			}
			
			if (nearest != null) {
				mop = new MovingObjectPosition(nearest, nearestIntercept.hitVec);
				mop.hitInfo = nearestIntercept.hitInfo;
			}
		}
		
		if (mop != null)
			onImpact(mop);
		
		
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		
		double vH2 = motionX * motionX + motionZ * motionZ;
		double v2 = vH2 + motionY * motionY;
		onMotionTick(sqrt(vH2), sqrt(v2), getDragFactor());
		
		setPosition(posX, posY, posZ);
		func_145775_I();
		
	}
	
	protected static float modAngle(float rotation) {
		if (rotation < 0)
			for (; rotation < -180; rotation += 360);
		else
			for (; rotation >= 180; rotation -= 360);
		return rotation;
	}
	
	public void updateRotationYaw() {
		rotationYaw = modAngle(atan2(motionX, motionZ) * 180 / PI);
	}
	
	public static MovingObjectPosition getBlockCollision(World world,
			double posX, double posY, double posZ,
			double motionX, double motionY, double motionZ) {
		
		Vec3 pos = world.getWorldVec3Pool().getVecFromPool(posX, posY, posZ);
		Vec3 pos1 = world.getWorldVec3Pool().getVecFromPool(
				posX + motionX, posY + motionY, posZ + motionZ);
		
		return world.func_147447_a(pos, pos1, false, true, false);
	}
	
	private static MovingObjectPosition calculateInterceptAdjusted(World world, AxisAlignedBB aabb, Vec3 a, Vec3 b) {
		MovingObjectPosition ret = calculateIntercept(world, aabb, a, b);
		
		if (ret != null) {
			Vec3 normal;
			
			if (ret.sideHit == UP.ordinal())
				normal = world.getWorldVec3Pool().getVecFromPool(0, 1, 0);
			else if (ret.sideHit == DOWN.ordinal())
				normal = world.getWorldVec3Pool().getVecFromPool(0, -1, 0);
			else {
				double x = ret.hitVec.xCoord
						- (aabb.minX + aabb.maxX) / 2;
				double z = ret.hitVec.zCoord
						- (aabb.minZ + aabb.maxZ) / 2;
				double r = sqrt(x * x + z * z);
				
				double x1 = a.xCoord - b.xCoord;
				double z1 = a.zCoord - b.zCoord;
				double r1 = sqrt(x1 * x1 + z1 * z1);
				
				x = x / r * 2.5 + x1 / r1;
				z = z / r * 2.5 + z1 / r1;
				r = sqrt(x * x + z * z);
				
				normal = world.getWorldVec3Pool().getVecFromPool(
						x / r,
						0,
						z / r);
			}
			
			ret.hitInfo = normal;
		}
		
		return ret;
	}
	
	private static MovingObjectPosition calculateIntercept(World world, AxisAlignedBB aabb, Vec3 a, Vec3 b) {
		Vec3 x = getIntermediateWithXValue(world, a, b, b.xCoord < a.xCoord ? aabb.maxX : aabb.minX);
		Vec3 y = getIntermediateWithYValue(world, a, b, b.yCoord < a.yCoord ? aabb.maxY : aabb.minY);
		Vec3 z = getIntermediateWithZValue(world, a, b, b.zCoord < a.zCoord ? aabb.maxZ : aabb.minZ);

		if (!aabb.isVecInside(a)) {
			//phy4j().debug("%s %s %s %s", x, aabb, a, b);
			if (x != null && (!isVecInY(aabb, x) || !isVecInZ(aabb, x)))
				x = null;
			if (y != null && (!isVecInX(aabb, y) || !isVecInZ(aabb, y)))
				y = null;
			if (z != null && (!isVecInX(aabb, z) || !isVecInY(aabb, z)))
				z = null;
		}
		
		Vec3 hit = null;
		if (x != null && (hit == null || a.squareDistanceTo(x) < a.squareDistanceTo(hit)))
			hit = x;
		if (y != null && (hit == null || a.squareDistanceTo(y) < a.squareDistanceTo(hit)))
			hit = y;
		if (z != null && (hit == null || a.squareDistanceTo(z) < a.squareDistanceTo(hit)))
			hit = z;
		
		if (hit != null) {
			ForgeDirection face = null;
			if (hit == x)
				face = b.xCoord < a.xCoord ? EAST : WEST;
			else if (hit == y)
				face = b.yCoord < a.yCoord ? UP : DOWN;
			else if (hit == z)
				face = b.zCoord < a.zCoord ? SOUTH : NORTH;
			
			return new MovingObjectPosition(0, 0, 0, face.ordinal(), hit);
		}
		
		return null;
	}

	private static boolean isVecInX(AxisAlignedBB aabb, Vec3 vec) {
		return vec.xCoord >= aabb.minX && vec.xCoord <= aabb.maxX;
	}
	private static boolean isVecInY(AxisAlignedBB aabb, Vec3 vec) {
		return vec.yCoord >= aabb.minY && vec.yCoord <= aabb.maxY;
	}
	private static boolean isVecInZ(AxisAlignedBB aabb, Vec3 vec) {
		return vec.zCoord >= aabb.minZ && vec.zCoord <= aabb.maxZ;
	}

	private static Vec3 getIntermediateWithXValue(World world, Vec3 a, Vec3 b, double k) {
		double x = b.xCoord - a.xCoord;
		double y = b.yCoord - a.yCoord;
		double z = b.zCoord - a.zCoord;
		
		double r = (k - a.xCoord) / x;
		
		if (!Double.isNaN(r)) {
			x = k;
			y = a.yCoord + y * r;
			z = a.zCoord + z * r;
			
			if (!Double.isNaN(y) && !Double.isNaN(z))
				return world.getWorldVec3Pool().getVecFromPool(x, y, z);
		}
		
		return null;
	}
	private static Vec3 getIntermediateWithYValue(World world, Vec3 a, Vec3 b, double k) {
		double x = b.xCoord - a.xCoord;
		double y = b.yCoord - a.yCoord;
		double z = b.zCoord - a.zCoord;
		
		double r = (k - a.yCoord) / y;
		
		if (!Double.isNaN(r)) {
			x = a.xCoord + x * r;
			y = k;
			z = a.zCoord + z * r;
			
			if (!Double.isNaN(x) && !Double.isNaN(z))
				return world.getWorldVec3Pool().getVecFromPool(x, y, z);
		}
		
		return null;
		
	}
	private static Vec3 getIntermediateWithZValue(World world, Vec3 a, Vec3 b, double k) {
		double x = b.xCoord - a.xCoord;
		double y = b.yCoord - a.yCoord;
		double z = b.zCoord - a.zCoord;
		
		double r = (k - a.zCoord) / z;
		
		if (!Double.isNaN(r)) {
			x = a.xCoord + x * r;
			y = a.yCoord + y * r;
			z = k;
			
			if (!Double.isNaN(x) && !Double.isNaN(y))
				return world.getWorldVec3Pool().getVecFromPool(x, y, z);
		}
		
		return null;
		
	}
	
	protected void onMotionTick(float vH, float v, float resistense) {
		float r = MathHelper.clamp_float(1 - resistense / v, 0, 1);
		float g = getGravity();

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
		
	}
	
	
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.typeOfHit == BLOCK) {
			x.set(mop.blockX);
			y.set(mop.blockY);
			z.set(mop.blockZ);
			
			Block b = worldObj.getBlock(x.get(), y.get(), z.get());
			ground.set(b);
			
			motionX = mop.hitVec.xCoord - posX;
			motionY = mop.hitVec.yCoord - posY;
			motionZ = mop.hitVec.zCoord - posZ;
			float v = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
			posX -= motionX / v * 0.05;
			posY -= motionY / v * 0.05;
			posZ -= motionZ / v * 0.05;
			
			if (b.getMaterial() != Material.air) {
				playHitSound(true, b, b.getBlockHardness(worldObj, x.get(), y.get(), z.get()));
				setState(IN_GROUND);
				
				b.onEntityCollidedWithBlock(this.worldObj, x.get(), y.get(), z.get(), this);
			}
			
		}
		
	}
	
	
	protected void playHitSound(boolean isStationary, Block block, float hardness) {
		if (!worldObj.isRemote)
			playSound(
					"random.bowhit",
					1.4F,
					(1.2F / rand.nextFloat() * 0.2F + 0.9F) * 3/5F);
	}
	
	protected void playInAirSound(double v2) {
		if (!worldObj.isRemote)
			playSound(
					"random.bow",
					clamp_float((float) v2 * 16, 0, 1),
					1 / (rand.nextFloat() * 0.4F + 1.2F) + 0.5F);
	}
	
	
	protected void rebounce(MovingObjectPosition mop, double reactFactor, boolean onEntity) {
		Vec3 nVec = (Vec3) mop.hitInfo;
		
		Vec3 vVec = worldObj.getWorldVec3Pool().getVecFromPool(motionX, motionY, motionZ);
		
		onRebounce(mop, nVec.yCoord, nVec.lengthVector(), -reactFactor * nVec.dotProduct(vVec));
		
		double r = 2 * (motionX * nVec.xCoord + motionY * nVec.yCoord + motionZ * nVec.zCoord);
		motionX = reactFactor * (motionX - r * nVec.xCoord);
		motionY = reactFactor * (motionY - r * nVec.yCoord);
		motionZ = reactFactor * (motionZ - r * nVec.zCoord);
		
		double
		pX = mop.hitVec.xCoord + motionX * 0.05,
		pY = mop.hitVec.yCoord + motionY * 0.05,
		pZ = mop.hitVec.zCoord + motionZ * 0.05;
		
		MovingObjectPosition mop1 = getBlockCollision(worldObj, posX, posY, posZ,
				pX - posX, pY - posY, pZ - posZ);
		
		if (mop1 != null)
			onImpact(mop1);
		
		updateRotationYaw();
		
	}
	
	protected abstract void onRebounce(MovingObjectPosition mop, double nY, double n, double react);
	
	
	
	protected float getDragFactor() {
		return 0.06F;
	}
	
	protected float getGravity() {
		return 0.12F;
	}
	
}
