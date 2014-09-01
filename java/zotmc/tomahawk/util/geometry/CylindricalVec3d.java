package zotmc.tomahawk.util.geometry;

import net.minecraft.nbt.NBTTagCompound;

public class CylindricalVec3d extends AbsCylindricalVec3d {
	
	private double y, rho;
	private int phi;
	
	CylindricalVec3d() { }
	
	CylindricalVec3d(double y, double rho, int phi) {
		this.y = y;
		this.rho = rho;
		this.phi = phi;
		normalize();
	}
	
	
	// internal
	
	protected boolean normalize() {
		if (rho < 0) {
			rho *= -1;
			phi -= Integer.MIN_VALUE;
			return true;
		}
		return false;
	}
	void addRho(double rho, AbsCylindricalVec3d vec, double f) {
		this.rho += rho;
		normalize();
	}
	void multiplyRho(double f) {
		rho *= f;
		normalize();
	}
	
	
	// getters
	
	@Override double rho() {
		return rho;
	}
	@Override int phi() {
		return phi;
	}
	
	@Override public double y() {
		return y;
	}
	
	
	// setters

	@Override public void setY(double y) {
		this.y = y;
	}
	
	@Override public void setX(double x) {
		double z = z();
		if (x == 0 && z == 0)
			rho = 0;
		else {
			rho = Math.sqrt(x * x + z * z);
			phi = atan2Int(x, z);
		}
	}
	@Override public void setZ(double z) {
		double x = x();
		if (x == 0 && z == 0)
			rho = 0;
		else {
			rho = Math.sqrt(x * x + z * z);
			phi = atan2Int(x, z);
		}
	}
	
	@Override public void setValues(double x, double y, double z) {
		this.y = y;
		if (x == 0 && z == 0)
			rho = 0;
		else {
			rho = Math.sqrt(x * x + z * z);
			phi = atan2Int(x, z);
		}
	}
	
	@Override public void setValues(Vec3d vec) {
		if (vec instanceof AbsCylindricalVec3d) {
			AbsCylindricalVec3d c = (AbsCylindricalVec3d) vec;
			y = c.y();
			rho = c.rho();
			phi = c.phi();
		}
		else
			super.setValues(vec);
	}
	
	
	// arithmetic mutators
	
	@Override public void add(Vec3d vec, double f) {
		if (f == 0)
			return;
		
		if (vec instanceof AbsCylindricalVec3d) {
			AbsCylindricalVec3d c = (AbsCylindricalVec3d) vec;
			
			if (c.rho() == 0)
				y += f * c.y();
			else {
				int cPhi = c.phi();
				if (cPhi == phi) {
					y += f * c.y();
					addRho(f * c.rho(), c, f);
				}
				else if (cPhi == phi - Integer.MIN_VALUE) {
					y += f * c.y();
					addRho(-f * c.rho(), c, f);
				}
				else
					super.add(vec, f);
			}
		}
		else {
			if (vec.x() == 0 && vec.z() == 0)
				y += f * vec.y();
			else
				super.add(vec, f);
		}
	}
	@Override public void add(Vec3d vec) {
		add(vec, 1);
	}
	@Override public void subtract(Vec3d vec, double f) {
		add(vec, -f);
	}
	@Override public void subtract(Vec3d vec) {
		add(vec, -1);
	}
	
	@Override public void multiplyValues(double f) {
		y *= f;
		multiplyRho(f);
	}
	@Override public void multiplyHorz(double f) {
		multiplyRho(f);
	}
	
	
	// minecraft
	
	@Override public NBTTagCompound writeToNBT() {
		NBTTagCompound tags = super.writeToNBT();
		tags.setDouble("y", y);
		tags.setDouble("rho", rho);
		tags.setInteger("phi", phi);
		return tags;
	}
	
	@Override public void readFromNBT(NBTTagCompound tags) {
		y = tags.getDouble("y");
		rho = tags.getDouble("rho");
		phi = tags.getInteger("phi");
		normalize();
	}

}
