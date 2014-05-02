package zotmc.tomahawk.config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;

public class Config {
	
	private static final String
	GENERAL = "general";
	
	public static final Configurable<Set<Item>>
	axeBlacklist = new ItemSetConfigurable(ImmutableSet.<Item>of(), GENERAL, "axeBlacklist");
	
	
	public static void init(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		
		for (Configurable<?> c : Configurable.configurables)
			c.load(config);
		
		Configurable.configurables = null;
		
		config.save();
		
	}
	
	
	public static abstract class Configurable<E> {
		private static List<Configurable<?>> configurables = Lists.newLinkedList();
		private static final Joiner LN = Joiner.on('\n');
		protected final String category, key, comment;
		private boolean isLoaded;
		public Configurable(String category, String key, String... comments) {
			this.category = category;
			this.key = key;
			this.comment = LN.join(Arrays.asList(comments));
			
			configurables.add(this);
		}
		public abstract void load(Configuration config);
		public final E get() {
			if (!isLoaded)
				throw new IllegalStateException();
			return computeResult();
		}
		protected abstract E computeResult();
		public void setIsLoaded() {
			if (isLoaded)
				throw new IllegalStateException();
			isLoaded = true;
		}
	}
	
	public static class ItemSetConfigurable extends Configurable<Set<Item>> {
		private Set<Item> value;
		public ItemSetConfigurable(Set<Item> _default,
				String category, String key, String... comments) {
			super(category, key, comments);
			value = _default;
		}
		@Override public void load(Configuration config) {
			Function<Item, String> idFunction = new Function<Item, String>() {
				@Override public String apply(Item input) {
					return GameData.getItemRegistry().getNameForObject(input);
				}
			};
			String[] a = config.get(category, key, FluentIterable
					.from(value)
					.transform(idFunction)
					.toArray(String.class)).getStringList();
			
			Function<String, Item> itemFunction = new Function<String, Item>() {
				@Override public Item apply(String input) {
					return GameData.getItemRegistry().getRaw(input);
				}
			};
			value = FluentIterable
					.from(Arrays.asList(a))
					.transform(itemFunction)
					.toSet();
			
			setIsLoaded();
		}
		@Override protected Set<Item> computeResult() {
			return value;
		}
	}

}
