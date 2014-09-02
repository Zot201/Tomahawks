package zotmc.tomahawk.projectile;

import static zotmc.tomahawk.core.LogTomahawk.phy4j;
import static zotmc.tomahawk.projectile.EntityTomahawk.State.IN_AIR;
import static zotmc.tomahawk.projectile.EntityTomahawk.State.IN_GROUND;
import static zotmc.tomahawk.projectile.EntityTomahawk.State.NO_REBOUNCE;
import static zotmc.tomahawk.projectile.EntityTomahawk.State.ON_GROUND;
import static zotmc.tomahawk.util.Utils.PI;
import static zotmc.tomahawk.util.Utils.atan;
import static zotmc.tomahawk.util.geometry.SideHit.DOWN;
import static zotmc.tomahawk.util.geometry.SideHit.EAST;
import static zotmc.tomahawk.util.geometry.SideHit.NORTH;
import static zotmc.tomahawk.util.geometry.SideHit.SOUTH;
import static zotmc.tomahawk.util.geometry.SideHit.UP;
import static zotmc.tomahawk.util.geometry.SideHit.WEST;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.core.TomahawkImpls;
import zotmc.tomahawk.util.IdentityBlockMeta;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.geometry.Angle;
import zotmc.tomahawk.util.geometry.NormalizedAngle;
import zotmc.tomahawk.util.geometry.SideHit;
import zotmc.tomahawk.util.geometry.Vec3d;
import zotmc.tomahawk.util.prop.Props;

import com.google.common.collect.Range;

public class TickerTomahawk implements Runnable {
	
	public static final Range<Float> ANGLE_RANGE =
			Range.closed(atan(4/9D) * 180 / PI - 45, 180 - atan(2) * 180 / PI);
	
	protected final EntityTomahawk hawk;
	protected final Vec3d pos;
	private Vec3d motion;
	private boolean isDominant;
	
	public TickerTomahawk(EntityTomahawk hawk) {
		this.hawk = hawk;
		pos = hawk.pos;
		motion = hawk.entityMotion;
	}
	
	
	protected void dominant(Runnable r) {
		if (isDominant)
			throw new IllegalStateException();
		
		isDominant = true;
		hawk.projectileMotion.setValues(hawk.entityMotion);
		motion = hawk.projectileMotion;
		try {
			r.run();
		} finally {
			motion = hawk.entityMotion;
			hawk.entityMotion.setValues(hawk.projectileMotion);
			hawk.entityRotationYaw.setRadians(hawk.projectileMotion.yaw());
			isDominant = false;
		}
	}
	protected void auxiliary(Runnable r) {
		if (!isDominant)
			throw new IllegalStateException();
		
		isDominant = false;
		hawk.entityMotion.setValues(hawk.projectileMotion);
		hawk.entityRotationYaw.setRadians(hawk.projectileMotion.yaw());
		motion = hawk.entityMotion;
		try {
			r.run();
		} finally {
			motion = hawk.projectileMotion;
			hawk.projectileMotion.setValues(hawk.entityMotion);
			isDominant = true;
		}
	}
	
	protected void debugInfo(boolean spacing, String title) {
		if (!hawk.worldObj.isRemote) {
			if (title != null)
				phy4j().debug(title);
			phy4j().debug("State:        %s", hawk.state.get());
			phy4j().debug("Pos:          %.1s", pos);
			phy4j().debug("Motion:       %.3s", motion);
			phy4j().debug("Rotation Yaw: %#.1s", hawk.entityRotationYaw);
			phy4j().debug("Rotation:     %#.1s", hawk.rotation);
			if (spacing)
				phy4j().debug("");
		}
	}
	
	protected void debugInfo(String title, String format, Object... args) {
		if (!hawk.worldObj.isRemote) {
			if (title != null)
				phy4j().debug(title);
			phy4j().debug(format, args);
		}
	}
	
	@Override public void run() {
		if (hawk.item.get() == null) {
			hawk.setDead();
			return;
		}
		
		dominant(ticking);
		
		if (hawk.isBreaking.get() && hawk.rand().nextBoolean())
			hawk.onBroken();
	}
	
