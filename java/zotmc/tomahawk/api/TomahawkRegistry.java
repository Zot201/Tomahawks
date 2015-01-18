package zotmc.tomahawk.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static zotmc.tomahawk.core.LogTomahawk.api4j;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Logger;

import zotmc.tomahawk.api.Launchable.Category;
import zotmc.tomahawk.api.Launchable.Usage;
import zotmc.tomahawk.core.TomahawksCore;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;

public class TomahawkRegistry {
	
	private static final Table<Class<?>, Class<? extends Annotation>, Delegation> annotatedHandlers = HashBasedTable.create();
	private static final Map<Class<?>, ItemHandler> genericHandlers = Maps.newIdentityHashMap();
	private static final Map<Class<? extends Item>, ItemHandler> computedHandlers = Maps.newIdentityHashMap();
	
	
	public synchronized static void registerItemHandler(Class<?> itemType, Object handler) {
		checkState(!Loader.instance().hasReachedState(LoaderState.AVAILABLE));
		checkNotNull(itemType);
		checkNotNull(handler);
		
		if (!genericHandlers.containsKey(itemType)) {
			if (handler instanceof ItemHandler)
				genericHandlers.put(itemType, (ItemHandler) handler);
			else {
				Map<Class<? extends Annotation>, Delegation>
				row = annotatedHandlers.row(itemType),
				reg = Maps.newIdentityHashMap();
				
				getDelegations(handler, row, reg, null);
				
				row.putAll(reg);
				TomahawksCore.instance.log.info("Associated %s with %s involving %d item handling%s: %s",
						itemType, handler, reg.size(), reg.size() == 1 ? "" : "s", toString(reg));
			}
		}
	}
	
	public static ItemHandler getItemHandler(ItemStack item) {
		return item != null ? getItemHandler(item.getItem()) : WeaponCategory.DISABLED;
	}
	
	public static ItemHandler getItemHandler(Item item) {
		ItemHandler ret = computedHandlers.get(item.getClass());
		return ret != null ? ret : WeaponCategory.DISABLED;
	}
	
	
	static void computeItemHandlers() {
		for (Item item : Utils.itemList())
			if (!computedHandlers.containsKey(item.getClass())) {
				
				ItemHandler baseHandler = WeaponCategory.DISABLED;
				Map<Class<? extends Annotation>, Delegation> delegations = Maps.newIdentityHashMap();
				
				for (Class<?> c : Utils.getTypes(item.getClass())) {
					ItemHandler computed = computedHandlers.get(c);
					if (computed != null) {
						baseHandler = computed;
						break;
					}
					
					ItemHandler registered = genericHandlers.get(c);
					if (registered != null) {
						baseHandler = registered;
						break;
					}
					
					for (Entry<Class<? extends Annotation>, Delegation> entry : annotatedHandlers.row(c).entrySet())
						if (!delegations.containsKey(entry.getKey()))
							delegations.put(entry.getKey(), entry.getValue());
					
					
					Launchable launchable = null;
					try {
						launchable = c.getAnnotation(Launchable.class);
					} catch (Throwable e) {
						Logger log = TomahawksCore.instance.log;
						log.error("Failed to get annotation for %s", c);
						
						if (e instanceof AnnotationFormatError)
							log.error("Encountered AnnotationFormatError. Stack trace is discarded.");
						else
							log.catching(e);
					}
					
					if (launchable != null) {
						ItemHandler value = Enums.getIfPresent(WeaponCategory.class, launchable.value()).orNull();
						
						if (value == null) {
							Object obj = null;
							
							try {
								Class<?> clz = Class.forName(launchable.value());
								checkArgument(!Modifier.isAbstract(clz.getModifiers()),
										"Launchable value is abstract and hence cannot be initiated: %s",
										launchable.value());
								
								Constructor<?> ctor = clz.getConstructor();
								ctor.setAccessible(true);
								obj = ctor.newInstance();
								
							} catch (ClassNotFoundException e) {
								throw new RuntimeException(
										"Unknown class in launchable value: " + launchable.value(), e);
								
							} catch (NoSuchMethodException e) {
								throw new RuntimeException(
										"No args constructor is required to initiate: " + launchable.value(), e);
								
							} catch (Throwable e) {
								Throwables.propagate(e);
							}
							
							
							if (obj instanceof ItemHandler)
								value = (ItemHandler) obj;
							else {
								value = WeaponCategory.DISABLED;
								
								Map<Class<? extends Annotation>, Delegation> r = Maps.newIdentityHashMap();
								Delegation d = getDelegations(obj, delegations, r, Category.class);
								if (d != null) {
									Object o = checkNotNull(d.invoke(), "Null return value is not allowed, %s", d.method);
									value = (ItemHandler) o;
								}
								
								delegations.putAll(r);
								api4j().debug("Associated %s with %s involving %d item handling%s: %s",
										c, obj, r.size(), r.size() == 1 ? "" : "s", toString(r));
							}
						}
						
						baseHandler = value;
						break;
					}
					
				}
				
				if (baseHandler == WeaponCategory.DISABLED) {
					Delegation d = delegations.get(Category.class);
					if (d != null) {
						Object o = checkNotNull(d.invoke(), "Null return value is not allowed, %s", d.method);
						baseHandler = (ItemHandler) o;
					}
				}
				
				if (!delegations.isEmpty()) {
					int i = delegations.size();
					api4j().debug("Found %d item handling%s under %s: %s",
							i, i == 1 ? "" : "s", item.getClass(), toString(delegations));
					api4j().debug("Building new item handler base on %s...", baseHandler);
				}
				
				ItemHandler handler = makeHandler(baseHandler, delegations);
				computedHandlers.put(item.getClass(), handler);
			}
		
		api4j().debug("Computed Handlers:\n" + Joiner.on('\n').join(computedHandlers.entrySet()));
	}
	
