package zotmc.tomahawk.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static zotmc.tomahawk.core.LogTomahawk.api4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.api.Launchable.Category;
import zotmc.tomahawk.api.Launchable.Usage;
import zotmc.tomahawk.core.TomahawksCore;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;

public class TomahawkRegistry {
	
	private static final Method LAUNCHABLE_VALUE = Utils.getDeclaredMethod(Launchable.class, "value");
	
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
				
				for (Class<?> c : Utils.getTypes(handler.getClass()))
					for (Entry<Method, Annotation> entry : Utils.getMethodAnnotations(c)) {
						Class<? extends Annotation> t = entry.getValue().annotationType();
						if (ItemHandler.ANNOTATION_MAP.containsKey(t) && !row.containsKey(t)) {
							Method m = checkMethod(entry.getKey(), t.getAnnotation(Usage.class).desc());
							reg.put(t, new Delegation(handler, m));
						}
					}
				
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
					
					Launchable launchable = c.getAnnotation(Launchable.class);
					if (launchable != null && !delegations.containsKey(Category.class))
						delegations.put(Category.class, new Delegation(launchable, LAUNCHABLE_VALUE));
					
					/*for (Entry<Method, Annotation> entry : Utils.getMethodAnnotations(c)) {
						Class<? extends Annotation> t = entry.getValue().annotationType();
						if (ItemHandler.ANNOTATION_MAP.containsKey(t) && t != Category.class && !delegations.containsKey(t)) {
							Method m = checkMethod(entry.getKey(), t.getAnnotation(Usage.class).desc());
							delegations.put(t, new Delegation(item, m));
						}
					}*/
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
	
	private static Method checkMethod(Method target, Class<?>[] requiredDesc) {
		checkArgument(Modifier.isPublic(target.getModifiers()), "Only public method is allowed");
		checkArgument(!Modifier.isStatic(target.getModifiers()), "Static method is not allowed, %s", target);
		
		checkArgument(requiredDesc[0].isAssignableFrom(target.getReturnType()), "Return type incompatible, %s", target);
		Class<?>[] parameterTypes = target.getParameterTypes();
		checkArgument(parameterTypes.length == requiredDesc.length - 1, "Arguments length incompatible, %s", target);
		for (int i = 0; i < parameterTypes.length; i++)
			checkArgument(parameterTypes[i].isAssignableFrom(requiredDesc[i + 1]),
					"Argument type incompatible, %s", parameterTypes[i]);
		
		Class<?>[] exceptionTypes = target.getExceptionTypes();
		for (Class<?> c : exceptionTypes)
			checkArgument(!RuntimeException.class.isAssignableFrom(c) && !Error.class.isAssignableFrom(c),
					"Checked exception / error declaration is not allowed, %s", target);
		
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
