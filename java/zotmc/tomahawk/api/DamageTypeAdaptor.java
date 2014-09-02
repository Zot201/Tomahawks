package zotmc.tomahawk.api;

import static cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST;

import java.lang.reflect.Field;
import java.util.Set;

import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import zotmc.tomahawk.data.ReflData;
import zotmc.tomahawk.projectile.DamageSourceTomahawk;
import zotmc.tomahawk.util.Fields;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.ASMEventHandler;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.IEventListener;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * An {@link EventBus} that adapt {@link DamageSource#damageType} from {@code "thrown"} to {@code "player"}
 * for a set of selected event types, in order to allow the use of code that require
 * {@code "player"} damageType to proceed.<br/>
 * <br/>
 * In order to subscribe to adapted events, you may either 1) register your subscriber instances
 * through {@link #registerDirectly} or 2) tell DamageTypeAdaptor to post adapted event to subscribers in
 * {@link MinecraftForge#EVENT_BUS} through {@link #delegateByModid} / {@link #delegateByNamePattern}.<br/>
 * <br/>
 * Currently, {@link LivingAttackEvent}, {@link LivingHurtEvent} and {@link LivingDropsEvent} is supported.<br/>
 * <br/>
 * Please note that {@link #delegateByModid} is not available until MC 1.7.2.
 */
public class DamageTypeAdaptor extends EventBus {
	
	private static DamageTypeAdaptor instance;
	private final int parentId = Fields.get(MinecraftForge.EVENT_BUS, ReflData.BUS_ID);
	private final Set<ModContainer> modContainers = Sets.newIdentityHashSet();
	private final Set<Predicate<CharSequence>> namePatterns = Sets.newHashSet();
	private final Predicate<CharSequence> namePredicate = Predicates.or(namePatterns);
	
	private DamageTypeAdaptor() { }
	
	public static DamageTypeAdaptor instance() {
		if (instance == null)
			MinecraftForge.EVENT_BUS.register(instance = new DamageTypeAdaptor());
		return instance;
	}
	
	@Deprecated @Override public void register(Object target) {
		registerDirectly(target);
	}
	public void registerDirectly(Object object) {
		super.register(object);
	}
	@Override public void unregister(Object object) {
		super.unregister(object);
	}
	
	public void delegateByModid(String modid) {
		if (!ReflData.OWNER.isPresent())
			throw new UnsupportedOperationException();
		
		modContainers.add(Loader.instance().getIndexedModList().get(modid));
	}
	public void undelegatedModid(String modid) {
		modContainers.remove(Loader.instance().getIndexedModList().get(modid));
	}
	
	public void delegateByNamePattern(String namePattern) {
		namePatterns.add(Predicates.containsPattern(namePattern));
	}
	public void undelegateNamePattern(String namePattern) {
		namePatterns.remove(Predicates.containsPattern(namePattern));
	}
	
	
	
	@Cancelable
	public static class AdaptedLivingAttackEvent extends LivingAttackEvent {
		public AdaptedLivingAttackEvent(WorldServer world, DamageSourceTomahawk source, LivingAttackEvent original) {
			super(original.entityLiving, source.adaptDamageType(world), original.ammount);
		}
	}
	
	@Cancelable
	public static class AdaptedLivingHurtEvent extends LivingHurtEvent {
		public AdaptedLivingHurtEvent(WorldServer world, DamageSourceTomahawk source, LivingHurtEvent original) {
			super(original.entityLiving, source.adaptDamageType(world), original.ammount);
		}
	}
	
	@Cancelable
	public static class AdaptedLivingDropsEvent extends LivingDropsEvent {
		public AdaptedLivingDropsEvent(WorldServer world, DamageSourceTomahawk source, LivingDropsEvent original) {
			super(original.entityLiving, source.adaptDamageType(world), original.drops, original.lootingLevel,
					original.recentlyHit, original.specialDropValue);
		}
	}
	
	
	
	@Override public boolean post(Event event) {
		super.post(event);
		
		for (IEventListener listener : event.getListenerList().getListeners(parentId))
			if (listener instanceof ASMEventHandler) {
				ASMEventHandler a = (ASMEventHandler) listener;
				
				boolean isTarget;
				try {
					isTarget = ReflData.OWNER.isPresent()
							&& modContainers.contains(ReflData.OWNER.get().get(a));
					
					if (!isTarget && !namePatterns.isEmpty()) {
						IEventListener handler = (IEventListener) ReflData.HANDLER.get(a);
						Field f = handler.getClass().getDeclaredField(ReflData.INSTANCE);
						isTarget = namePredicate.apply(f.get(handler).getClass().getName());
					}
				} catch (Throwable e) {
					throw Throwables.propagate(e);
				}
				
				if (isTarget)
					a.invoke(event);
			}
		
		return event.isCancelable() && event.isCanceled();
	}
	
	@SubscribeEvent(priority = HIGHEST)
	public void onLivingAttack(LivingAttackEvent original) {
		if (original.entityLiving.worldObj instanceof WorldServer
				&& original.source instanceof DamageSourceTomahawk) {
			
			AdaptedLivingAttackEvent adapted = new AdaptedLivingAttackEvent(
					(WorldServer) original.entityLiving.worldObj,
					(DamageSourceTomahawk) original.source,
					original
			);
			
			if (post(adapted) && original.isCancelable())
				original.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	public void onLivingHurt(LivingHurtEvent original) {
		if (original.entityLiving.worldObj instanceof WorldServer
				&& original.source instanceof DamageSourceTomahawk) {
			
			AdaptedLivingHurtEvent adapted = new AdaptedLivingHurtEvent(
					(WorldServer) original.entityLiving.worldObj,
					(DamageSourceTomahawk) original.source,
					original
			);
			
			if (post(adapted) && original.isCancelable())
				original.setCanceled(true);
			
			original.ammount = adapted.ammount;
		}
	}
	
	@SubscribeEvent(priority = HIGHEST)
	public void onLivingDrops(LivingDropsEvent original) {
		if (original.entityLiving.worldObj instanceof WorldServer
				&& original.source instanceof DamageSourceTomahawk) {
			
			AdaptedLivingDropsEvent adapted = new AdaptedLivingDropsEvent(
					(WorldServer) original.entityLiving.worldObj,
					(DamageSourceTomahawk) original.source,
					original
			);
			
			if (post(adapted) && original.isCancelable())
				original.setCanceled(true);
		}
	}
	
}
