package zotmc.tomahawk.util.geometry;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.FormattableFlags.ALTERNATE;
import static java.util.FormattableFlags.LEFT_JUSTIFY;
import static zotmc.tomahawk.util.Utils.PI;
import static zotmc.tomahawk.util.geometry.Angle.Unit.DEGREE;
import static zotmc.tomahawk.util.geometry.Angle.Unit.RADIAN;

import java.util.Formattable;
import java.util.Formatter;

import net.minecraft.util.MathHelper;

public abstract class Angle implements Formattable {
	
	public enum Unit {
		DEGREE {
			@Override String getSymbol() {
				return "°";
			}
			@Override protected float convert(float ang) {
				return ang * (PI / 180);
			}
		},
		RADIAN {
			@Override String getSymbol() {
				return "";
			}
			@Override protected float convert(float ang) {
				return ang * (180 / PI);
			}
		};
		
		abstract String getSymbol();
		protected abstract float convert(float ang);
		public float to(Unit unit, float ang) {
			return checkNotNull(unit) == this ? ang : convert(ang);
		}
	}
	
	
	
	// getters

	public abstract float getAngle(Unit unit);
	
	public float toDegrees() {
		return getAngle(DEGREE);
	}
	public float toRadians() {
		return getAngle(RADIAN);
	}
	
	
	// setters
	
	public abstract void setAngle(Unit unit, float ang);
	
	public void setDegrees(float deg) {
		setAngle(DEGREE, deg);
	}
	public void setRadians(float rad) {
		setAngle(RADIAN, rad);
	}
	
	
	// arithmetic
	
	public void addDegrees(float deg) {
		setDegrees(toDegrees() + deg);
	}
	public void addRadians(float rad) {
		setRadians(toRadians() + rad);
	}
	
	
	// trigonometry
	
	public float sin() {
		return MathHelper.sin(getAngle(RADIAN));
	}
	public float cos() {
		return MathHelper.cos(getAngle(RADIAN));
	}
	
	
	// string
	
	@Override public void formatTo(Formatter formatter, int flags, int width, int precision) {
		Unit unit = (flags & ALTERNATE) != 0 ? DEGREE : RADIAN;
		
		String f = (flags & LEFT_JUSTIFY) != 0 ? "-" : "";
		String w = width == -1 ? "" : Integer.toString(width);
		String p = precision == -1 ? "" : "." + precision;
		
		formatter.format("%" + f + w + p + "f%s", getAngle(unit), unit.getSymbol());
	}
	
	@Override public String toString() {
		return Float.toString(toRadians());
	}
	
}
