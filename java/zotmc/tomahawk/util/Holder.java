package zotmc.tomahawk.util;

import static com.google.common.base.Preconditions.checkNotNull;

public class Holder<T> {
	
	private T reference;
	
	private Holder() { }
	
	
	public static <T> Holder<T> absent() {
		return new Holder<T>();
	}
	
	public static <T> Holder<T> of(T reference) {
		Holder<T> ret = absent();
		ret.set(reference);
		return ret;
	}
	
	public static <T> Holder<T> ofNullable(T reference) {
		Holder<T> ret = absent();
		ret.setNullable(reference);
		return ret;
	}
	
	
	public void set(T reference) {
		this.reference = checkNotNull(reference);
	}
	
	public void clear() {
		reference = null;
	}
	
	public void setNullable(T reference) {
		this.reference = reference;
	}
	
	
	public T get() {
		if (reference == null)
			throw new IllegalStateException();
		return reference;
	}
	
	public T orNull() {
		return reference;
	}
	
	public T or(T defaultValue) {
		checkNotNull(defaultValue);
		return reference != null ? reference : defaultValue;
	}
	
	
	public boolean isPresent() {
		return reference != null;
	}
	

}
