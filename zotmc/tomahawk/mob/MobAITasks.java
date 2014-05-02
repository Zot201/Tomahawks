package zotmc.tomahawk.mob;

import static zotmc.tomahawk.mob.EventListener.MOB_AI_TASKS;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import zotmc.tomahawk.TomahawkRegistry;

public class MobAITasks implements IExtendedEntityProperties {
	
	EntityAITasks tasks;
	
	@Override public void init(Entity entity, World world) {
		tasks = TomahawkRegistry.getThrowerAITasks(entity);
		
	}
	
	@Override public void saveNBTData(NBTTagCompound compound) { }
	
	@Override public void loadNBTData(NBTTagCompound compound) { }
	
	
	
	public static MobAITasks getTasks(EntityLiving living) {
		return (MobAITasks) living.getExtendedProperties(MOB_AI_TASKS);
	}

	public static void onUpdate(EntityLiving living) {
		getTasks(living).onUpdate();
	}
	public void onUpdate() {
		tasks.onUpdateTasks();
	}
	

}
