package zotmc.tomahawk.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.reflect.Modifier.FINAL;

import java.lang.reflect.Field;

import zotmc.tomahawk.util.geometry.Vec3i;
import zotmc.tomahawk.util.prop.Prop;

import com.google.common.base.Throwables;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;

public class Fields {
	
	public static Field definalize(Field field) {
		try {
			MODIFIERS.setInt(field, field.getModifiers() & ~FINAL);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
		return field;
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
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Object obj, Field field) {
		try {
			return (T) field.get(obj);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	public static <T> void set(Object obj, Field field, T value) {
		try {
			field.set(obj, value);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}

	private static int getInt(Object obj, Field field) {
		try {
			return field.getInt(obj);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	private static void setInt(Object obj, Field field, int value) {
		try {
			field.setInt(obj, value);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	
	
	// factory
	
	public static Vec3i asVec3i(final Object obj, final Field x, final Field y, final Field z) {
		checkArgument(Primitives.unwrap(x.getType()) == int.class);
		checkArgument(Primitives.unwrap(y.getType()) == int.class);
		checkArgument(Primitives.unwrap(z.getType()) == int.class);
		
		return new Vec3i() {
			@Override public void setX(int x1) {
				Fields.setInt(obj, x, x1);
			}
			@Override public void setY(int y1) {
				Fields.setInt(obj, y, y1);
			}
			@Override public void setZ(int z1) {
				Fields.setInt(obj, z, z1);
			}
			
			@Override public int x() {
				return Fields.getInt(obj, x);
			}
			@Override public int y() {
				return Fields.getInt(obj, y);
			}
			@Override public int z() {
				return Fields.getInt(obj, z);
			}
		};
	}
	
	
	
	// prop

	public static FieldAccess<?> referTo(Class<?> declaringClz, String... fieldNames) {
		return referTo(null, Utils.findField(declaringClz, fieldNames));
	}
	public static <T> FieldAccess<?> referTo(T obj, Class<? super T> declaringClz, String... fieldNames) {
		return referTo(obj, Utils.findField(declaringClz, fieldNames));
	}
	public static FieldAccess<?> referTo(final Object obj, Field field) {
		return new FieldAccess<Object>(field) {
			@Override protected Object obj() {
				return obj;
			}
		};
	}
	
	public static abstract class FieldAccess<T> implements Prop<T> {
		private final Field field;
		private FieldAccess(Field field) {
			this.field = field;
		}
		protected abstract Object obj();
		@Override public T get() {
			return Fields.get(obj(), field);
		}
		@Override public void set(T value) {
			Fields.set(obj(), field, value);
		}
		
		@SuppressWarnings("unchecked")
		public <U> FieldAccess<U> ofType(Class<U> clz) {
			checkArgument(clz.isAssignableFrom(field.getType()));
			return (FieldAccess<U>) this;
		}
		@SuppressWarnings("unchecked")
		public <U> FieldAccess<U> ofType(TypeToken<U> type) {
			checkArgument(type.getRawType().isAssignableFrom(field.getType()));
			return (FieldAccess<U>) this;
		}
		
		public FieldAccess<?> downTo(Class<?> declaringClz, String... fieldNames) {
			return downTo(Utils.findField(declaringClz, fieldNames));
		}
		public FieldAccess<?> downTo(Field field) {
			return new FieldAccess<T>(field) {
				@Override protected Object obj() {
					return FieldAccess.this.get();
				}
			};
		}
	}

}
