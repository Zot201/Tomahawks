package zotmc.tomahawk.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import zotmc.tomahawk.Tomahawk;
import zotmc.tomahawk.TomahawkRegistry;

public class LivingAITasks implements IExtendedEntityProperties {
	
	private static final String LIVING_AI_TASKS = Tomahawk.MODID + ".livingAITasks";
	
	EntityAITasks tasks;
	
	
	public void register(EntityLiving living) {
		living.registerExtendedProperties(LIVING_AI_TASKS, this);
	}
	
	@Override public void init(Entity entity, World world) {
		tasks = TomahawkRegistry.getThrowerAITasks(entity);
		
	}
	
	public void onUpdate() {
		tasks.onUpdateTasks();
	}
	
	@Override public void saveNBTData(NBTTagCompound compound) { }
	
	@Override public void loadNBTData(NBTTagCompound compound) { }
	
	
	
	public static LivingAITasks getTasks(EntityLiving living) {
		return (LivingAITasks) living.getExtendedProperties(LIVING_AI_TASKS);
	}
	
}
