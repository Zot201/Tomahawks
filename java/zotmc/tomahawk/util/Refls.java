package zotmc.tomahawk.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;

public class Refls {
	
	public static Field getDeclaredField(Class<?> clz, String fieldName) {
		try {
			Field ret = clz.getDeclaredField(fieldName);
			ret.setAccessible(true);
			return ret;
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Field field, Object obj) {
		try {
			return (T) field.get(obj);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	
	public static Runnable asRunnable(final Object obj, String methodName) {
		final Method method = getInstanceMethod(obj, methodName);
		
		return new Runnable() {
			@Override public void run() {
				invoke(method, obj);
			}
		};
	}
	
	public static <T> Supplier<T> asSupplier(final Object obj, String methodName) {
		final Method method = getInstanceMethod(obj, methodName);
		
		return new Supplier<T>() {
			@SuppressWarnings("unchecked")
			@Override public T get() {
				return (T) invoke(method, obj);
			}
		};
	}
	
	
	private static Object invoke(Method method, Object obj, Object... args) {
		try {
			return method.invoke(obj, args);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	
	
	private static Method getInstanceMethod(Object obj, String name, Class<?>... parameterTypes) {
		for (Class<?> clz = obj.getClass(); clz != null; clz = clz.getSuperclass()) {
			
			Method ret = tryGetDeclaredMethod(clz, name, parameterTypes);
			if (ret != null) {
				ret.setAccessible(true);
				return ret;
			}
		}
		
		throw new RuntimeException(
				new NoSuchMethodException(String.format(
						"%s.%s(%s)",
						obj.getClass(), name, argumentTypesToString(parameterTypes))));
	}
	
	private static String argumentTypesToString(Class<?>[] argTypes) {
		return Joiner.on(", ").join(FluentIterable
				.from(Arrays.asList(argTypes))
				.transform(getClassName));
	}
	
	private static final Function<Class<?>, String> getClassName =
			new Function<Class<?>, String>() {
		@Override public String apply(Class<?> input) {
			return input != null ? input.getName() : "null";
		}
	};
	
	
	
	private static Method getDeclaredMethod(Class<?> clz, String name, Class<?>... parameterTypes) {
		try {
			return clz.getDeclaredMethod(name, parameterTypes);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	
	private static Method tryGetDeclaredMethod(Class<?> clz, String name, Class<?>... parameterTypes) {
		try {
			return clz.getDeclaredMethod(name, parameterTypes);
		} catch (Throwable ignored) { }
		
		return null;
	}

}
