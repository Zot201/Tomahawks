package zotmc.tomahawk;

import java.lang.reflect.Field;

public abstract class FieldAccess<T> {
	
	private final Field field;
	
	private FieldAccess(Field field) {
		this.field = field;
	}

	public static <T> FieldAccess<T> of(Field field, final Object obj) {
		return new FieldAccess<T>(field) {
			@Override protected Object object() {
				return obj;
			}
		};
	}
	public static <T> FieldAccess<T> nesting(Field field, final FieldAccess<?> fa) {
		return new FieldAccess<T>(field) {
			@Override protected Object object() {
				return fa.get();
			}
		};
	}
	
	
	protected abstract Object object();
	
	public void set(T value) {
		try {
			field.set(object(), value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public T get() {
		try {
			return (T) field.get(object());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
