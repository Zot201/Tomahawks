package zotmc.tomahawk.config;

import java.util.Objects;

import net.minecraftforge.common.config.Configuration;

import com.google.common.base.Supplier;

public abstract class Configurable<T> implements Supplier<T> {
	
	protected final String category, key;
	
	Configurable(String category, String key) {
		this.category = category;
		this.key = key;
	}
	
	abstract Configurable<T> set(T value);
	
	abstract void load(Configuration configFile);
	
	abstract void save(Configuration configFile);
	
	
	@Override public int hashCode() {
		return Objects.hashCode(get());
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Configurable)
			return Objects.equals(get(), ((Configurable<?>) obj).get());
		return false;
	}

}
