package zotmc.tomahawk.ench;

import static zotmc.tomahawk.api.ItemHandler.EnchantmentAction.REPLICA;
import static zotmc.tomahawk.api.ItemHandler.EnchantmentAction.REPLICA_ON_BOOK;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.api.TomahawkRegistry;

public class EnchReplica extends Enchantment {

	public EnchReplica(int id) {
		super(id, 1, EnumEnchantmentType.all);
	}
	
	@Override public int getMaxEnchantability(int lvl) {
		return 15;
	}
	
	@Override public int getMinEnchantability(int lvl) {
		return super.getMinEnchantability(lvl) + 50;
	}
	
	@Override public int getMaxLevel() {
		return 1;
	}
	
	private boolean isBreakable(ItemStack item) {
		Item i = item.getItem();
		return i != null && i.isDamageable();
	}
	
	@Override public boolean canApply(ItemStack item) {
		return isBreakable(item) && TomahawkRegistry.getItemHandler(item).isEnchantable(item, REPLICA);
	}
	
	@Override public boolean canApplyAtEnchantingTable(ItemStack item) {
		return isBreakable(item) && TomahawkRegistry.getItemHandler(item).isEnchantable(item, REPLICA_ON_BOOK);
	}

}
