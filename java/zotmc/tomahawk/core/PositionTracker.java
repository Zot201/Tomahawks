package zotmc.tomahawk.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import zotmc.tomahawk.data.ModData.AxeTomahawk;
import zotmc.tomahawk.util.geometry.CartesianVec3d;
import zotmc.tomahawk.util.geometry.EntityGeometry;
import zotmc.tomahawk.util.geometry.Vec3d;

public class PositionTracker implements IExtendedEntityProperties {
	
	private static final String KEY = AxeTomahawk.MODID + ".PostionTracker";
	
	private final EntityPlayer player;
	private final Vec3d p0, p1;
	
	PositionTracker(EntityPlayer player) {
		this.player = player;
		p0 = EntityGeometry.getPos(player);
		p1 = new CartesianVec3d(p0);
	}
	
	public static PositionTracker get(EntityPlayer player) {
		return (PositionTracker) player.getExtendedProperties(KEY);
	}
	
	void register() {
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
		tags.setDouble("p1x", p1.x());
		tags.setDouble("p1y", p1.y());
		tags.setDouble("p1z", p1.z());
	}
	
	@Override public void loadNBTData(NBTTagCompound tags) {
		tags = getCompound(tags);
		if (tags.hasKey("p1x"))
			p1.setValues(
					tags.getDouble("p1x"),
					tags.getDouble("p1y"),
					tags.getDouble("p1z")
			);
	}
	
	@Override public void init(Entity entity, World world) { }
	
	
	void onUpdate() {
		p1.setValues(p0);
	}
	
	public Vec3d getCurrentMotion() {
		return new CartesianVec3d(p0.x() - p1.x(), p0.y() - p1.y(), p0.z() - p1.z());
	}

}
