package zotmc.tomahawk.config;

import java.util.Set;

import net.minecraft.item.Item;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

import cpw.mods.fml.common.registry.GameData;

public class ConfigurableItemSet extends ConfigurableStringList<Set<Item>, Item> {

	ConfigurableItemSet(String category, String key) {
		super(category, key);
	}
	
	@Override protected Set<Item> getInitialValue() {
		return ImmutableSet.of();
	}
	
	@Override protected Function<Item, String> toStringFunction() {
		return new Function<Item, String>() {
			@Override public String apply(Item input) {
				return GameData.getItemRegistry().getNameForObject(input);
			}
		};
	}
	
	@Override protected Function<String, Item> valueOfFunction() {
		return new Function<String, Item>() {
			@Override public Item apply(String input) {
				return GameData.getItemRegistry().getRaw(input);
			}
		};
	}
	
	@Override protected void setIterable(FluentIterable<Item> iterable) {
		value = iterable.toSet();
	}
	
}
