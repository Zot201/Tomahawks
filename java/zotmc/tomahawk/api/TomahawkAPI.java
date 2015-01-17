package zotmc.tomahawk.api;

import java.util.Set;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.core.TomahawksCore.LoadComplete;
import zotmc.tomahawk.core.TomahawksCore.ServerAboutToStart;
import zotmc.tomahawk.core.TomahawksCore.ServerStopping;
import zotmc.tomahawk.util.Feature;
import zotmc.tomahawk.util.Reserve;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class TomahawkAPI {
	
	public static final Feature<Enchantment> replica = Reserve.absent();
	
	private static Set<Item> itemBlacklist;
	
	private TomahawkAPI() { }
	
	
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
	
	public static boolean isItemBlacklisted(Item item) {
		return (itemBlacklist != null ? itemBlacklist : Config.current().itemBlacklist.asItems().get()).contains(item);
	}
	
	
	@SubscribeEvent public void onAvailable(LoadComplete event) {
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
	
	@SubscribeEvent public void onServerStart(ServerAboutToStart event) {
		itemBlacklist = Sets.newIdentityHashSet();
		itemBlacklist.addAll(Config.current().itemBlacklist.asItems().get());
	}
	
	@SubscribeEvent public void onServerStop(ServerStopping event) {
		itemBlacklist = null;
	}
	
}
