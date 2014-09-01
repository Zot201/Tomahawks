package zotmc.tomahawk.config;

import net.minecraftforge.common.config.Configuration;

public class ConfigurableInteger extends Configurable<Integer> {
	
	private int value;
	
	ConfigurableInteger(String category, String key) {
		super(category, key);
	}
	
	@Override public Integer get() {
		return value;
	}
	
	@Override Configurable<Integer> set(Integer value) {
		this.value = value;
		return this;
	}
	
	@Override void load(Configuration configFile) {
		value = configFile.get(category, key, value).getInt(value);
	}
	
	@Override void save(Configuration configFile) {
		configFile.get(category, key, 0).set(value);
		
		configFile.save();
	}
	
}
