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

public class PlayerTracker implements IExtendedEntityProperties {
	
	private static final String KEY = AxeTomahawk.MODID + ".PlayerTracker";
	
	private final EntityPlayer player;
	private final Vec3d currentPosition, lastPosition;
	private int afterInteract = -1;
	
	PlayerTracker(EntityPlayer player) {
		this.player = player;
		currentPosition = EntityGeometry.getPos(player);
		lastPosition = new CartesianVec3d(currentPosition);
	}
	
	public static PlayerTracker get(EntityPlayer player) {
		return (PlayerTracker) player.getExtendedProperties(KEY);
	}
	
	void register() {
		player.registerExtendedProperties(KEY, this);
	}
	
	private static NBTTagCompound getTags(NBTTagCompound tags) {
		if (!tags.hasKey(KEY)) {
			NBTTagCompound ret = new NBTTagCompound();
			tags.setTag(KEY, ret);
			return ret;
		}
		return tags.getCompoundTag(KEY);
	}
	
	@Override public void saveNBTData(NBTTagCompound tags) {
		tags = getTags(tags);
		tags.setTag("lastPosition", lastPosition.writeToNBT());
		tags.setInteger("afterInteract", afterInteract);
	}
	
	@Override public void loadNBTData(NBTTagCompound tags) {
		tags = getTags(tags);
		lastPosition.readFromNBT(tags.getCompoundTag("lastPosition"));
		afterInteract = tags.getInteger("afterInteract");
	}
	
	@Override public void init(Entity entity, World world) { }
	
	
	void onUpdate() {
		lastPosition.setValues(currentPosition);
		
		if (afterInteract >= 0)
			afterInteract++;
	}
	
	public void onInteract() {
		afterInteract = 0;
	}
	public int getAfterInteract() {
		return afterInteract;
	}
	
	public Vec3d getLastMotion() {
		return new CartesianVec3d(
				currentPosition.x() - lastPosition.x(),
				currentPosition.y() - lastPosition.y(),
				currentPosition.z() - lastPosition.z()
		);
	}

}