	private static String toString(Map<Class<? extends Annotation>, Delegation> map) {
		Iterable<?> entries = Utils.transformKeys(map, new Function<Class<?>, String>() {
			@Override public String apply(Class<?> input) {
				return input.getSimpleName();
			}
		});
		
		return Joiner.on(", ").appendTo(new StringBuilder("{"), entries).append("}").toString();
	}
	
	private static Delegation getDelegations(Object obj, Map<Class<? extends Annotation>, Delegation> existing,
			Map<Class<? extends Annotation>, Delegation> results, Class<? extends Annotation> exception) {
		
		Delegation ret = null;
		
		for (Class<?> c : Utils.getTypes(obj.getClass()))
			for (Entry<Method, Annotation> entry : Utils.getMethodAnnotations(c)) {
				Class<? extends Annotation> t = entry.getValue().annotationType();
				
				if (ItemHandler.ANNOTATION_MAP.containsKey(t)
						&& (t == exception ? ret == null : !existing.containsKey(t))) {
					Method m = checkMethod(entry.getKey(), t.getAnnotation(Usage.class).desc());
					
					if (t == exception)
						ret = new Delegation(obj, m);
					else
						results.put(t, new Delegation(obj, m));
				}
			}
		
		return ret;
	}
	
	private static Method checkMethod(Method target, Class<?>[] requiredDesc) {
		checkArgument(Modifier.isPublic(target.getModifiers()), "Only public method is allowed");
		checkArgument(!Modifier.isStatic(target.getModifiers()), "Static method is not allowed, %s", target);
		
		checkArgument(requiredDesc[0].isAssignableFrom(target.getReturnType()), "Return type incompatible, %s", target);
		Class<?>[] parameterTypes = target.getParameterTypes();
		checkArgument(parameterTypes.length == requiredDesc.length - 1, "Arguments length incompatible, %s", target);
		for (int i = 0; i < parameterTypes.length; i++)
			checkArgument(parameterTypes[i].isAssignableFrom(requiredDesc[i + 1]),
					"Argument type incompatible, %s", parameterTypes[i]);
		
		/*Class<?>[] exceptionTypes = target.getExceptionTypes();
		for (Class<?> c : exceptionTypes)
			checkArgument(!RuntimeException.class.isAssignableFrom(c) && !Error.class.isAssignableFrom(c),
					"Checked exception / error declaration is not allowed, %s", target);*/
		
		return target;
	}
	
	private static ItemHandler makeHandler(ItemHandler base, Map<Class<? extends Annotation>, Delegation> delegations) {
		return delegations.isEmpty() ? base : AsmItemHandlers.create(base, delegations);
	}
	
	static void sanityCheckHandlers() {
		for (ItemHandler handler : computedHandlers.values())
			checkNotNull(handler.category(), "Null category is not allowed, %s", handler);
	}
	
}
