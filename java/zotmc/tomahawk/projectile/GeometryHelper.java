package zotmc.tomahawk.projectile;

import static zotmc.tomahawk.util.Utils.sqrt;
import static zotmc.tomahawk.util.geometry.SideHit.DOWN;
import static zotmc.tomahawk.util.geometry.SideHit.EAST;
import static zotmc.tomahawk.util.geometry.SideHit.NORTH;
import static zotmc.tomahawk.util.geometry.SideHit.SOUTH;
import static zotmc.tomahawk.util.geometry.SideHit.UP;
import static zotmc.tomahawk.util.geometry.SideHit.WEST;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import zotmc.tomahawk.core.PlayerTracker;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.geometry.CartesianVec3d;
import zotmc.tomahawk.util.geometry.SideHit;
import zotmc.tomahawk.util.geometry.Vec3d;

public class GeometryHelper {
	
	public static Vec3d motion(Entity entity) {
		Vec3d ret = entity instanceof EntityPlayer ? PlayerTracker.get((EntityPlayer) entity).getLastMotion()
			: new CartesianVec3d(entity.motionX, entity.motionY, entity.motionZ);
		
		return ret.isNaN() ? Vec3d.zero() : ret;
	}
	
	public static MovingObjectPosition calculateInterceptAdjusted(AxisAlignedBB aabb, Vec3 a, Vec3 b) {
		MovingObjectPosition ret = calculateIntercept(aabb, a, b);
		
		if (ret != null) {
			Vec3d normal;
			
			if (ret.sideHit == UP || ret.sideHit == DOWN)
				normal = SideHit.of(ret.sideHit).asVec3d();
			else {
				double x = ret.hitVec.xCoord - (aabb.minX + aabb.maxX) / 2;
				double z = ret.hitVec.zCoord - (aabb.minZ + aabb.maxZ) / 2;
				double r = sqrt(x * x + z * z);
				
				double x1 = a.xCoord - b.xCoord;
				double z1 = a.zCoord - b.zCoord;
				double r1 = sqrt(x1 * x1 + z1 * z1);
				
				x = x / r * 2.5 + x1 / r1;
				z = z / r * 2.5 + z1 / r1;
				r = sqrt(x * x + z * z);
				
				normal = new CartesianVec3d(x / r, 0, z / r);
			}
			
			ret.hitInfo = normal;
		}
		
		return ret;
	}
	
	private static MovingObjectPosition calculateIntercept(AxisAlignedBB aabb, Vec3 a, Vec3 b) {
		Vec3 x = getIntermediateWithXValue(a, b, b.xCoord < a.xCoord ? aabb.maxX : aabb.minX);
		Vec3 y = getIntermediateWithYValue(a, b, b.yCoord < a.yCoord ? aabb.maxY : aabb.minY);
		Vec3 z = getIntermediateWithZValue(a, b, b.zCoord < a.zCoord ? aabb.maxZ : aabb.minZ);

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
			int face = -1;
			if (hit == x)
				face = b.xCoord < a.xCoord ? EAST : WEST;
			else if (hit == y)
				face = b.yCoord < a.yCoord ? UP : DOWN;
			else if (hit == z)
				face = b.zCoord < a.zCoord ? SOUTH : NORTH;
			
			return new MovingObjectPosition(0, 0, 0, face, hit);
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

	private static Vec3 getIntermediateWithXValue(Vec3 a, Vec3 b, double k) {
		double x = b.xCoord - a.xCoord;
		double y = b.yCoord - a.yCoord;
		double z = b.zCoord - a.zCoord;
		
		double r = (k - a.xCoord) / x;
		
		if (r == r) {
			x = k;
			y = a.yCoord + y * r;
			z = a.zCoord + z * r;
			
			if (y == y && z == z)
				return Utils.vec3(x, y, z);
		}
		
		return null;
	}
	private static Vec3 getIntermediateWithYValue(Vec3 a, Vec3 b, double k) {
		double x = b.xCoord - a.xCoord;
		double y = b.yCoord - a.yCoord;
		double z = b.zCoord - a.zCoord;
		
		double r = (k - a.yCoord) / y;
		
		if (r == r) {
			x = a.xCoord + x * r;
			y = k;
			z = a.zCoord + z * r;
			
			if (x == x && z == z)
				return Utils.vec3(x, y, z);
		}
		
		return null;
		
	}
	private static Vec3 getIntermediateWithZValue(Vec3 a, Vec3 b, double k) {
		double x = b.xCoord - a.xCoord;
		double y = b.yCoord - a.yCoord;
		double z = b.zCoord - a.zCoord;
		
		double r = (k - a.zCoord) / z;
		
		if (r == r) {
			x = a.xCoord + x * r;
			y = a.yCoord + y * r;
			z = k;
			
			if (x == x && y == y)
				return Utils.vec3(x, y, z);
		}
		
		return null;
		
	}

}
