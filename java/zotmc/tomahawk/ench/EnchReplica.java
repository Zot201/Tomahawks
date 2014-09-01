package zotmc.tomahawk.ench;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.api.TomahawkRegistry;

public class EnchReplica extends Enchantment {

	public EnchReplica(int id) {
		super(id, 1, EnumEnchantmentType.breakable);
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
	
	@Override public boolean canApply(ItemStack item) {
		return super.canApply(item) && TomahawkRegistry.getItemHandler(item).isReplicable(item, false);
	}
	
	@Override public boolean canApplyAtEnchantingTable(ItemStack item) {
		return super.canApply(item) && TomahawkRegistry.getItemHandler(item).isReplicable(item, true);
	}

}
