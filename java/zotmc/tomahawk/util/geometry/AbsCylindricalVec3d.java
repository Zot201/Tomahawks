package zotmc.tomahawk.util.geometry;

import zotmc.tomahawk.data.ReflData;
import zotmc.tomahawk.util.Fields;
import zotmc.tomahawk.util.Utils;

public abstract class AbsCylindricalVec3d extends Vec3d {
	
	public interface DelegationHandler {
		
		public double getY(double y);
		
		public double getRho(double rho);
		
		public int getPhi(int phi);
		
	}
	
	AbsCylindricalVec3d() { }
	
	
	// internal
	
	@Override protected Vec3d copy() {
		return new CylindricalVec3d(y(), rho(), phi());
	}
	
	private static final float[] SIN_TABLE = Fields.get(null, ReflData.SIN_TABLE);
	
	private static int upper16(int a) {
		return ((a & 0x1FFFF) == 0x8000 ? a : 0x8000 + a) >>> 16;
	}
	private static int upper31(int a) {
		return ((a & 3) == 1 ? a : 1 + a) >>> 1; 
	}
	
	static float sinInt(int a) {
		return SIN_TABLE[upper16(a)];
	}
	static float cosInt(int a) {
		return sinInt(a + Integer.MIN_VALUE / -2);
	}
	
	static int atan2Int(double y, double x) {
		return (int) Math.rint(Math.atan2(y, x) * (Integer.MIN_VALUE / -Math.PI));
	}
	static int att2Int(double y, double x, int theta) {
		if (y != 0 && (y == x || y == -x)) {
			if (y > 0)
				if (x > 0)
					return theta;
				else
					return Integer.MIN_VALUE - theta;
			else
				if (x > 0)
					return -theta;
				else
					return theta - Integer.MIN_VALUE;
		}
		return atan2Int(y * sinInt(theta), x * cosInt(theta));
	}
	
	
	// getters
	
	abstract double rho();
	
	abstract int phi();
	
	@Override public double x() {
		return rho() * sinInt(phi());
	}
	@Override public double z() {
		return rho() * cosInt(phi());
	}
	
	@Override public float yaw() {
		return phi() * (-Utils.PI / Integer.MIN_VALUE);
	}
	
	
	// vector operations
	
	@Override public float horz() {
		return (float) rho();
	}
	@Override public double horz2() {
		double rho = rho();
		return rho * rho;
	}
	
	@Override public double norm2() {
		double y = y(), rho = rho();
		return y * y + rho * rho;
	}
	
	@Override public double dot(Vec3d vec) {
		if (vec instanceof AbsCylindricalVec3d) {
			AbsCylindricalVec3d c = (AbsCylindricalVec3d) vec;
			return y() * c.y() + rho() * c.rho() * cosInt(c.phi() - phi());
		}
		else
			return super.dot(vec);
	}
	
	@Override public Vec3d cross(Vec3d vec) {
		if (vec instanceof AbsCylindricalVec3d) {
			AbsCylindricalVec3d that = (AbsCylindricalVec3d) vec;
			double
			rho1 = this.rho(), rho2 = that.rho(),
			a = rho1 * that.y(), b = rho2 * this.y();
			int
			phi1 = this.phi(), phi2 = that.phi(), diff = phi2 - phi1,
			beta = upper31(diff), alpha = phi1 + beta;
			
			return new CylindricalVec3d(
					rho1 * rho2 * sinInt(diff),
					Math.sqrt(a * a + b * b - 2 * a * b * cosInt(diff)),
					att2Int(a + b, b - a, beta) + alpha + Integer.MIN_VALUE / -2
			);
		}
		else
			return super.cross(vec);
	}
	@Override public Vec3d cross(Vec3d vec, double f) {
		Vec3d ret = cross(vec);
		ret.multiplyValues(f);
		return ret;
	}
	
	
	// derive
	
	public AbsCylindricalVec3d derive(final DelegationHandler h) {
		return new AbsCylindricalVec3d() {
			@Override public double y() {
				return h.getY(AbsCylindricalVec3d.this.y());
			}
			@Override double rho() {
				return h.getRho(AbsCylindricalVec3d.this.rho());
			}
			@Override int phi() {
				return h.getPhi(AbsCylindricalVec3d.this.phi());
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
	}
	
	
	// others
	
	@Override public boolean isNaN() {
		double y = y();
		if (y != y)
			return true;
		
		double rho = rho();
		if (rho != rho)
			return true;
		
		return false;
	}
	
}
