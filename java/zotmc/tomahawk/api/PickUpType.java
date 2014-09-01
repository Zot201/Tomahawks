package zotmc.tomahawk.api;

public enum PickUpType {
	SURVIVAL,
	CREATIVE,
	ENCH;
	
	public boolean canBePickedUpBy(PickUpType type) {
		switch (type) {
		case SURVIVAL:
			return this == SURVIVAL;
		case CREATIVE:
			return this != ENCH;
		case ENCH:
			return this != CREATIVE;
		}
		return false;
	}
	
}
