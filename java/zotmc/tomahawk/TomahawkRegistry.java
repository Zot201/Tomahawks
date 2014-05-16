package zotmc.tomahawk;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.Block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.ModContainer;

public class TomahawkRegistry {
	
	//Feel free to access these fields through reflection, their names are remaining unchanged unless stated.
	private static final Queue<Predicate<Item>> throwableAxes = Queues.newArrayDeque();
	private static final Set<Item> throwableAxesCache = Sets.newIdentityHashSet();
	
	private static final Queue<Function<Item, SoundType>> hitSounds = Queues.newArrayDeque();
	private static final Map<Item, SoundType> hitSoundsCache = Maps.newIdentityHashMap();
	
	//Experimental feature
	private static final Set<ModContainer> damageFakings = Sets.newIdentityHashSet();
	
	
	
	public static void registerThrowableAxes(Predicate<Item> isThrowableAxe) {
		throwableAxes.add((Predicate<Item>) isThrowableAxe);
	}
	
	public static void registerHitSounds(Function<Item, SoundType> hitSoundFactory) {
		hitSounds.add(hitSoundFactory);
	}
	
	public static void registerDamageFaking(ModContainer mc) {
		damageFakings.add(mc);
	}
	
	
	
	public static boolean isThrowableAxe(Item item) {
		return throwableAxesCache.contains(item);
	}
	public static boolean isThrowableAxe(ItemStack item) {
		return item != null && isThrowableAxe(item.getItem());
	}
	
	public static Predicate<Item> isThrowableAxeRaw() {
		return Predicates.or(throwableAxes);
	}
	
	public static SoundType getHitSound(Item axe) {
		return hitSoundsCache.get(axe);
	}
	
	public static boolean fakeDamages(ModContainer modContainer) {
		return damageFakings.contains(modContainer);
	}
	
	
	
	
	static {
		registerThrowableAxes(new Predicate<Item>() {
			@Override public boolean apply(Item input) {
				return Config.current().commonAxesThrowing.get()
						&& input instanceof ItemAxe;
			}
		});
		
	}
	
	public static void refreshThrowableAxes() {
		throwableAxesCache.clear();
		
		Predicate<Item> predicates = Predicates.or(throwableAxes);
		
		for (Item i : Utils.itemList())
			if (!Config.current().axeBlacklist.items().get().contains(i)
					&& predicates.apply(i))
				throwableAxesCache.add(i);
	}
	
	public static void refreshHitSounds() {
		hitSoundsCache.clear();
		
		for (Item i : Utils.itemList())
			for (Function<Item, SoundType> f : hitSounds) {
				SoundType s = f.apply(i);
				if (s != null) {
					hitSoundsCache.put(i, s);
					break;
				}
			}
	}

}
