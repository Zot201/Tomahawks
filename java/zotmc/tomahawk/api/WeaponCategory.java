package zotmc.tomahawk.api;

import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.projectile.EntityTomahawk;

/**
 * This class provides a rough categorization as well as base implementations.
 */
public enum WeaponCategory implements ItemHandler {
	DISABLED {
		@Override public boolean isEnabled() {
			return false;
		}
	},
	AXE {
		@Override public Entity createProjectile(WeaponLaunchEvent event) {
			return new EntityTomahawk(event);
		}
		@Override public Entity createDispenserProjectile(WeaponDispenseEvent event) {
			return new EntityTomahawk(event);
		}
	},
	FRYPAN {
		@Override public Entity createProjectile(WeaponLaunchEvent event) {
			return new EntityTomahawk(event);
		}
		@Override public Entity createDispenserProjectile(WeaponDispenseEvent event) {
			return new EntityTomahawk(event);
		}
	},
	HAMMER { //TODO
		@Override public Entity createProjectile(WeaponLaunchEvent event) {
			return new EntityTomahawk(event);
		}
		@Override public Entity createDispenserProjectile(WeaponDispenseEvent event) {
			return new EntityTomahawk(event);
		}
	};
	
	@Override public WeaponCategory category() {
		return this;
	}
	@Override public boolean isEnabled() {
		return true;
	}
	@Override public boolean isLaunchable(ItemStack item) {
		return true;
	}
	@Override public Entity createProjectile(WeaponLaunchEvent event) {
		throw new UnsupportedOperationException(toString());
	}
	@Override public Entity createDispenserProjectile(WeaponDispenseEvent event) {
		throw new UnsupportedOperationException(toString());
	}
	@Override public float getInitialSpeed(ItemStack item) {
		return 2.11F;
	}
	@Override public SoundType getHitSound(ItemStack item) {
		return null;
	}
	@Override public boolean isReplicable(ItemStack item, boolean atEnchantmentTable) {
		return !atEnchantmentTable && isEnabled() && isLaunchable(item)
				&& !TomahawkAPI.isItemBlacklisted(item);
	}
	@Override public boolean inheritGoldenSword(ItemStack item) {
		return isReplicable(item, false);
	}
	
}
