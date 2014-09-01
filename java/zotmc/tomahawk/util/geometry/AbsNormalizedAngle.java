package zotmc.tomahawk.util.geometry;

import static zotmc.tomahawk.util.Utils.PI;

public abstract class AbsNormalizedAngle extends Angle {
	
	final Unit unit;
	
	public AbsNormalizedAngle(Unit unit) {
		this.unit = unit;
	}
	
	
	// getters
	
	protected abstract float getValue();
	
	@Override public float getAngle(Unit unit) {
		return this.unit.to(unit, getValue());
	}
	
	
	// setters
	
	protected abstract void setValue(float value);
	
	@Override public void setAngle(Unit unit, float ang) {
		switch (unit) {
		case DEGREE:
			ang %= 360;
			if (ang < -180)
				ang += 360;
			else if (ang >= 180)
				ang -= 360;
			break;
			
		case RADIAN:
			ang %= 2 * PI;
			if (ang < -PI)
				ang += 2 * PI;
			else if (ang >= PI)
				ang -= 2 * PI;
			break;
		}
		
		setValue(unit.to(this.unit, ang));
	}

}
