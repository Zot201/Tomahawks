package zotmc.tomahawk.config;

import net.minecraftforge.common.config.Configuration;

import com.google.common.base.Supplier;

public abstract class Configurable<T> implements Supplier<T> {
	
	protected final String category, key;
	
	public Configurable(String category, String key) {
		this.category = category;
		this.key = key;
	}
	
	abstract Configurable<T> set(T value);
	
	abstract void load(Configuration configFile);
	
	abstract void save(Configuration configFile);

}
