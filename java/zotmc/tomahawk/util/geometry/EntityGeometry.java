package zotmc.tomahawk.util.geometry;

import zotmc.tomahawk.util.geometry.Angle.Unit;
import net.minecraft.entity.Entity;

public class EntityGeometry {
	
	public static Vec3d getPos(final Entity entity) {
		return new Vec3d() {
			@Override public double x() {
				return entity.posX;
			}
			@Override public double y() {
				return entity.posY;
			}
			@Override public double z() {
				return entity.posZ;
			}
			
			@Override public void setX(double x) {
				entity.posX = x;
			}
			@Override public void setY(double y) {
				entity.posY = y;
			}
			@Override public void setZ(double z) {
				entity.posZ = z;
			}
		};
	}
	
	public static Vec3d getMotion(final Entity entity) {
		return new Vec3d() {
			@Override public double x() {
				return entity.motionX;
			}
			@Override public double y() {
				return entity.motionY;
			}
			@Override public double z() {
				return entity.motionZ;
			}
			
			@Override public void setX(double x) {
				entity.motionX = x;
			}
			@Override public void setY(double y) {
				entity.motionY = y;
			}
			@Override public void setZ(double z) {
				entity.motionZ = z;
			}
		};
	}
	
	public static Angle getRotationYaw(final Entity entity) {
		return new AbsNormalizedAngle(Unit.DEGREE) {
			@Override protected void setValue(float value) {
				entity.rotationYaw = value;
			}
			@Override protected float getValue() {
				return entity.rotationYaw;
			}
		};
	}

}
