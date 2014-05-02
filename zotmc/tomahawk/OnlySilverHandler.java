package zotmc.tomahawk;

import static zotmc.onlysilver.api.OnlySilverAPI.silverAxe;
import static zotmc.onlysilver.api.OnlySilverAPI.silverSword;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import zotmc.onlysilver.api.OnlySilverRegistry;
import zotmc.onlysilver.api.OnlySilverRegistry.InUseWeapon;
import zotmc.tomahawk.projectile.EntityTomahawk;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class OnlySilverHandler {
	
	static void register() {
		TomahawkRegistry.addEquipManipulation(EntityZombie.class, silverSword.get(), silverAxe.get());
		TomahawkRegistry.addEquipManipulation(EntityZombie.class, silverAxe.get(), silverAxe.get());
		
		TomahawkRegistry.addEquipManipulation(EntitySkeleton.class, silverSword.get(), silverAxe.get());
		
		
		OnlySilverRegistry.registerWeaponFunction("thrown",
				new Function<DamageSource, InUseWeapon>() {
			
			@Override public InUseWeapon apply(DamageSource input) {
				Entity projectile = input.getSourceOfDamage();
				
				if (projectile instanceof EntityTomahawk) {
					final EntityTomahawk hawk = (EntityTomahawk) projectile;
					
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
							
							if (item != hawk.getItem())
								hawk.setItem(item);
							
						}
						
						@Override public String toString() {
							return String.format("[Tomahawk %s thrown by %s]",
									getItem().orNull(), getUser().orNull());
						}
					};
				}
				
				return null;
			}
		});
		
	}

}
