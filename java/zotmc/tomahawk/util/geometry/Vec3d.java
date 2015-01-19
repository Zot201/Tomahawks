package zotmc.tomahawk.util.geometry;

import static java.util.FormattableFlags.LEFT_JUSTIFY;

import java.util.Formattable;
import java.util.Formatter;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zotmc.tomahawk.util.IdentityBlockMeta;
import zotmc.tomahawk.util.Utils;

public abstract class Vec3d implements Formattable {
	
	Vec3d() { }

	public static Vec3d zero() {
		return ZERO;
	}
	
	
	// internal
	
	protected Vec3d copy() {
		return new CartesianVec3d(x(), y(), z());
	}
	
	private static final Vec3d ZERO = new Vec3d() {
		@Override public double x() {
			return 0;
		}
		@Override public double y() {
			return 0;
		}
		@Override public double z() {
			return 0;
		}
		
		@Override public void setX(double x) {
			throw new UnsupportedOperationException();
		}
		@Override public void setY(double y) {
			throw new UnsupportedOperationException();
		}
		@Override public void setZ(double z) {
			throw new UnsupportedOperationException();
		}
	};
	
	
	// getters
	
	public abstract double x();
	public abstract double y();
	public abstract double z();
	
	public float yaw() {
		return Utils.atan2(x(), z());
	}
	
	
	// setters
	
	public abstract void setX(double x);
	public abstract void setY(double y);
	public abstract void setZ(double z);
	
	public void setValues(double x, double y, double z) {
		setX(x);
		setY(y);
		setZ(z);
	}
	public void setValues(Vec3d vec) {
		setValues(vec.x(), vec.y(), vec.z());
	}
	public void setValues(Vec3 vec) {
		setValues(vec.xCoord, vec.yCoord, vec.zCoord);
	}
	
	
	// arithmetic mutators
	
	public void add(double x, double y, double z) {
		setValues(x() + x, y() + y, z() + z);
	}
	public void add(double x, double y, double z, double f) {
		add(f * x, f * y, f * z);
	}
	public void add(Vec3d vec) {
		add(vec.x(), vec.y(), vec.z());
	}
	public void add(Vec3d vec, double f) {
		add(vec.x(), vec.y(), vec.z(), f);
	}
	
	public void addY(double y) {
		setY(y() + y);
	}
	
	public void subtract(double x, double y, double z) {
		setValues(x() - x, y() - y, z() - z);
	}
	public void subtract(double x, double y, double z, double f) {
		subtract(f * x, f * y, f * z);
	}
	public void subtract(Vec3d vec) {
		subtract(vec.x(), vec.y(), vec.z());
	}
	public void subtract(Vec3d vec, double f) {
		subtract(vec.x(), vec.y(), vec.z(), f);
	}
	
	public void multiplyValues(double f) {
		setValues(f * x(), f * y(), f * z());
	}
	public void multiplyHorz(double f) {
		setX(f * x());
		setZ(f * z());
	}
	
	public void multiplyValues(Random rand, float f) {
		setValues(
				f * rand.nextFloat() * x(),
				f * rand.nextFloat() * y(),
				f * rand.nextFloat() * z()
		);
	}
	
	
	// vector operations
	
	public float horz() {
		return Utils.sqrt(horz2());
	}
	public double horz2() {
		double x = x(), z = z();
		return x * x + z * z;
	}
	
	public float norm() {
		return Utils.sqrt(norm2());
	}
	public double norm2() {
		double x = x(), y = y(), z = z();
		return x * x + y * y + z * z;
	}
	
	public double dot(double x, double y, double z) {
		return x() * x + y() * y + z() * z;
	}
	public double dot(Vec3d vec) {
		return dot(vec.x(), vec.y(), vec.z());
	}
	
	public Vec3d cross(double x, double y, double z) {
		double _x = x(), _y = y(), _z = z();
		return new CartesianVec3d(
				_y * z - _z * y,
				_z * x - _x * z,
				_x * y - _y * x
		);
	}
	public Vec3d cross(double x, double y, double z, double f) {
		return cross(f * x, f * y, f * z);
	}
	public Vec3d cross(Vec3d vec) {
		return cross(vec.x(), vec.y(), vec.z());
	}
	public Vec3d cross(Vec3d vec, double f) {
		return cross(vec.x(), vec.y(), vec.z(), f);
	}

	public Vec3d relativize(Vec3d vec) {
		Vec3d ret = copy();
		ret.subtract(vec);
		return ret;
	}
	
	
	// minecraft
	
	public Vec3 toVec3() {
		return Utils.vec3(x(), y(), z());
	}
	
	public NBTTagCompound writeToNBT() {
		return new NBTTagCompound();
	}
	public void readFromNBT(NBTTagCompound tags) { }
	
	public IdentityBlockMeta getBlockMeta(World world, int side) {
		SideHit s = SideHit.of(side);
		int x = Utils.floor(x()) + s.x(), y = Utils.floor(y()) + s.y(), z = Utils.floor(z()) + s.z();
		return IdentityBlockMeta.of(world.getBlockId(x, y, z), world.getBlockMetadata(x, y, z));
	}
	
	public boolean canBlockCollideCheck(World world, boolean flag) {
		int x = Utils.floor(x()), y = Utils.floor(y()), z = Utils.floor(z());
		int blockId = world.getBlockId(x, y, z);
		return blockId != 0 && Block.blocksList[blockId].canCollideCheck(world.getBlockMetadata(x, y, z), flag);
	}
	
	public AxisAlignedBB addCoord(AxisAlignedBB aabb) {
		return aabb.addCoord(x(), y(), z());
	}
	
	public void subtractMotionFrom(Entity entity, double f) {
		entity.motionX -= f * x();
		entity.motionY -= f * y();
		entity.motionZ -= f * z();
	}
	
	
	// others

	public boolean isNaN() {
		double x = x();
		if (x != x)
			return true;
		
		double y = y();
		if (y != y)
			return true;
		
		double z = z();
		if (z != z)
			return true;
		
		return false;
	}
	
	public void addTo(Vec3d vec) {
		vec.add(this);
	}
	
	public void setTo(Vec3i vec) {
		vec.setValues(Utils.floor(x()), Utils.floor(y()), Utils.floor(z()));
	}
	
	
	// string

	@Override public void formatTo(Formatter formatter, int flags, int width, int precision) {
		String f = (flags & LEFT_JUSTIFY) != 0 ? "-" : "";
		String w = width == -1 ? "" : Integer.toString(width);
		String p = precision == -1 ? "" : "." + precision;
		
		String coord = "%" + f + w + p + "f";
		formatter.format("(" + coord + ", " + coord + ", " + coord + ")", x(), y(), z());
	}
	
	@Override public String toString() {
		return "(" + x() + ", " + y() + ", " + z() + ")";
	}
	
}
