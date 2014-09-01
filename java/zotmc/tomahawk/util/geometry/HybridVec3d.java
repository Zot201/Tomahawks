package zotmc.tomahawk.util.geometry;

import net.minecraft.nbt.NBTTagCompound;

public class HybridVec3d extends CylindricalVec3d {
	
	private Double x, z;
	
	public HybridVec3d() { }
	
	
	// internal
	
	@Override void addRho(double rho, AbsCylindricalVec3d vec, double f) {
		super.addRho(rho, vec, f);
		if (x != null)
			this.x += f * vec.x();
		if (z != null)
			this.z += f * vec.z();
	}
	@Override void multiplyRho(double f) {
		super.multiplyRho(f);
		if (x != null)
			x *= f;
		if (z != null)
			z *= f;
	}
	
	
	// getters
	
	@Override public double x() {
		return x != null ? x : (x = super.x());
	}
	@Override public double z() {
		return z != null ? z : (z = super.z());
	}
	
	
	// setters
	
	@Override public void setX(double x) {
		if (this.x == null || x != this.x) {
			super.setX(x);
			this.x = x;
		}
	}
	@Override public void setZ(double z) {
		if (this.z == null || z != this.z) {
			super.setZ(z);
			this.z = z;
		}
	}
	
	@Override public void setValues(double x, double y, double z) {
		if (this.x == null || this.z == null || x != this.x || z != this.z) {
			super.setValues(x, y, z);
			this.x = x;
			this.z = z;
		}
		else
			setY(y);
	}
	
	@Override public void setValues(Vec3d vec) {
		if (vec instanceof HybridVec3d) {
			HybridVec3d h = (HybridVec3d) vec;
			super.setValues(vec);
			x = h.x;
			z = h.z;
		}
		else
			super.setValues(vec);
	}
	
	
	// minecraft
	
	@Override public NBTTagCompound writeToNBT() {
		NBTTagCompound tags = super.writeToNBT();
		if (x != null)
			tags.setDouble("x", x);
		if (z != null)
			tags.setDouble("z", z);
		return tags;
	}
	
	@Override public void readFromNBT(NBTTagCompound tags) {
		super.readFromNBT(tags);
		if (tags.hasKey("x"))
			x = tags.getDouble("x");
		if (tags.hasKey("z"))
			z = tags.getDouble("z");
	}

}
