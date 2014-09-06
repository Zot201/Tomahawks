package zotmc.tomahawk.util.geometry;

public class CartesianVec3f extends Vec3f {

	private float x, y, z;
	
	public CartesianVec3f() { }
	
	
	// getters
	
	@Override public float x() {
		return x;
	}
	@Override public float y() {
		return y;
	}
	@Override public float z() {
		return z;
	}
	
	
	// setters
	
	@Override public void setX(float x) {
		this.x = x;
	}
	@Override public void setY(float y) {
		this.y = y;
	}
	@Override public void setZ(float z) {
		this.z = z;
	}
	
}
