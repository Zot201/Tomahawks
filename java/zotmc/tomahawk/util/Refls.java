package zotmc.tomahawk.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class Refls {
	
	public static Class<?> findClass(String... names) {
		for (String s : names)
			try {
				return Class.forName(s);
			} catch (Throwable ignored) { }
		
		throw new UnknownClassException(Joiner.on(", ").join(names)); 
	}
	
	
	public static Field findField(Class<?> clz, String... names) {
		String owner = unmapTypeName(clz);
		for (String s : names)
			try {
				Field f = clz.getDeclaredField(remapFieldName(owner, s));
				f.setAccessible(true);
				return f;
			} catch (Throwable ignored) { }
		
		throw new UnknownFieldException(clz.getName() + ".{" + Joiner.on(", ").join(names) + "}");
	}
	
	
	public static <T> MethodFinder<T> findMethod(Class<T> clz, String... names) {
		return new MethodFinder<T>(clz, names);
	}
	public static class MethodFinder<T> {
		private final Class<T> clz;
		private final String[] names;
		private MethodFinder(Class<T> clz, String[] names) {
			this.clz = clz;
			this.names = names;
		}
		
		public Method withArgs(Class<?>... parameterTypes) {
			String owner = unmapTypeName(clz);
			for (String s : names)
				try {
					Method m = clz.getDeclaredMethod(remapMethodName(owner, s), parameterTypes);
					m.setAccessible(true);
					return m;
				} catch (Throwable ignored) { }
			
			throw new UnknownMethodException(String.format(
					"%s.{%s}(%s)",
					clz.getName(),
					Joiner.on(", ").join(names),
					Joiner.on(", ").join(Utils.transform(parameterTypes, ClassNameFunction.INSTANCE))
			));
		}
		public Invokable<T, Object> asInvokable(Class<?>... parameterTypes) {
			return TypeToken.of(clz).method(withArgs(parameterTypes));
		}
	}
	
	
	
	private static String unmapTypeName(Class<?> clz) {
		return FMLDeobfuscatingRemapper.INSTANCE.unmap(Type.getInternalName(clz));
	}
	private static String remapFieldName(String owner, String field) {
		return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, field, null);
	}
	private static String remapMethodName(String owner, String method) {
		return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner, method, null);
	}
	
	enum ClassNameFunction implements Function<Class<?>, String> {
		INSTANCE;
		@Override public String apply(Class<?> input) {
			return input == null ? "null" : input.getName();
		}
	}

}
