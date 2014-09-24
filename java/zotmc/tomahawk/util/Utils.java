package zotmc.tomahawk.util;

import static com.google.common.base.Preconditions.checkArgument;
import static zotmc.tomahawk.data.ReflData.EnchantmentHelpers.damageIterators;
import static zotmc.tomahawk.data.ReflData.EnchantmentHelpers.modifierLivings;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import org.objectweb.asm.Type;

import zotmc.tomahawk.data.ReflData.Bootstraps;
import zotmc.tomahawk.data.ReflData.EnchantmentHelpers;
import zotmc.tomahawk.util.geometry.CartesianVec3d;
import zotmc.tomahawk.util.geometry.Vec3d;
import zotmc.tomahawk.util.init.SimpleVersion;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.VersionParser;

public class Utils {
	
	// version sensitive methods
	
	public static GameProfile newGameProfile(UUID id, String name) {
		return GameProfileFactory.INSTANCE.newGameProfile(id, name);
	}
	private enum GameProfileFactory {
		INSTANCE;
		private final boolean useUUID;
		private final Constructor<GameProfile> constructor;
		private GameProfileFactory() {
			if (MC_VERSION.isAtLeast("1.7.10")) {
				constructor = getConstructor(GameProfile.class, UUID.class, String.class);
				useUUID = true;
			}
			else {
				constructor = getConstructor(GameProfile.class, String.class, String.class);
				useUUID = false;
			}
		}
		public GameProfile newGameProfile(UUID id, String name) {
			try {
				return useUUID ? constructor.newInstance(id, name)
						: constructor.newInstance(id == null ? "" : id.toString(), name);
			} catch (Throwable e) {
				throw Throwables.propagate(e);
			}
		}
	}
	
	public static float getEnchantmentModifierLiving(ItemStack item, EntityLivingBase victim) {
		if (!modifierLivings.isPresent())
			return EnchantmentHelper.func_152377_a(item, victim.getCreatureAttribute());
		else {
			modifierLivings.get().result.set(0.0F);
			modifierLivings.get().victim.set(victim);
			applyEnchantmentModifier(modifierLivings.get().instance, item);
			return modifierLivings.get().result.get();
		}
	}
	
	public static void applyEnchantmentDamageIterator(EntityLivingBase user, ItemStack item, Entity victim) {
		if (damageIterators.isPresent()) {
			damageIterators.get().user.set(user);
			damageIterators.get().victim.set(victim);
			applyEnchantmentModifier(damageIterators.get().instance, item);
		}
	}
	
