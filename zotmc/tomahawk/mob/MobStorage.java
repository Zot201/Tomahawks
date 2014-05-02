package zotmc.tomahawk.mob;

import static zotmc.tomahawk.mob.EventListener.MOB_STORAGE;

import java.util.Deque;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import zotmc.tomahawk.Tomahawk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;

public class MobStorage implements IExtendedEntityProperties {
	
	public static final String DROP_MODE = Tomahawk.MODID + ".dropMode";
	public static final byte NO_DROP = 0, DAMAGING_DROP = 1, DROP_UNCHANGED = 2;

	private Deque<ItemStack> queue;
	private int persistenceRequired;
	
	@Override public void init(Entity entity, World world) { }
	
	private Deque<ItemStack> queue() {
		return queue != null ? queue : (queue = Queues.newArrayDeque());
	}
	
	@Override public void saveNBTData(NBTTagCompound tags) {
		if (queue == null)
			return;
		
		tags = EventListener.getNestedTagCompound(tags, MOB_STORAGE);
		
		
		NBTTagList queueList = new NBTTagList();
		for (ItemStack item : queue())
			queueList.appendTag(item.writeToNBT(new NBTTagCompound()));
		tags.setTag("queue", queueList);
		
		tags.setByte("persistenceRequired", (byte) persistenceRequired);
		
	}

	@Override public void loadNBTData(NBTTagCompound tags) {
		tags = EventListener.getNestedTagCompound(tags, MOB_STORAGE);
		
		
		NBTTagList queueList = tags.getTagList("queue", 10);
		for (int i = 0; i < queueList.tagCount(); i++)
			queue().add(ItemStack.loadItemStackFromNBT(queueList.getCompoundTagAt(i)));
		
		persistenceRequired = tags.getByte("persistenceRequired");
		
	}
	
	
	
	public ItemStack pollLast() {
		ItemStack ret = queue().pollLast();

		if (ret.stackTagCompound != null
				&& ret.stackTagCompound.getByte(DROP_MODE) == DROP_UNCHANGED)
			persistenceRequired--;
		
		return ret;
	}
	
	public Iterable<ItemStack> clear() {
		if (queue != null) {
			persistenceRequired = 0;
			
			Iterable<ItemStack> ret = queue;
			queue = null;
			return ret;
		}
		
		return ImmutableList.of();
	}
	
	
	
	public static MobStorage getStorage(EntityLivingBase living) {
		return (MobStorage) living.getExtendedProperties(MOB_STORAGE);
	}
	
	public static boolean store(EntityLivingBase living, ItemStack item) {
		return getStorage(living).store(item);
	}
	public boolean store(ItemStack item) {
		if (size() < 18) {
			queue().add(item);
			
			if (item.stackTagCompound != null
					&& item.stackTagCompound.getByte(DROP_MODE) == DROP_UNCHANGED)
				persistenceRequired++;
			
			return true;
		}
		return false;
	}
	
	public static int size(EntityLivingBase living) {
		return getStorage(living).size();
	}
	public int size() {
		return queue != null ? queue.size() : 0;
	}
	
	public static boolean isPersistenceRequired(EntityLivingBase living) {
		return getStorage(living).isPersistenceRequired();
	}
	public boolean isPersistenceRequired() {
		return persistenceRequired > 0;
	}

	
}
