package zotmc.tomahawk.api;

import java.util.Set;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.util.Feature;
import zotmc.tomahawk.util.Reserve;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLCommonHandler;

public class TomahawkAPI {
	
	public static final Feature<Enchantment> replica = Reserve.absent();
	
	private static Set<Item> itemBlacklist;
	
	
	public static boolean isLaunchable(ItemStack item) {
		if (item != null) {
			Item i = item.getItem();
			if (!isItemBlacklisted(i)) {
				ItemHandler h = TomahawkRegistry.getItemHandler(i);
				return h.isEnabled() && h.isLaunchable(item);
			}
		}
		return false;
	}
	
	public static boolean isItemBlacklisted(ItemStack item) {
		return item != null && isItemBlacklisted(item.getItem());
	}
	
	public static boolean isItemBlacklisted(Item item) {
		return (itemBlacklist != null ? itemBlacklist : Config.current().itemBlacklist.asItems().get())
				.contains(item);
	}
	
	
	private static final void onAvailable() {
		try {
			TomahawkRegistry.computeItemHandlers();
		} catch (Throwable e) {
			FMLCommonHandler.instance().raiseException(e, "An error occurred while computing item handlers", true);
		}
		try {
			TomahawkRegistry.sanityCheckHandlers();
		} catch (Throwable e) {
			FMLCommonHandler.instance().raiseException(e, "A problem within item handlers has been discovered", true);
		}
	}
	
	private static final void onServerStart() {
		itemBlacklist = Sets.newIdentityHashSet();
		itemBlacklist.addAll(Config.current().itemBlacklist.asItems().get());
	}
	
	private static final void onServerStop() {
		itemBlacklist = null;
	}
	
}
