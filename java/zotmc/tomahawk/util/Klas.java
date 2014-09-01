package zotmc.tomahawk.util;

import java.lang.reflect.Array;
import java.util.Map;

import org.objectweb.asm.Type;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public abstract class Klas<T> implements Supplier<Class<T>> {
	
	public static final Klas<Byte> BYTE = PrimitiveKlas.of(byte.class, "B");
	public static final Klas<Character> CHAR = PrimitiveKlas.of(char.class, "C");
	public static final Klas<Double> DOUBLE = PrimitiveKlas.of(double.class, "D");
	public static final Klas<Float> FLOAT = PrimitiveKlas.of(float.class, "F");
	public static final Klas<Integer> INT = PrimitiveKlas.of(int.class, "I");
	public static final Klas<Long> LONG = PrimitiveKlas.of(long.class, "J");
	public static final Klas<Short> SHORT = PrimitiveKlas.of(short.class, "S");
	public static final Klas<Void> VOID = PrimitiveKlas.of(void.class, "V");
	public static final Klas<Boolean> BOOLEAN = PrimitiveKlas.of(boolean.class, "Z");
	
	
	private Klas() { }
	
	public abstract Class<T> toClass();
	@Deprecated @Override public Class<T> get() {
		return toClass();
	}
	
	public abstract String toName();
	
	public abstract String toDescriptor();
	
	public Type toType() {
		return Type.getType(toDescriptor());
	}
	
	
	@Override public int hashCode() {
		return toName().hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Klas)
			return ((Klas<?>) obj).toName().equals(toName());
		
		return false;
	}
	
	@Override public String toString() {
		return getClass().getSimpleName() + " " + toName();
	}
	
	
	
	
	public static class PrimitiveKlas<T> extends Klas<T> {
		private static final Map<String, Klas<?>>
		FROM_NAME = Maps.newHashMap(), FROM_DESC = Maps.newHashMap();
		
		private final Class<T> clz;
		private final String name, desc;
		private PrimitiveKlas(Class<T> clz, String desc) {
			this.clz = clz;
			this.name = clz.getName();
			this.desc = desc;
			
			FROM_NAME.put(name, this);
			FROM_DESC.put(desc, this);
		}
		private static <T> PrimitiveKlas<T> of(Class<T> clz, String desc) {
			return new PrimitiveKlas<T>(clz, desc);
		}
		
		@Override public Class<T> toClass() {
			return clz;
		}
		@Override public String toName() {
			return name;
		}
		@Override public String toDescriptor() {
			return desc;
		}
	}
	
	public static class ObjectKlas<T> extends Klas<T> {
		private final String name;
		private ObjectKlas(String name) {
			this.name = name;
		}
		private ObjectKlas(Class<T> clz) {
			this(clz.getName());
			this.clz = clz;
		}
		
		private Class<T> clz;
		@SuppressWarnings("unchecked")
		@Override public Class<T> toClass() {
			if (clz != null)
				return clz;
			
			try {
				return clz = (Class<T>) Class.forName(name);
			} catch (Throwable e) {
				throw Throwables.propagate(e);
			}
		}
		@Override public String toName() {
			return name;
		}
		@Override public String toDescriptor() {
			return "L" + toName().replace('.', '/') + ";";
		}
	}
	
	public static class ArrayKlas<T> extends Klas<T> {
		private final String name;
		private final Klas<?> component;
		private final int nDimensions;
		private ArrayKlas(String name, Klas<?> component, int nDimensions) {
			this.name = name;
			this.component = component;
			this.nDimensions = nDimensions;
		}
		private ArrayKlas(Class<T> clz) {
			this(clz.getName(), null, -1);
			this.clz = clz;
		}
		
		private Class<T> clz;
		@SuppressWarnings("unchecked")
		@Override public Class<T> toClass() {
			if (clz != null)
				return clz;
			
			return clz = (Class<T>) Array
					.newInstance(component.toClass(), new int[nDimensions])
					.getClass();
		}
		@Override public String toName() {
			return name;
		}
		@Override public String toDescriptor() {
			return toName().replace('.', '/');
		}
	}
	
	
	public static <T> Klas<T> ofClass(Class<T> clz) {
		@SuppressWarnings("unchecked")
		Klas<T> primitive = (Klas<T>) PrimitiveKlas.FROM_NAME.get(clz.getName());
		if (primitive != null)
			return primitive;
		
		if (!clz.isArray())
			return new ObjectKlas<T>(clz);
		
		return new ArrayKlas<T>(clz);
	}
	
	private static <T> ArrayKlas<T> getArrayKlas(String desc) {
		int nDims = desc.lastIndexOf('[') + 1;
		if (nDims > 0) {
			for (int i = 0; i < nDims - 1; i++)
				if (desc.charAt(i) != '[')
					throw new IllegalArgumentException(desc);
			
			return new ArrayKlas<T>(
					desc.replace('/', '.'),
					ofDescriptor(desc.substring(nDims)),
					nDims);
		}
		
		return null;
	}
	
	public static <T> Klas<T> ofName(String name) {
		@SuppressWarnings("unchecked")
		Klas<T> primitive = (Klas<T>) PrimitiveKlas.FROM_NAME.get(name);
		if (primitive != null)
			return primitive;
		
		if (name.indexOf('/') >= 0)
			throw new IllegalArgumentException(name);
		
		Klas<T> array = getArrayKlas(name.replace('.', '/'));
		if (array != null)
			return array;
		
		return new ObjectKlas<T>(name);
	}
	
	public static <T> Klas<T> ofDescriptor(String desc) {
		@SuppressWarnings("unchecked")
		Klas<T> primitive = (Klas<T>) PrimitiveKlas.FROM_DESC.get(desc);
		if (primitive != null)
			return primitive;
		
		if (desc.indexOf('.') >= 0)
			throw new IllegalArgumentException(desc);
		
		Klas<T> array = getArrayKlas(desc);
		if (array != null)
			return array;
		
		return new ObjectKlas<T>(desc.substring(1, desc.length() - 1).replace('/', '.'));
	}
	
	public static <T> Klas<T> ofType(Type type) {
		return ofDescriptor(type.getDescriptor());
	}
	
}