	private final Runnable ticking = new Runnable() { public void run() {
		switch (hawk.state.get()) {
		case IN_GROUND:
			if (!hawk.worldObj.isRemote)
				tickStationary(hawk.inTilePos.getBlockMeta(hawk.worldObj) == hawk.inTile);
			break;
			
		case ON_GROUND:
			if (!hawk.worldObj.isRemote)
				tickStationary(pos.getBlockMeta(hawk.worldObj, DOWN) == hawk.inTile);
			break;
			
		case NO_REBOUNCE:
			tickNoRebounce();
			break;
			
		case ON_RELEASE:
			tickOnRelease();
			break;
			
		default:
			break;
		}
		
		if (hawk.state.get() == IN_AIR)
			tickInAir();
		
		if (!hawk.state.get().isStationary()) {
			float spinStrength = hawk.getSpinStrength();
			
			hawk.rotation.addDegrees(57.5F * spinStrength);
			
			float p = 7 / spinStrength;
			float t = hawk.ticksInAir.get() % p;
			if (t >= p - 1)
				hawk.playInAirSound(motion.norm2());
			
			
			if (hawk.afterHit.get() < 0) {
				if (hawk.getIsCritical())
					for (int i = 0; i < 4; i++) {
						double f = i / 4.0;
						hawk.worldObj.spawnParticle("crit",
								pos.x() + motion.x() * f, 
								pos.y() + motion.y() * f,
								pos.z() + motion.z() * f,
								-motion.x(), -motion.y() + 0.2, -motion.z()
						);
					}
				
				if (hawk.worldObj.isRemote && hawk.item.get().isItemEnchanted())
					for (int i = 0; i < 3; i++)
						if (ThreadLocalRandom.current().nextInt(4) == 0) {
							double f = i / 3.0;
							hawk.worldObj.spawnParticle("magicCrit",
									pos.x() + motion.x() * f, 
									pos.y() + motion.y() * f,
									pos.z() + motion.z() * f,
									-motion.x(), -motion.y() + 0.2, -motion.z()
							);
						}
			}
			else
				Props.increment(hawk.afterHit);
		}
	}};
	
	protected void tickStationary(boolean remainStill) {
		if (remainStill) {
			if (!hawk.isPersistenceRequired()) {
				Props.increment(hawk.ticksInGround);
				
				if (hawk.ticksInGround.get() >= hawk.getLifespan())
					hawk.setDead();
			}
		}
		else {
			hawk.state.set(IN_AIR);
			hawk.onRelease(motion);
		}
	}
	
	protected void tickNoRebounce() {
		Props.increment(hawk.ticksInAir);
		
		MovingObjectPosition mop = hawk.worldObj.func_147447_a(
				pos.toVec3(),
				Utils.sumVec3(pos, motion),
				false, true, false
		);
		if (mop != null) {
			pos.setValues(mop.hitVec);
			
			//debugInfo("====Tick No Rebounce====", "Side Hit      %.3s", SideHit.of(mop.sideHit));
			
			double f;
			
			switch(mop.sideHit) {
			case UP:
				hawk.state.set(ON_GROUND);
				hawk.inTile = hawk.pos.getBlockMeta(hawk.worldObj, DOWN);
				//fall through
			case DOWN:
				f = motion.y();
				motion.setY(0);
				break;
				
			case EAST:
			case WEST:
				f = motion.x();
				motion.setX(0);
				break;
				
			case NORTH:
			case SOUTH:
				f = motion.z();
				motion.setZ(0);
				break;
				
			default:
				throw new AssertionError();
			}
			
			f = Math.max(0.2, f);
			motion.add(Utils.nextUnitVec3d(hawk.rand()), Utils.sqrt(f * f / 2));
			
			if (motion.norm() < 2) {
				Props.increment(hawk.ticksInGround);
				
				if (hawk.ticksInGround.get() > 8) {
					hawk.state.set(ON_GROUND);
					hawk.inTile = hawk.pos.getBlockMeta(hawk.worldObj, DOWN);
					hawk.ticksInGround.set(0);
				}
			}
		}
		else
			tickNoImpact();
	}
	
	protected void tickOnRelease() {
		hawk.state.set(IN_AIR); return;
		
		/*if (hawk.sideHit == -1) { // if accidently the sideHit has not been saved.
			hawk.state.set(IN_AIR);
			return;
		}
		
		motion.add(SideHit.of(hawk.sideHit).asVec3d(), 1);
		
		if (hawk.posGround.getBlock(hawk.worldObj) == hawk.ground.get()) {
			//TODO: improve algorithm
			
			//Vec3 pos0 = pos.toVec3();
			motion.addY(-0.4);
			pos.add(motion);
			
			if (hawk.worldObj.func_147461_a(hawk.boundingBox).size() > 0) {
				hawk.state.set(NO_REBOUNCE);
				hawk.onRelease(motion);
			}
		}
		else {
			hawk.state.set(IN_AIR);
			hawk.onRelease(motion);
		}*/
	}
	
