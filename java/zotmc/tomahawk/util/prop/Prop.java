package zotmc.tomahawk.util.prop;

import com.google.common.base.Supplier;

public interface Prop<T> extends Supplier<T> {
	
	public void set(T value);

}
