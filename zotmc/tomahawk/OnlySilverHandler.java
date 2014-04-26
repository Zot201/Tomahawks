package zotmc.tomahawk;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import zotmc.onlysilver.api.OnlySilverRegistry;
import zotmc.onlysilver.api.OnlySilverRegistry.InUseWeapon;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class OnlySilverHandler {
	
	public static void registerWeaponFunction() {
		OnlySilverRegistry.registerWeaponFunction("thrown",
				new Function<DamageSource, InUseWeapon>() {
			
			@Override public InUseWeapon apply(DamageSource input) {
				Entity indirectEntity = input.getEntity();
				
				if (indirectEntity instanceof EntityTomahawk) {
					final EntityTomahawk hawk = (EntityTomahawk) indirectEntity;
					
					return new InUseWeapon() {
						@Override public Optional<EntityLivingBase> getUser() {
							Entity thrower = hawk.getThrower();
							return Optional.fromNullable(
									thrower instanceof EntityLivingBase ?
											(EntityLivingBase) thrower : null);
						}
						
						@Override public Optional<ItemStack> getItem() {
							return Optional.fromNullable(hawk.getItem());
						}
						
						@Override public void update(ItemStack item) {
							if (item == null)
								hawk.setDead();
						}
					};
				}
				
				return null;
			}
		});
		
	}

}
