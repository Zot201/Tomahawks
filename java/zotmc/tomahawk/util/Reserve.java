package zotmc.tomahawk.util;

import static com.google.common.base.Preconditions.checkNotNull;

public class Reserve<T> implements Feature<T> {

	private T reference;
	private Reserve() { }
	
	public static <T> Reserve<T> absent() {
		return new Reserve<T>();
	}
	
	
	@Override public boolean exists() {
		return reference != null;
	}
	
	@Override public T get() {
		if (!exists())
			throw new IllegalStateException();
		return reference;
	}
	
	public void set(T reference) {
		if (exists())
			throw new IllegalStateException();
		this.reference = checkNotNull(reference);
	}
	
}
