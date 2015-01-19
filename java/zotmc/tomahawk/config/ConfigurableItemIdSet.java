package zotmc.tomahawk.config;

import static com.google.common.base.Predicates.isNull;

import java.util.Arrays;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.Configuration;
import zotmc.tomahawk.util.TransformedSet;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ConfigurableItemIdSet extends ConfigurableStringList<Set<Integer>, Integer> {

	ConfigurableItemIdSet(String category, String key) {
		super(category, key);
	}
	
	@Override protected Set<Integer> getInitialValue() {
		return ImmutableSet.of();
	}
	@Override protected Function<Integer, String> toStringFunction() {
		return Utils.toStringFunction();
	}
	@Override protected Function<String, Integer> valueOfFunction() {
		return IntegerParser.INSTANCE;
	}
	
	@Override protected void setIterable(FluentIterable<Integer> iterable) {
		value = iterable.toSet();
	}
	
	
	private Configurable<Set<Item>> items;
	public Configurable<Set<Item>> asItems() {
		return items != null ? items : (items = new ItemSet());
	}
	
	private class ItemSet extends Configurable<Set<Item>> {
		private final Function<Item, Integer> toInteger = new Function<Item, Integer>() {
			@Override public Integer apply(Item input) {
				return Arrays.binarySearch(Item.itemsList, input);
			}
		};
		
		private final Function<Integer, Item> valueOf = new Function<Integer, Item>() {
			@Override public Item apply(Integer input) {
				return Item.itemsList[input];
			}
		};
		
		private Set<Item> value = Sets.filter(new TransformedSet<Integer, Item>() {
			@Override protected Set<Integer> backing() {
				return ConfigurableItemIdSet.this.value;
			}
			@Override protected Function<Integer, Item> transformation() {
				return valueOf;
			}
			
			@Override public boolean contains(Object o) {
				return o instanceof Item && backing().contains(toInteger.apply((Item) o));
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
					.<Integer>builder()
					.addAll(Collections2.transform(
							value,
							toInteger))
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
		@Override NBTTagCompound writeToNBT() {
			throw new UnsupportedOperationException();
		}
		@Override void readFromNBT(NBTTagCompound tags) {
			throw new UnsupportedOperationException();
		}
	}
	
	private enum IntegerParser implements Function<String, Integer> {
		INSTANCE;
		@Override public Integer apply(String input) {
			return Integer.parseInt(input);
		}
	}
	
}
