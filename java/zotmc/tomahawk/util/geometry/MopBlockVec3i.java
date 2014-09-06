package zotmc.tomahawk.util.geometry;

import net.minecraft.util.MovingObjectPosition;

public class MopBlockVec3i extends Vec3i {
	
	private final MovingObjectPosition mop;
	
	public MopBlockVec3i(MovingObjectPosition mop) {
		this.mop = mop;
	}

	@Override public int x() {
		return mop.blockX;
	}
	@Override public int y() {
		return mop.blockY;
	}
	@Override public int z() {
		return mop.blockZ;
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
