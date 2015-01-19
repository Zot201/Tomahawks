package zotmc.tomahawk.api;

import java.util.Random;

import net.minecraft.block.StepSound;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.data.ModData.AxeTomahawk;
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
	
	
	private static final Random rand = new Random();
	private static final StepSound inAirSound = new StepSound(AxeTomahawk.DOMAIN + ":random.tomahawk", 1, 1) {
		@Override public float getPitch() {
			return 1 / (rand.nextFloat() * 0.4F + 1.2F) + 0.5F;
		}
	};
	private static final StepSound hitSound = new StepSound(AxeTomahawk.DOMAIN + ":random.tomahawk_hit", 1, 1) {
		@Override public float getPitch() {
			return 1.2F / (rand.nextFloat() * 0.2F + 0.9F) - 0.5F;
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
	@Override public StepSound getSound(ItemStack item, PlaybackType type) {
		switch (type) {
		case LAUNCH:
		case IN_AIR:
			return inAirSound;
			
		case HIT_BLOCK_WEAK:
			return hitSound;
			
		default:
			return null;
		}
	}
	@Override public boolean isEnchantable(ItemStack item, EnchantmentAction action) {
		return action != EnchantmentAction.REPLICA && TomahawkAPI.isLaunchable(item);
	}
	
}
