package zotmc.tomahawk.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static zotmc.tomahawk.core.LogTomahawk.api4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.api.Launchable.Category;
import zotmc.tomahawk.api.Launchable.Usage;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Joiner;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;

public class TomahawkRegistry {
	
	private static final Method LAUNCHABLE_VALUE = Utils.getDeclaredMethod(Launchable.class, "value");
	
	private static final Table<Class<?>, Class<? extends Annotation>, Delegation> annotatedHandlers = HashBasedTable.create();
	private static final Map<Class<?>, ItemHandler> genericHandlers = Maps.newIdentityHashMap();
	private static final Map<Class<? extends Item>, ItemHandler> computedHandlers = Maps.newIdentityHashMap();
	
	
	public static void registerItemHandler(Class<?> itemType, Object handler) {
		if (Loader.instance().hasReachedState(LoaderState.AVAILABLE))
			throw new IllegalStateException();
		
		if (!genericHandlers.containsKey(itemType)) {
			if (handler instanceof ItemHandler)
				genericHandlers.put(itemType, (ItemHandler) handler);
			else {
				Map<Class<? extends Annotation>, Delegation> row = annotatedHandlers.row(itemType);
				row.putAll(mapDelegations(handler, Sets.difference(ItemHandler.ANNOTATION_MAP.keySet(), row.keySet())));
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
				
				ItemHandler baseHandler = null;
				Map<Class<? extends Annotation>, Delegation> delegations = Maps.newIdentityHashMap();
				
				Map<Class<? extends Annotation>, Delegation> annotatedMethods = mapDelegations(
						item, Sets.difference(ItemHandler.ANNOTATION_MAP.keySet(), ImmutableSet.of(Category.class))
				);
				
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
					
					Iterator<Entry<Class<? extends Annotation>, Delegation>> itr = annotatedMethods.entrySet().iterator();
					while (itr.hasNext()) {
						Entry<Class<? extends Annotation>, Delegation> entry = itr.next();
						if (c.isAssignableFrom(entry.getValue().method.getDeclaringClass())) {
							if (!delegations.containsKey(entry.getKey()))
								delegations.put(entry.getKey(), entry.getValue());
							itr.remove();
						}
					}
				}
				
				if (baseHandler == null) {
					Delegation d = delegations.get(Category.class);
					baseHandler = d == null ? WeaponCategory.DISABLED
							: checkNotNull((ItemHandler) d.invoke(), "Null return value is not allowed, %s", d.method);
				}
				
				ItemHandler handler = makeHandler(baseHandler, delegations);
				computedHandlers.put(item.getClass(), handler);
			}
		
		api4j().debug("Computed Handlers:\n" + Joiner.on('\n').join(computedHandlers.entrySet()));
	}
	
	private static Map<Class<? extends Annotation>, Delegation> mapDelegations(Object delegatee,
			Set<Class<? extends Annotation>> targetAnnotations) {
		
		Map<Class<? extends Annotation>, Delegation> ret = Maps.newIdentityHashMap();
		for (Entry<Method, Annotation> entry : Utils.getAnnotatedMethods(delegatee.getClass())) {
			Class<? extends Annotation> type = entry.getValue().annotationType();
			if (!ret.containsKey(type) && targetAnnotations.contains(type))
				ret.put(type, new Delegation(delegatee,
						checkMethod(entry.getKey(), type.getAnnotation(Usage.class).desc())
				));
		}
		
		/*if (!ret.isEmpty())
			api4j().debug("Mapped Delegations:\n" + Joiner.on('\n').join(ret.entrySet()));*/
		return ret;
	}
	
	private static Method checkMethod(Method target, Class<?>[] requiredDesc) {
		checkArgument(!Modifier.isStatic(target.getModifiers()), "Static method is not allowed, %s", target);
		
		checkArgument(requiredDesc[0].isAssignableFrom(target.getReturnType()), "Return type incompatible, %s", target);
		Class<?>[] parameterTypes = target.getParameterTypes();
		checkArgument(parameterTypes.length == requiredDesc.length - 1);
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
