package zotmc.tomahawk.core;

import java.util.Map;
import java.util.Random;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zotmc.tomahawk.api.PickUpType;
import zotmc.tomahawk.api.TomahawkAPI;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.api.WeaponDispenseEvent;
import zotmc.tomahawk.data.ReflData;
import zotmc.tomahawk.util.FallbackingMap;
import zotmc.tomahawk.util.Fields;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.prop.Prop;

import com.google.common.base.Function;
import com.google.common.reflect.TypeToken;

class DispenserHandler extends FallbackingMap<Item, IBehaviorDispenseItem> {
	
	static void init() {
		final Prop<Map<Item, IBehaviorDispenseItem>> registryObjects = Fields
				.referTo(BlockDispenser.dispenseBehaviorRegistry, Fields.definalize(ReflData.REGISTRY_OBJECTS))
				.ofType(new TypeToken<Map<Item, IBehaviorDispenseItem>>() { });
		
		registryObjects.set(new DispenserHandler(registryObjects.get()));
	}
	
	
	private final Random rand = new Random();
	private final Map<Item, IBehaviorDispenseItem> delegatee;
	
	private DispenserHandler(Map<Item, IBehaviorDispenseItem> delegatee) {
		this.delegatee = delegatee;
	}
	
	@Override protected Map<Item, IBehaviorDispenseItem> delegate() {
		return delegatee;
	}
	@Override protected Function<Item, IBehaviorDispenseItem> function() {
		return function;
	}
	
	private final Function<Item, IBehaviorDispenseItem> function = new Function<Item, IBehaviorDispenseItem>() {
		@Override public IBehaviorDispenseItem apply(Item input) {
			return input != null && TomahawkRegistry.getItemHandler(input).isEnabled() ? dispenseWeapon : null;
		}
	};
	
	private final IBehaviorDispenseItem dispenseWeapon = new BehaviorProjectileDispense() {
		@Override protected IProjectile getProjectileEntity(World world, IPosition pos) {
			return null;
		}
		
		@Override public ItemStack dispenseStack(IBlockSource blockSrc, ItemStack item) {
			if (item != null && item.stackSize > 0 && TomahawkAPI.isLaunchable(item)) {
				WeaponDispenseEvent event = new WeaponDispenseEvent(
						blockSrc,
						BlockDispenser.func_149939_a(blockSrc),
						BlockDispenser.func_149937_b(blockSrc.getBlockMetadata()),
						Utils.itemStack(item, 1),
						TomahawkRegistry.getItemHandler(item)
				);
				
				event.isForwardSpin = rand.nextBoolean();
				
				boolean replica = Utils.getEnchLevel(TomahawkAPI.replica.get(), item) > 0;
				
				if (replica) {
					event.setPickUpType(PickUpType.ENCH);
					event.isFragile = true;
				}
				
				if (event.run() && (!replica || item.attemptDamageItem(2, rand)))
					item.stackSize --;
			}
			
			return item;
		}
	};

}
