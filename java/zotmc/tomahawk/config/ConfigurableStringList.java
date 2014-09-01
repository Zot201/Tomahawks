package zotmc.tomahawk.config;

import java.util.Arrays;

import net.minecraftforge.common.config.Configuration;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public abstract class ConfigurableStringList<T extends Iterable<U>, U> extends Configurable<T> {
	
	protected T value = getInitialValue();
	
	ConfigurableStringList(String category, String key) {
		super(category, key);
	}
	
	protected abstract T getInitialValue();
	
	protected abstract Function<U, String> toStringFunction();
	
	protected abstract Function<String, U> valueOfFunction();
	
	
	
	@Override public T get() {
		return value;
	}
	
	protected String[] getStringList() {
		return FluentIterable
				.from(value)
				.transform(toStringFunction())
				.toArray(String.class);
	}
	
	@Override Configurable<T> set(T value) {
		this.value = value;
		return this;
	}
	
	protected abstract void setIterable(FluentIterable<U> iterable);
	
	@Override void load(Configuration configFile) {
		String[] a = configFile
				.get(category, key, getStringList())
				.getStringList();
		
		setIterable(FluentIterable
				.from(Arrays.asList(a))
				.transform(valueOfFunction()));
	}
	
	@Override void save(Configuration configFile) {
		configFile
			.get(category, key, new String[0])
			.set(getStringList());
		
		configFile.save();
	}

}
