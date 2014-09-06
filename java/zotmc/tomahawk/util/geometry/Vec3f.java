package zotmc.tomahawk.util.geometry;

public abstract class Vec3f {
	
	Vec3f() { }
	
	
	// getters
	
	public abstract float x();
	public abstract float y();
	public abstract float z();
	
	
	// setters
	
	public abstract void setX(float x);
	public abstract void setY(float y);
	public abstract void setZ(float z);
	
	public void setValues(float x, float y, float z) {
		setX(x);
		setY(y);
		setZ(z);
	}

}
