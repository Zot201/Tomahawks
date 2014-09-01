package zotmc.tomahawk.util.geometry;

public class NormalizedAngle extends AbsNormalizedAngle {
	
	public static NormalizedAngle create(Angle ang) {
		Unit unit = !(ang instanceof AbsNormalizedAngle) ? Unit.DEGREE
				: ((AbsNormalizedAngle) ang).unit;
		return new NormalizedAngle(unit, ang.getAngle(unit));
	}
	
	public static NormalizedAngle createFromYawNegative(Vec3d vec) {
		return new NormalizedAngle(Unit.RADIAN, -vec.yaw());
	}
	
	
	private float value;
	
	private NormalizedAngle(Unit unit, float value) {
		super(unit);
		this.value = value;
	}
	
	
	// getters
	
	@Override protected float getValue() {
		return value;
	}
	
	@Override protected void setValue(float value) {
		this.value = value;
	}

}
