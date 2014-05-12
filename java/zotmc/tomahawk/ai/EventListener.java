package zotmc.tomahawk.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventListener {
	
	@SubscribeEvent public void onLivingUpdate(LivingUpdateEvent event) {
		if (!event.entityLiving.worldObj.isRemote
				&& event.entityLiving.isEntityAlive()
				&& event.entityLiving instanceof EntityLiving)
			LivingAITasks.getTasks((EntityLiving) event.entityLiving).onUpdate();
		
	}
	
	@SubscribeEvent public void onEntityConstruct(EntityConstructing event) {
		if (event.entity.worldObj != null
				&& !event.entity.worldObj.isRemote
				&& event.entity instanceof EntityLiving)
			new LivingAITasks().register((EntityLiving) event.entity);
		
	}

}
