package zotmc.tomahawk.util;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpw.mods.fml.common.ObfuscationReflectionHelper.remapFieldNames;
import static java.lang.reflect.Modifier.FINAL;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.base.Throwables;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class Obfs {
	
	public static Field findField(Class<?> clz, String... names) {
		Field f = null;
		for (String s : remapFieldNames(clz.getName(), names))
			try {
				f = clz.getDeclaredField(s);
				f.setAccessible(true);
				break;
			} catch (Throwable ignored) { }
		
		return checkNotNull(f);
	}
	
	public static Field findFieldFinal(Class<?> clz, String... names) {
		Field f = findField(clz, names);
		try {
			MODIFIERS.setInt(f, f.getModifiers() & ~FINAL);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
		return f;
	}
	
	private static final Field MODIFIERS;
	static {
		Field f = null;
		try {
			f = Field.class.getDeclaredField("modifiers");
			f.setAccessible(true);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
		MODIFIERS = f;
	}
	
	
	
	public static MethodFinder findMethod(Class<?> clz, String... names) {
		return new MethodFinder(clz, names);
	}
	
	public static class MethodFinder {
		private final Class<?> clz;
		private final String[] names;
		private MethodFinder(Class<?> clz, String[] names) {
			this.clz = clz;
			this.names = names;
		}
		public Method withArgs(Class<?>... parameterTypes) {
			Method m = null;
			for (String s : remapMethodNames(clz.getName(), names))
				try {
					m = clz.getDeclaredMethod(s, parameterTypes);
					m.setAccessible(true);
					break;
				} catch (Throwable ignored) { }
			
			return checkNotNull(m);
		}
	}
	
    private static String[] remapMethodNames(String className, String... methodNames) {
        String internalClassName = FMLDeobfuscatingRemapper.INSTANCE.unmap(className.replace('.', '/'));
        String[] mappedNames = new String[methodNames.length];
        int i = 0;
        for (String mName : methodNames)
            mappedNames[i++] = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
            		internalClassName, mName, null);
        return mappedNames;
    }

}
