package zotmc.tomahawk.util.geometry;

import static java.util.FormattableFlags.LEFT_JUSTIFY;

import java.util.Formattable;
import java.util.Formatter;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zotmc.tomahawk.util.IdentityBlockMeta;

public abstract class Vec3i implements Formattable {
	
	protected Vec3i() { }
	
	
	// getters
	
	public abstract int x();
	public abstract int y();
	public abstract int z();
	
	
	// setters
	
	public abstract void setX(int x);
	public abstract void setY(int y);
	public abstract void setZ(int z);
	
	public void setValues(int x, int y, int z) {
		setX(x);
		setY(y);
		setZ(z);
	}
	
	
	// minecraft
	
	public void setBlockPos(MovingObjectPosition mop) {
		setValues(mop.blockX, mop.blockY, mop.blockZ);
	}
	
	public Block getBlock(World world) {
		return world.getBlock(x(), y(), z());
	}
	public IdentityBlockMeta getBlockMeta(World world) {
		int x = x(), y = y(), z = z();
		return IdentityBlockMeta.of(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
	}
	public float getBlockHardness(World world, Block block) {
		return block.getBlockHardness(world, x(), y(), z());
	}
	public void onEntityCollidedWithBlock(World world, Block block, Entity entity) {
		block.onEntityCollidedWithBlock(world, x(), y(), z(), entity);
	}
	
	
	// conversion
	
	private Vec3d vec3d;
	public Vec3d asVec3d() {
		return vec3d != null ? vec3d : (vec3d = new Vec3d() {
			@Override public double x() {
				return Vec3i.this.x();
			}
			@Override public double y() {
				return Vec3i.this.y();
			}
			@Override public double z() {
				return Vec3i.this.z();
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
		});
	}
	
	
	// string

	@Override public void formatTo(Formatter formatter, int flags, int width, int precision) {
		String f = (flags & LEFT_JUSTIFY) != 0 ? "-" : "";
		String w = width == -1 ? "" : Integer.toString(width);
		
		String coord = "%" + f + w + "d";
		formatter.format("(" + coord + ", " + coord + ", " + coord + ")", x(), y(), z());
	}
	
	@Override public String toString() {
		return "(" + x() + ", " + y() + ", " + z() + ")";
	}
	
}
