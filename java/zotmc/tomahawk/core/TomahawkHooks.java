package zotmc.tomahawk.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import zotmc.tomahawk.api.PickUpType;
import zotmc.tomahawk.api.TomahawkAPI;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.api.WeaponLaunchEvent;
import zotmc.tomahawk.util.Utils;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class TomahawkHooks {
	
	public static final ThreadLocal<PlayerInteractEvent> interactEvent = new ThreadLocal<>();
	private static final ThreadLocal<PlayerInteractEvent> interactEventActivating = new ThreadLocal<>();
	
	static boolean isLaunchable(PlayerInteractEvent event) {
		EntityPlayer player = event.entityPlayer;
		ItemStack item = player.getHeldItem();
		return item != null && item.stackSize > 0 && TomahawkAPI.isLaunchable(item);
	}
	
	static void activateTomahawk(PlayerInteractEvent event) {
		EntityPlayer player = event.entityPlayer;
		PlayerTracker tracker = PlayerTracker.get(player);
		
		if (tracker.getAfterInteract() != 0) {
			ItemStack item = player.getHeldItem();
			WeaponLaunchEvent launchEvent =
					new WeaponLaunchEvent(player, Utils.itemStack(item, 1), TomahawkRegistry.getItemHandler(item));
			
			boolean replica = !player.isSneaking() && Utils.getEnchLevel(TomahawkAPI.replica.get(), item) > 0;
			boolean creative = player.capabilities.isCreativeMode;
			
			if (replica) {
				launchEvent.setPickUpType(PickUpType.ENCH);
				launchEvent.isFragile = true;
			}
			else if (creative)
				launchEvent.setPickUpType(PickUpType.CREATIVE);
			
			if (launchEvent.run()) {
				if (!creative) {
					if (replica)
						item.damageItem(2, player);
					else
						item.stackSize--;
					
					if (item.stackSize == 0)
						player.setCurrentItemOrArmor(0, null);
				}
				
				event.useBlock = Event.Result.DENY;
				event.useItem = Event.Result.DENY;
				tracker.onInteract();
			}
		}
	}
	
	public static boolean isLaunchable() {
		PlayerInteractEvent event = interactEvent.get();
		
		if (event != null) {
			interactEvent.set(null);
			
			if (event.useItem != Result.DENY && isLaunchable(event)) {
				interactEventActivating.set(event);
				return true;
			}
		}
		return false;
	}
	
	public static boolean activateTomahawk(World world, float hitX, float hitY, float hitZ) {
		PlayerInteractEvent event = interactEventActivating.get();
		
		if (event != null) {
			interactEventActivating.set(null);
			activateTomahawk(event);
			boolean ret = event.useItem == Result.DENY;
			EntityPlayer player = event.entityPlayer;
			ItemStack item = player.getHeldItem();
			
			if (!ret && item != null && item.stackSize > 0) {
				Item i = item.getItem();
				ret = i != null && i.onItemUseFirst(item, player, world, event.x, event.y, event.z, event.face, hitX, hitY, hitZ);
			}
			
			return ret;
		}
		
		return false;
	}

}
