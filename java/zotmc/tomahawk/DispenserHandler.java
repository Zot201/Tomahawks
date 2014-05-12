package zotmc.tomahawk;

import static zotmc.tomahawk.util.Obfs.findFieldFinal;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.RegistrySimple;
import net.minecraft.world.World;
import zotmc.tomahawk.projectile.EntityTomahawk;
import zotmc.tomahawk.util.FallbackingMap;
import zotmc.tomahawk.util.FieldAccess;

import com.google.common.base.Function;

public class DispenserHandler {
	
	static void init() {
		
		final FieldAccess<Map<Item, IBehaviorDispenseItem>> registryObjects = FieldAccess.of(
				findFieldFinal(RegistrySimple.class, "registryObjects", "field_82596_a"),
				BlockDispenser.dispenseBehaviorRegistry);
		
		
		registryObjects.set(new FallbackingMap<Item, IBehaviorDispenseItem>() {
			final Map<Item, IBehaviorDispenseItem> delegatee = registryObjects.get();
			
			final Function<Item, IBehaviorDispenseItem> function =
					new Function<Item, IBehaviorDispenseItem>() {
				final IBehaviorDispenseItem shootTomahawk = new BehaviorProjectileDispense() {
					
					final Random rand = new Random();
					
					@Override protected IProjectile getProjectileEntity(
							World world, IPosition pos) {
						return null;
					}
					
					@Override public ItemStack dispenseStack(
							IBlockSource blockSrc,
							ItemStack item) {
						
				        World world = blockSrc.getWorld();
				        IPosition pos = BlockDispenser.func_149939_a(blockSrc);
				        EnumFacing facing =
				        		BlockDispenser.func_149937_b(blockSrc.getBlockMetadata());
				        
				        EntityTomahawk hawk = new EntityTomahawk(
				        		world, pos.getX(), pos.getY(), pos.getZ(), item.splitStack(1));
				        
				        hawk.setThrowableHeading(
				        		facing.getFrontOffsetX(),
				        		facing.getFrontOffsetY() + 0.1F,
				        		facing.getFrontOffsetZ(),
				        		func_82500_b(), func_82498_a());
				        
				        hawk.setIsForwardSpin(rand.nextBoolean());
				        
				        world.spawnEntityInWorld(hawk);
				        
				        return item;
					}
				};
				
				@Override public IBehaviorDispenseItem apply(Item input) {
					if (TomahawkRegistry.isThrowableAxe(input))
						return shootTomahawk;
					return null;
				}
				
			};
			
			@Override protected Map<Item, IBehaviorDispenseItem> delegate() {
				return delegatee;
			}
			@Override protected Function<Item, IBehaviorDispenseItem> function() {
				return function;
			}
			
		});
		
	}

}
