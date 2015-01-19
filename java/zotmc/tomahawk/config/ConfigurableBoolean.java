package zotmc.tomahawk.config;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;

public class ConfigurableBoolean extends Configurable<Boolean> {
	
	private boolean value;
	
	ConfigurableBoolean(String category, String key) {
		super(category, key);
	}
	
	@Override public Boolean get() {
		return value;
	}
	
	@Override Configurable<Boolean> set(Boolean value) {
		this.value = value;
		return this;
	}
	
	@Override void load(Configuration configFile) {
		value = configFile.get(category, key, value).getBoolean(value);
	}
	
	@Override void save(Configuration configFile) {
		configFile.get(category, key, false).set(value);
		
		configFile.save();
	}
	
	@Override NBTTagCompound writeToNBT() {
		NBTTagCompound tags = new NBTTagCompound();
		tags.setBoolean("value", value);
		return tags;
	}
	
	@Override void readFromNBT(NBTTagCompound tags) {
		value = tags.getBoolean("value");
	}
	
}