	protected void tickInAir() {
		Props.increment(hawk.ticksInAir);
		
		MovingObjectPosition blockMop = hawk.worldObj.func_147447_a(
				pos.toVec3(),
				Utils.sumVec3(pos, motion),
				false, true, false
		);
		MovingObjectPosition entityMop = null;
		
		Vec3 pos0 = pos.toVec3();
		Vec3 pos1 = blockMop != null ? Utils.vec3(blockMop.hitVec) : Utils.sumVec3(pos, motion);
		
		if (!hawk.worldObj.isRemote) {
			Entity nearest = null;
			MovingObjectPosition nearestIntercept = null;
			
			@SuppressWarnings("unchecked")
			List<Entity> onTrack = hawk.worldObj.getEntitiesWithinAABBExcludingEntity(
					hawk, motion.addCoord(hawk.boundingBox).expand(1.25, 1.5, 1.25)
			);
			
			double min = 0;
			
			for (int i = 0; i < onTrack.size(); i++) {
				Entity candidate = onTrack.get(i);

				if (candidate.isEntityAlive() && candidate.canBeCollidedWith()
						&& (candidate != hawk.shootingEntity || hawk.ticksInAir.get() >= 5)) {
					
					AxisAlignedBB aabb = candidate.boundingBox.expand(0.4, 0.5, 0.4);
					MovingObjectPosition intersect =
							PositionHelper.calculateInterceptAdjusted(aabb, pos0, pos1);
					
					if (intersect != null) {
						double d = intersect.hitVec.distanceTo(pos0);
						
						if (d < min || min == 0) {
							nearest = candidate;
							nearestIntercept = intersect;
							min = d;
						}
					}
				}
			}
			
			if (nearest != null) {
				entityMop = new MovingObjectPosition(nearest, nearestIntercept.hitVec);
				entityMop.hitInfo = nearestIntercept.hitInfo;
			}
		}
		
		if (entityMop != null)
			tickEntityImpact(entityMop);
		else if (blockMop != null)
			tickBlockImpact(blockMop);
		else
			tickNoImpact();
	}
	
	protected void tickNoImpact() {
		pos.add(motion);
		
		{
			boolean inWater = hawk.isInWater();
			if (inWater)
				for (int i = 0; i < 4; ++i)
					hawk.worldObj.spawnParticle("bubble",
							pos.x() - motion.x() * 0.25F,
							pos.y() - motion.y() * 0.25F,
							pos.z() - motion.z() * 0.25F,
							motion.x(), motion.y(), motion.z()
					);
			
			motion.multiplyValues(
					Utils.closed(0, 1, (1 - hawk.getDragFactor() / motion.norm())) * (inWater ? 0.8F : 1)
			);
			motion.addY(-hawk.getGravity());
		}
		
		if (hawk.state.get() != ON_GROUND) {
			Vec3d magnus = motion.cross(hawk.spin, hawk.getSpinMagnusFactor());
			
			/*if (!hawk.worldObj.isRemote) {
				phy4j().debug("====Tick No Impact====");
				phy4j().debug("Motion:       %.3s", motion);
				phy4j().debug("Yaw:          %.3f", motion.yaw());
				phy4j().debug("Spin:         %.3s", hawk.spin);
				phy4j().debug("Sign Spin:    %+d", Props.toSignum(hawk.isForwardSpin));
				phy4j().debug("#### Magnus:  %.3s", magnus);
			}*/
			
			motion.add(magnus);
		}
		
		hawk.setPosition(pos.x(), pos.y(), pos.z());
		hawk.func_145775_I();
		
	}
	
	protected void tickEntityImpact(final MovingObjectPosition mop) {
		if (hawk.afterHit.get() < 0 && mop.entityHit != null) {
			
			auxiliary(new Runnable() { public void run() {
				TomahawkImpls.onEntityImpactImpl(hawk, motion, mop.entityHit, hawk.rand());
			}});
			
			tickRebounce(mop, hawk.getEntityRestitutionFactor());
			
			Props.increment(hawk.afterHit);
			
			if (hawk.isFragile.get())
				hawk.startBreaking();
			else {
				ItemStack item = hawk.item.get();
				SoundType hitSound = TomahawkRegistry.getItemHandler(item).getHitSound(item);
				if (hitSound != null)
					hawk.playSound(hitSound.soundName, hitSound.getVolume(), hitSound.getPitch());
			}
		}
		
		tickNoImpact();
	}
	
