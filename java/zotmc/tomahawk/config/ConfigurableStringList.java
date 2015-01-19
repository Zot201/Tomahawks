package zotmc.tomahawk.config;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.Configuration;
import zotmc.tomahawk.util.Utils;

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
		
		setIterable(Utils.asIterable(a).transform(valueOfFunction()));
	}
	
	@Override void save(Configuration configFile) {
		configFile
			.get(category, key, new String[0])
			.set(getStringList());
		
		configFile.save();
	}
	
	@Override NBTTagCompound writeToNBT() {
		NBTTagCompound tags = new NBTTagCompound();
		
		NBTTagList list = new NBTTagList();
		for (String s : getStringList())
			list.appendTag(new NBTTagString(s));
		tags.setTag("value", list);
		
		return tags;
	}
	
	@Override void readFromNBT(NBTTagCompound tags) {
		NBTTagList list = tags.getTagList("value");
		
		String[] a = new String[list.tagCount()];
		for (int i = 0; i < list.tagCount(); i++)
			a[i] = ((NBTTagString) list.tagAt(i)).data;
		
		setIterable(Utils.asIterable(a).transform(valueOfFunction()));
	}

}
