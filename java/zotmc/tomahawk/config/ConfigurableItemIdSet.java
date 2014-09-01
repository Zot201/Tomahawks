package zotmc.tomahawk.config;

import static com.google.common.base.Predicates.isNull;

import java.util.Set;

import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import zotmc.tomahawk.util.TransformedSet;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.registry.GameData;

public class ConfigurableItemIdSet extends ConfigurableStringList<Set<String>, String> {

	ConfigurableItemIdSet(String category, String key) {
		super(category, key);
	}
	
	@Override protected Set<String> getInitialValue() {
		return ImmutableSet.of();
	}
	@Override protected Function<String, String> toStringFunction() {
		return Functions.identity();
	}
	@Override protected Function<String, String> valueOfFunction() {
		return Functions.identity();
	}
	
	@Override protected void setIterable(FluentIterable<String> iterable) {
		value = iterable.toSet();
	}
	
	
	private Configurable<Set<Item>> items;
	public Configurable<Set<Item>> asItems() {
		return items != null ? items : (items = new ItemSet());
	}
	
	private class ItemSet extends Configurable<Set<Item>> {
		private final Function<Item, String> toString = new Function<Item, String>() {
			@Override public String apply(Item input) {
				return GameData.getItemRegistry().getNameForObject(input);
			}
		};
		
		private final Function<String, Item> valueOf = new Function<String, Item>() {
			@Override public Item apply(String input) {
				return GameData.getItemRegistry().getRaw(input);
			}
		};
		
		private Set<Item> value = Sets.filter(new TransformedSet<String, Item>() {
			@Override protected Set<String> backing() {
				return ConfigurableItemIdSet.this.value;
			}
			@Override protected Function<String, Item> transformation() {
				return valueOf;
			}
			
			@Override public boolean contains(Object o) {
				return o instanceof Item && backing().contains(toString.apply((Item) o));
			}
			
			@Override public boolean add(Item e) {
				throw new UnsupportedOperationException();
			}
			@Override public boolean remove(Object o) {
				throw new UnsupportedOperationException();
			}
		}, Predicates.notNull());
		
		private ItemSet() {
			super("", "");
		}
		
		@Override public Set<Item> get() {
			return value;
		}
		@Override Configurable<Set<Item>> set(Set<Item> value) {
			ConfigurableItemIdSet.this.set(ImmutableSet
					.<String>builder()
					.addAll(Collections2.transform(
							value,
							toString))
					.addAll(Sets.filter(
							ConfigurableItemIdSet.this.value,
							Predicates.compose(isNull(), valueOf)))
					.build());
			
			return this;
		}
		
		@Override void load(Configuration configFile) {
			throw new UnsupportedOperationException();
		}
		@Override void save(Configuration configFile) {
			throw new UnsupportedOperationException();
		}
	}
	
}
