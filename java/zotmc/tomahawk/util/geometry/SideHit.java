package zotmc.tomahawk.util.geometry;

public class SideHit extends Vec3i {
	
	public static final int DOWN = 0, UP = 1, NORTH = 2, SOUTH = 3, WEST = 4, EAST = 5;

	private final static SideHit[] VALUES = new SideHit[] {
		new SideHit("DOWN" , DOWN ,  0, -1,  0),
		new SideHit("UP"   , UP   ,  0, +1,  0),
		new SideHit("NORTH", NORTH,  0,  0, -1),
		new SideHit("SOUTH", SOUTH,  0,  0, +1),
		new SideHit("WEST" , WEST , -1,  0,  0),
		new SideHit("EAST" , EAST , +1,  0,  0)
	};
	
	public static SideHit of(int side) {
		return VALUES[side];
	}
	
	
	private final String name;
	private final int ordinal, x, y, z;
	
	private SideHit(String name, int ordinal, int x, int y, int z) {
		this.name = name;
		this.ordinal = ordinal;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public String name() {
		return name;
	}
	public int ordinal() {
		return ordinal;
	}

	@Override public int x() {
		return x;
	}
	@Override public int y() {
		return y;
	}
	@Override public int z() {
		return z;
	}

	@Override public void setX(int x) {
		throw new UnsupportedOperationException();
	}
	@Override public void setY(int y) {
		throw new UnsupportedOperationException();
	}
	@Override public void setZ(int z) {
		throw new UnsupportedOperationException();
	}

}