	private static void applyEnchantmentModifier(Supplier<?> iModifier, ItemStack item) {
		try {
			EnchantmentHelpers.applyEnchantmentModifier.invoke(null, iModifier.get(), item);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	
	
	// minecraft and forge
	
	// prevent inlining
	public static final SimpleVersion MC_VERSION = new SimpleVersion(Fields.<String>get(null, Bootstraps.MC_VERSION));
	
	public static class EntityLists {
		@SuppressWarnings("unchecked")
		public static Map<String, Class<? extends Entity>> stringToClassMapping() {
			return EntityList.stringToClassMapping;
		}
	}
	
	public static ItemStack itemStack(ItemStack itemStack, int newSize) {
		ItemStack ret = new ItemStack(itemStack.getItem(), newSize, itemStack.getItemDamage());
		if (itemStack.stackTagCompound != null)
			ret.stackTagCompound = (NBTTagCompound) itemStack.stackTagCompound.copy();
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static Iterable<Item> itemList() {
		return GameData.getItemRegistry();
	}
	
	private static final Splitter COLON = Splitter.on(':').limit(2);
	public static String getModid(Item item) {
		return COLON.split(GameData.getItemRegistry().getNameForObject(item)).iterator().next();
	}
	
	public static Supplier<String> localize(String unlocalized) {
		return localize(unlocalized, new Object[0]);
	}
	public static Supplier<String> localize(final String unlocalized, Object... args) {
		final Object[] copy = args.clone();
		
		return new Supplier<String>() {
			@Override public String get() {
				return I18n.format(unlocalized, copy);
			}
			
			@Override public String toString() {
				return get();
			}
		};
	}
	
	public static int getEnchLevel(Enchantment enchantment, ItemStack item) {
		return EnchantmentHelper.getEnchantmentLevel(enchantment.effectId, item);
	}
	
	@SuppressWarnings("unchecked")
	public static Map<Integer, Integer> getEnchs(ItemStack item) {
		return EnchantmentHelper.getEnchantments(item);
	}
	
	public static Block getBlock(String name) {
		return GameData.getBlockRegistry().getObject(name);
	}
	
	public static String getNameForBlock(Block block) {
		return GameData.getBlockRegistry().getNameForObject(block);
	}
	
	
	
	// geometry
	
	public static Vec3 vec3(double x, double y, double z) {
		return Vec3.createVectorHelper(x, y, z);
	}
	public static Vec3 vec3(Vec3 vec) {
		return vec3(vec.xCoord, vec.yCoord, vec.zCoord);
	}
	
	public static Vec3 sumVec3(Vec3d a, Vec3d b) {
		return vec3(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
	}
	
	public static Vec3d nextUnitVec3d(Random rand) {
		while (true) {
			Vec3d ret = new CartesianVec3d(rand.nextGaussian(), rand.nextGaussian(), rand.nextGaussian());
			ret.multiplyValues(1 / ret.norm());
			if (ret.x() == ret.x() && ret.y() == ret.y() && ret.z() == ret.z())
				return ret;
		}
	}
	
	
	
	// maths
	
	public static final float PI = (float) Math.PI;
	
	public static float atan(double a) {
		return (float) Math.atan(a);
	}
	
	public static float atan2(double y, double x) {
		return (float) Math.atan2(y, x);
	}
	
	public static float sqrt(double a) {
		return (float) Math.sqrt(a);
	}
	
	public static int floor(double a) {
		return MathHelper.floor_double(a);
	}
	public static int floor(float a) {
		return MathHelper.floor_float(a);
	}
	
	public static int closed(int min, int max, int a) {
		return a < min ? min : a > max ? max : a;
	}
	public static float closed(float min, float max, float a) {
		return a < min ? min : a > max ? max : a;
	}
	
	
	
	// reflections
	
	public static Field getDeclaredField(Class<?> clz, String name) {
		try {
			Field ret = clz.getDeclaredField(name);
			ret.setAccessible(true);
			return ret;
		} catch (NoSuchFieldException e) {
			throw new UnknownFieldException(e);
		}
	}
	
	public static Optional<Field> getDeclaredFieldIf(boolean condition, Class<?> clz, String name) {
		return condition ? Optional.of(getDeclaredField(clz, name)) : Optional.<Field>absent();
	}
	
	public static Method getDeclaredMethod(Class<?> clz, String name, Class<?>... parameterTypes) {
		try {
			Method ret = clz.getDeclaredMethod(name, parameterTypes);
			ret.setAccessible(true);
			return ret;
		} catch (NoSuchMethodException e) {
			throw new UnknownMethodException(e);
		}
	}
	
	private static <T> Constructor<T> getConstructor(Class<T> clz, Class<?>... parameterTypes) {
		try {
			Constructor<T> ret = clz.getConstructor(parameterTypes);
			ret.setAccessible(true);
			return ret;
		} catch (NoSuchMethodException e) {
			throw new UnknownMethodException(e);
		}
	}
	
	public static <T> boolean invokeIfExists(T obj, Class<? super T> declaringClz, String methodName) {
		Method m = null;
		try {
			m = declaringClz.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException e) {
			return false;
		}
		
		if (m != null)
			try {
				m.setAccessible(true);
				m.invoke(obj);
			} catch (Throwable e) {
				throw Throwables.propagate(e);
			}
		
		return true;
	}
	
	public static void invokeDeclared(Class<?> clz, String methodName) {
		try {
			Method m = clz.getDeclaredMethod(methodName);
			m.setAccessible(true);
			m.invoke(null);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	public static <T> Optional<T> constructIf(boolean condition, Class<T> clz) {
		if (condition)
			try {
				Constructor<T> ctor = clz.getConstructor();
				ctor.setAccessible(true);
				return Optional.of(ctor.newInstance());
			} catch (Throwable e) {
				throw Throwables.propagate(e);
			}
		
		return Optional.absent();
	}
	
	public static Iterable<Class<?>> getTypes(final Class<?> clz) {
		return hierarchy(clz, new Function<Class<?>, Iterable<Class<?>>>() {
			final Set<Class<?>> seenInterfaces = Sets.newIdentityHashSet();
			
			@Override public Iterable<Class<?>> apply(Class<?> input) {
				if (input == clz)
					seenInterfaces.clear();
				
				Queue<Class<?>> ret = Queues.newArrayDeque();
				for (Class<?> c : input.getInterfaces())
					if (!seenInterfaces.contains(c)) {
						ret.add(c);
						seenInterfaces.add(c);
					}
				
				Class<?> c = input.getSuperclass();
				if (c != null)
					ret.add(c);
				
				return ret;
			}
		});
	}
	
	public static Method getPublic(Method method) {
		for (Class<?> clz : getTypes(method.getDeclaringClass()))
			if (Modifier.isPublic(clz.getModifiers()))
				try {
					Method m = clz.getDeclaredMethod(method.getName(), method.getParameterTypes());
					if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()))
						return m;
				} catch (NoSuchMethodException ignored) { }
		
		return null;
	}
	
	public static Iterable<Entry<Method, Annotation>> getMethodAnnotations(final Class<?> clz) {
		return new Iterable<Entry<Method, Annotation>>() { public Iterator<Entry<Method, Annotation>> iterator() {
			return new AnnotatedMethodIterator(Iterators.forArray(clz.getDeclaredMethods()));
		}};
	}
	private static class AnnotatedMethodIterator extends AbstractIterator<Entry<Method, Annotation>> {
		final Iterator<Method> methodIterator;
		Method currentMethod;
		Iterator<Annotation> annotationIterator = Iterators.emptyIterator();
		AnnotatedMethodIterator(Iterator<Method> methodIterator) {
			this.methodIterator = methodIterator;
		}
		@Override protected Entry<Method, Annotation> computeNext() {
			while (true) {
				if (annotationIterator.hasNext())
					return Maps.immutableEntry(currentMethod, annotationIterator.next());
				else if (methodIterator.hasNext()) {
					currentMethod = methodIterator.next();
					annotationIterator = Iterators.forArray(currentMethod.getAnnotations());
				}
				else
					return endOfData();
			}
		}
	}
	
	public static void initialize(Class<?>... a) {
		for (Class<?> c : a)
			try {
				Class.forName(c.getName());
			} catch (ClassNotFoundException e) {
				throw Throwables.propagate(e);
			}
	}
	
	
	
	// asm
	
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> defineClass(String name, byte[] data) {
		return (Class<T>) ASMClassLoader.INSTANCE.define(name, data);
	}
	private static class ASMClassLoader extends ClassLoader {
		private static final ASMClassLoader INSTANCE = new ASMClassLoader();
		private ASMClassLoader() {
			super(ASMClassLoader.class.getClassLoader());
		}
		public Class<?> define(String name, byte[] data) {
			return defineClass(name, data, 0, data.length);
		}
	}
	
	
	
	// functional idioms
	
	private static <T> Iterable<T> hierarchy(final T root, Function<? super T, ? extends Iterable<T>> subordinateFunction) {
		final Function<? super T, Iterator<T>> func = Functions.compose(Utils.<T>iterableFunction(), subordinateFunction);
		
		return new Iterable<T>() { public Iterator<T> iterator() {
			return new HierarchyIterator<>(Iterators.singletonIterator(root), func);
		}};
	}
	private static class HierarchyIterator<T> extends AbstractIterator<T> {
		final Queue<Iterator<T>> queue = Collections.asLifoQueue(Queues.<Iterator<T>>newArrayDeque());
		final Function<? super T, Iterator<T>> function;
		HierarchyIterator(Iterator<T> root, Function<? super T, Iterator<T>> function) {
			queue.add(root);
			this.function = function;
		}
		@Override protected T computeNext() {
			while (true) {
				Iterator<T> itr = queue.peek();
				if (itr != null) {
					if (itr.hasNext()) {
						T ret = itr.next();
						queue.add(function.apply(ret));
						return ret;
					}
					else
						queue.remove();
				}
				else
					return endOfData();
			}
		}
	}
	
	public static <E> List<E> asList(Function<Integer, E> elementFunction, int size) {
		return new FunctionList<>(elementFunction, size);
	}
	private static class FunctionList<E> extends AbstractList<E> implements RandomAccess {
		private final Function<Integer, E> elementFunction;
		private final int size;
		private FunctionList(Function<Integer, E> elementFunction, int size) {
			this.elementFunction = elementFunction;
			this.size = size;
		}
		@Override public E get(int index) {
			if (index < 0 || index >= size)
				throw new IndexOutOfBoundsException();
			return elementFunction.apply(index);
		}
		@Override public int size() {
			return size;
		}
	}
	
	public static <T> Predicate<T> notIn(Collection<? extends T> target) {
		return Predicates.not(Predicates.in(target));
	}
	
	public static <F, T> Iterable<T> transform(F[] a, Function<? super F, ? extends T> function) {
		return Iterables.transform(Arrays.asList(a), function);
	}
	
	public static <T> Iterable<T> filter(T[] a, Predicate<? super T> predicate) {
		return Iterables.filter(Arrays.asList(a), predicate);
	}
	
	public static <K1, K2, V> Iterable<Entry<K2, V>> transformKeys(Map<K1, V> map, final Function<? super K1, K2> function) {
		return Iterables.transform(map.entrySet(), new Function<Entry<K1, V>, Entry<K2, V>>() {
			@Override public Entry<K2, V> apply(Entry<K1, V> input) {
				return Maps.immutableEntry(function.apply(input.getKey()), input.getValue());
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static <F> Function<F, String> toStringFunction() {
		return (Function<F, String>) Functions.toStringFunction();
	}
	
	public static Runnable doNothing() {
		return EmptyRunnable.INSTANCE;
	}
	private enum EmptyRunnable implements Runnable {
		INSTANCE;
		@Override public void run() { }
	}
	
	public static Function<Class<?>, Type> asmTypeAdaptor() {
		return AsmTypeAdaptor.INSTANCE;
	}
	private enum AsmTypeAdaptor implements Function<Class<?>, Type> {
		INSTANCE;
		@Override public Type apply(Class<?> input) {
			return Type.getType(input);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T> Function<Iterable<T>, Iterator<T>> iterableFunction() {
		return (Function) IterableFunction.INSTANCE;
	}
	private enum IterableFunction implements Function<Iterable<?>, Iterator<?>> {
		INSTANCE;
		@Override public Iterator<?> apply(Iterable<?> input) {
			return input.iterator();
		}
	}
	
	
	
	// syntax
	
	@SuppressWarnings("unchecked")
	public static <T> Optional<T> tryCast(Class<T> clz, Object obj) {
		return clz.isInstance(obj) ? Optional.of((T) obj) : Optional.<T>absent();
	}
	
	public static <T> FluentIterable<T> asIterable(T[] a) {
		return FluentIterable.from(Arrays.asList(a));
	}
	
	
	
	// version validations
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Modid { }

	/**
	 * When applied to an outer class, this represents a map from building MC versions to the required MC versions.
	 * When applied to an inner class, this represents a map from actual MC versions to the required mod versions.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface Requirements {
		public String[] value();
	}
	
	public static Set<ArtifactVersion> checkRequirements(Class<?> clz, String mcString) {
		Set<ArtifactVersion> missing = Sets.newHashSet();
		
		ModContainer mc = Loader.instance().getMinecraftModContainer();
		ArtifactVersion m0 = check(clz, "Minecraft", new SimpleVersion(mcString), mc.getProcessedVersion());
		if (m0 != null)
			missing.add(m0);
		
		Map<String, ModContainer> mods = Loader.instance().getIndexedModList();
		for (Class<?> c : clz.getDeclaredClasses()) {
			String modid = null;
			
			for (Field f : c.getDeclaredFields())
				if (f.getAnnotation(Modid.class) != null) {
					checkArgument(modid == null);
					checkArgument(Modifier.isStatic(f.getModifiers()));
					
					f.setAccessible(true);
					modid = Fields.get(null, f);
				}
			
			if (modid != null && Loader.isModLoaded(modid)) {
				ArtifactVersion m = check(c, modid, MC_VERSION, mods.get(modid).getProcessedVersion());
				if (m != null)
					missing.add(m);
			}
		}
		
		return missing;
	}
	
	private static ArtifactVersion check(Class<?> c, String modid, SimpleVersion key, ArtifactVersion actual) {
		Requirements requirements = c.getAnnotation(Requirements.class);
		
		if (requirements != null) {
			for (String s : requirements.value()) {
				List<String> entry = Splitter.on('=').trimResults().splitToList(s);
				checkArgument(entry.size() == 2);
				
				if (key.isAtLeast(entry.get(0))) {
					ArtifactVersion r = parse(modid, entry.get(1));
					
					if (!r.containsVersion(actual))
						return r;
					break;
				}
			}
		}
		
		return null;
	}
	
	private static ArtifactVersion parse(String modid, String versionRange) {
		char c = versionRange.charAt(0);
		if (c != '[' && c != '(')
			versionRange = "[" + versionRange + ",)";
		return VersionParser.parseVersionReference(modid + "@" + versionRange);
	}
	
}
