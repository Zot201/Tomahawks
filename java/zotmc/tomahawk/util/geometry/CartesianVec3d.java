package zotmc.tomahawk.util.geometry;

import net.minecraft.nbt.NBTTagCompound;

public class CartesianVec3d extends Vec3d {

	private double x, y, z;
	
	public CartesianVec3d() { }
	
	public CartesianVec3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public CartesianVec3d(Vec3d vec) {
		this(vec.x(), vec.y(), vec.z());
	}
	
	
	// getters
	
	@Override public double x() {
		return x;
	}
	@Override public double y() {
		return y;
	}
	@Override public double z() {
		return z;
	}
	
	
	// setters
	
	@Override public void setX(double x) {
		this.x = x;
	}
	@Override public void setY(double y) {
		this.y = y;
	}
	@Override public void setZ(double z) {
		this.z = z;
	}
	
	
	// minecraft
	
	@Override public NBTTagCompound writeToNBT() {
		NBTTagCompound tags = super.writeToNBT();
		tags.setDouble("x", x);
		tags.setDouble("y", y);
		tags.setDouble("z", z);
		return tags;
	}
	
	@Override public void readFromNBT(NBTTagCompound tags) {
		x = tags.getDouble("x");
		y = tags.getDouble("y");
		z = tags.getDouble("z");
	}

}
