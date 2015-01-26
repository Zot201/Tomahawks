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
import net.minecraft.util.IRegistry;
import net.minecraft.util.RegistryDefaulted;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

class DispenserHandler extends FallbackingMap<Item, IBehaviorDispenseItem> {
	
	static void init() {
		if (BlockDispenser.dispenseBehaviorRegistry.getClass() == RegistryDefaulted.class) {
			Prop<Map<Item, IBehaviorDispenseItem>> registryObjects = Fields
					.referTo(BlockDispenser.dispenseBehaviorRegistry, Fields.definalize(ReflData.REGISTRY_OBJECTS))
					.ofType(new TypeToken<Map<Item, IBehaviorDispenseItem>>() { });
			
			registryObjects.set(new DispenserHandler(registryObjects.get()));
			
			LogTomahawk.api4j().debug("Handled dispense behavior by wrapping %s", ReflData.REGISTRY_OBJECTS);
		}
		else {
			Prop<IRegistry> dispenseBehaviorRegistry = Fields
					.referTo(BlockDispenser.class, Fields.definalize(ReflData.DISPENSE_BEHAVIOR_REGISTRY))
					.ofType(IRegistry.class);
			
			final IRegistry backing = dispenseBehaviorRegistry.get();
			final DispenserHandler handler = new DispenserHandler(ImmutableMap.<Item, IBehaviorDispenseItem>of());
			
			dispenseBehaviorRegistry.set(new IRegistry() {
				@Override public void putObject(Object key, Object value) {
					backing.putObject(key, value);
				}
				
				@Override public Object getObject(Object key) {
					Object ret = backing.getObject(key);
					return ret != null ? ret : handler.fallback(key);
				}
			});
			
			LogTomahawk.api4j().debug("Handled dispense behavior by wrapping %s", ReflData.DISPENSE_BEHAVIOR_REGISTRY);
		}
	}
	
	
	private final Random rand = new Random();
	private final Map<Item, IBehaviorDispenseItem> delegatee;
	
	private DispenserHandler(Map<Item, IBehaviorDispenseItem> delegatee) {
		this.delegatee = delegatee;
	}
	
	@Override protected Map<Item, IBehaviorDispenseItem> delegate() {
		return delegatee;
	}
	
	@Override protected IBehaviorDispenseItem fallback(Object input) {
		return input instanceof Item && TomahawkRegistry.getItemHandler((Item) input).isEnabled() ? dispenseWeapon : null;
	}
	
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
				
				boolean replica = Utils.getEnchLevel(TomahawkAPI.replica, item) > 0;
				
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
