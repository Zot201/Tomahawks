package zotmc.tomahawk;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PositionTracker implements IExtendedEntityProperties {
	
	private static final int X = 0, Y = 1, Z = 2;
	private static final String KEY = Tomahawk.MODID + ".postionTracker";
	
	private final EntityPlayer player;
	private double[] p1, p2;
	
	public PositionTracker(EntityPlayer player) {
		this.player = player;
	}
	
	public static PositionTracker get(EntityPlayer player) {
		return (PositionTracker) player.getExtendedProperties(KEY);
	}
	
	public void register() {
		player.registerExtendedProperties(KEY, this);
	}
	
	private static NBTTagCompound getCompound(NBTTagCompound tags) {
		if (!tags.hasKey(KEY)) {
			NBTTagCompound ret = new NBTTagCompound();
			tags.setTag(KEY, ret);
			return ret;
		}
		return tags.getCompoundTag(KEY);
	}
	
	@Override public void saveNBTData(NBTTagCompound tags) {
		tags = getCompound(tags);
		if (p1 != null) {
			tags.setDouble("p1x", p1[X]);
			tags.setDouble("p1y", p1[Y]);
			tags.setDouble("p1z", p1[Z]);
			tags.setDouble("p2x", p2[X]);
			tags.setDouble("p2y", p2[Y]);
			tags.setDouble("p2z", p2[Z]);
		}
	}
	
	@Override public void loadNBTData(NBTTagCompound tags) {
		tags = getCompound(tags);
		if (tags.hasKey("p1x")) {
			p1 = new double[] {
					tags.getDouble("p1x"),
					tags.getDouble("p1y"),
					tags.getDouble("p1z")};
			p2 = new double[] {
					tags.getDouble("p2x"),
					tags.getDouble("p2y"),
					tags.getDouble("p2z")};
		}
	}
	
	@Override public void init(Entity entity, World world) { }
	
	
	public void onUpdate() {
		if (p1 == null) {
			setCurrentPosition(p1 = new double[3]);
			setCurrentPosition(p2 = new double[3]);
			return;
		}
		
		p2[X] = p1[X];
		p2[Y] = p1[Y];
		p2[Z] = p1[Z];
		
		setCurrentPosition(p1);
		
	}
	
	public double[] getCurrentMotion() {
		return new double[] {
				func(player.posX, p1[X], p2[X]),
				func(player.posY, p1[Y], p2[Y]),
				func(player.posZ, p1[Z], p2[Z])};
	}
	
	private static double func(double p0, double p1, double p2) {
		return 2 * p0 - 3 * p1 + p2;
	}
	
	private void setCurrentPosition(double[] p) {
		p[X] = player.posX;
		p[Y] = player.posY;
		p[Z] = player.posZ;
	}

}