	protected void tickBlockImpact(MovingObjectPosition mop) {
		boolean setInGround = hawk.state.get() != ON_GROUND;
		
		if (setInGround) {
			Angle angle = NormalizedAngle.create(hawk.rotation);
			if (mop.sideHit == DOWN)
				angle.addDegrees(45 - 90 * Props.toSignum(hawk.isRolled));
			else if (mop.sideHit == UP)
				angle.addDegrees(45 + 90 * Props.toSignum(hawk.isRolled));
			else
				angle.addDegrees(45);
			
			setInGround = ANGLE_RANGE.contains(angle.toDegrees());
		}
		
		if (setInGround) {
			//debugInfo(null, "SET IN GROUND");
			
			onItemUse(mop);
			
			{
				hawk.inTilePos.setBlockPos(mop);
				
				IdentityBlockMeta b = hawk.inTilePos.getBlockMeta(hawk.worldObj);
				hawk.inTile = b;
				hawk.sideHit = mop.sideHit;
				
				motion.setValues(mop.hitVec);
				motion.subtract(pos);
				pos.subtract(motion, 0.05 / motion.norm());
				
				if (b.block.getMaterial() != Material.air) {
					if (!hawk.isFragile.get())
						hawk.playHitSound(true, b.block, hawk.inTilePos.getBlockHardness(hawk.worldObj, b.block));
					hawk.state.set(IN_GROUND);
					
					hawk.inTilePos.onEntityCollidedWithBlock(hawk.worldObj, b.block, hawk);
				}
			}
			
			hawk.setIsCritical(false);
			
			tickNoImpact();
			
			if (hawk.isFragile.get())
				hawk.startBreaking();
		}
		else {
			mop.hitInfo = SideHit.of(mop.sideHit).asVec3d();
			
			onItemUse(mop);
			
			tickRebounce(mop, hawk.getBlockRestitutionFactor());
			
			if (motion.norm2() < 1/9D) {
				hawk.state.set(NO_REBOUNCE);
				hawk.setIsCritical(false);
			}
			
			if (hawk.isFragile.get())
				hawk.startBreaking();
			else {
				Block block = hawk.worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
				
				hawk.playHitSound(false, block,
						block.getBlockHardness(hawk.worldObj, mop.blockX, mop.blockY, mop.blockZ)
				);
			}
		}
	}
	
	protected void onItemUse(final MovingObjectPosition mop) {
		final ItemStack item = hawk.item.get();
		
		if (item != null && hawk.worldObj instanceof WorldServer)
			auxiliary(new Runnable() { public void run() {
				Vec3 vec = mop.hitVec;
				item.getItem().onItemUse(item, hawk.createFakePlayer((WorldServer) hawk.worldObj), hawk.worldObj,
						mop.blockX, mop.blockY, mop.blockZ, mop.sideHit,
						(float) vec.xCoord - mop.blockX, (float) vec.yCoord - mop.blockY, (float) vec.zCoord - mop.blockZ);
			}});
	}
	
	protected void tickRebounce(MovingObjectPosition mop, double f) {
		Angle change = NormalizedAngle.createFromYawNegative(motion);
		
		{
			Vec3d normal = (Vec3d) mop.hitInfo;

			/*if (!hawk.worldObj.isRemote) {
				phy4j().debug("====Tick Rebounce====");
				phy4j().debug("Pos:          %.1s", pos);
				phy4j().debug("Motion:       %.3s", motion);
				phy4j().debug("#### Normal:  %.3s", normal);
			}*/
			
			{
				Vec3d friction = normal.cross(hawk.spin, f * hawk.getSpinFrictionFactor() * motion.dot(normal));
				
				/*if (!hawk.worldObj.isRemote) {
					phy4j().debug("====Tick Rebounce====");
					phy4j().debug("Type of Hit:  %s", mop.typeOfHit);
					phy4j().debug("Motion:       %.3s", motion);
					phy4j().debug("Normal:       %.3s", normal);
					phy4j().debug("Spin:         %.3s", hawk.spin);
					phy4j().debug("Friction:     %.3s", friction);
					phy4j().debug("");
				}*/
				
				motion.add(friction);
				
				if (mop.entityHit != null)
					friction.subtractMotionFrom(mop.entityHit, 0.02);
			}
			
			/*if (mop.typeOfHit == BLOCK)
				debugInfo("====On Impact====",
						"Side Hit    : %s", SideHit.of(mop.sideHit).name()
				);*/
			
			double r = 2 * motion.dot(normal);
			motion.setValues(
					f * (motion.x() - r * normal.x()),
					f * (motion.y() - r * normal.y()),
					f * (motion.z() - r * normal.z())
			);
			
			/*if (!hawk.worldObj.isRemote) {
				phy4j().debug("#### MOP1:    %s", mop1);
				phy4j().debug("Pos:          %.1s", pos);
				phy4j().debug("Motion:       %.3s", motion);
			}*/
		}
		
		change.addRadians(motion.yaw());
		if (Math.abs(change.toRadians()) > PI / 2) {
			//Props.toggle(hawk.isForwardSpin);
			Props.toggle(hawk.isRolled);
			hawk.rotation.addDegrees(180);
		}
		
	}

}
