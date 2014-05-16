package zotmc.tomahawk;

import static cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import zotmc.tomahawk.projectile.TomahawkDamage;
import zotmc.tomahawk.util.ListenerArrayList;
import zotmc.tomahawk.util.Refls;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.ASMEventHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class DamageFaker extends EventBus {
	
	private final Predicate<ASMEventHandler> isTarget = new Predicate<ASMEventHandler>() {
		final Field OWNER = Refls.getDeclaredField(ASMEventHandler.class, "owner");
		
		@Override public boolean apply(ASMEventHandler input) {
			return TomahawkRegistry.fakeDamages(Refls.<ModContainer>get(OWNER, input));
		}
	};
	
	private final Iterable<ASMEventHandler>
	livingAttackListeners = getListeners(LivingAttackEvent.class),
	livingHurtListeners = getListeners(LivingHurtEvent.class);
	
	private Iterable<ASMEventHandler> getListeners(Class<? extends Event> clz) {
		try {
			Constructor<? extends Event> ctr = clz.getConstructor();
			ctr.setAccessible(true);
			Event event = (Event) ctr.newInstance();
			
			return FluentIterable
					.from(new ListenerArrayList(event, MinecraftForge.EVENT_BUS))
					.filter(ASMEventHandler.class)
					.filter(isTarget);
			
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	public void onLivingAttack(LivingAttackEvent event) {
		if (event.entityLiving.worldObj instanceof WorldServer
				&& event.source.getClass() == TomahawkDamage.class) {
			
			LivingAttackEvent fakingEvent = new LivingAttackEvent(
					event.entityLiving,
					((TomahawkDamage) event.source)
						.faking((WorldServer) event.entityLiving.worldObj),
					event.ammount);
			
			for (IEventListener listener : livingAttackListeners)
				listener.invoke(fakingEvent);
			
		}
		
	}
	
	@SubscribeEvent(priority = HIGHEST)
	public void onLivingHurt(LivingHurtEvent event) {
		if (event.entityLiving.worldObj instanceof WorldServer
				&& event.source.getClass() == TomahawkDamage.class) {
			
			LivingHurtEvent fakingEvent = new LivingHurtEvent(
					event.entityLiving,
					((TomahawkDamage) event.source)
						.faking((WorldServer) event.entityLiving.worldObj),
					event.ammount);
			
			for (IEventListener listener : livingHurtListeners)
				listener.invoke(fakingEvent);
			
			event.ammount = fakingEvent.ammount;
			
		}
		
	}
	
	@SubscribeEvent(priority = HIGHEST)
	public void onLivingDrops(LivingDropsEvent event) {
		if (event.entityLiving.worldObj instanceof WorldServer
				&& event.source.getClass() == TomahawkDamage.class)
			post(new LivingDropsEvent(
					event.entityLiving,
					((TomahawkDamage) event.source)
						.faking((WorldServer) event.entityLiving.worldObj),
					event.drops,
					event.lootingLevel,
					event.recentlyHit,
					event.specialDropValue));
		
	}
	
}
