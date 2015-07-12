package zotmc.tomahawk.ench;

import static zotmc.tomahawk.api.ItemHandler.EnchantmentAction.REPLICA;
import static zotmc.tomahawk.api.ItemHandler.EnchantmentAction.REPLICA_ON_BOOK;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.api.TomahawkRegistry;

public class EnchReplica extends Enchantment {

	public EnchReplica(int id) {
		super(id, 1, EnumEnchantmentType.breakable);
	}
	
	@Override public int getMinEnchantability(int lvl) {
		return 15;
	}
	
	@Override public int getMaxEnchantability(int lvl) {
		return super.getMinEnchantability(lvl) + 50;
	}
	
	@Override public int getMaxLevel() {
		return 1;
	}
	
	private boolean isType(ItemStack item) {
		return type.canEnchantItem(item.getItem());
	}
	
	/**
	 * Secondary enchantments implementation.
	 * Positive result indicate that Replica is a valid enchantment in most cases
	 * (except enchanting table, which depends on canApplyAtEnchantingTable).
	 */
	@Override public boolean canApply(ItemStack item) {
		return isType(item) && TomahawkRegistry.getItemHandler(item).isEnchantable(item, REPLICA_ON_BOOK);
	}
	
	/**
	 * Primary enchantments implementation.
	 * Positive results indicate that Replica can be applied directly through enchanting table.
	 */
	@Override public boolean canApplyAtEnchantingTable(ItemStack item) {
		return isType(item) && TomahawkRegistry.getItemHandler(item).isEnchantable(item, REPLICA);
	}

}
