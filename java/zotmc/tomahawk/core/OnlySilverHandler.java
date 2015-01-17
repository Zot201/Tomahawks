package zotmc.tomahawk.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import zotmc.tomahawk.data.ModData.OnlySilver;
import zotmc.tomahawk.projectile.EntityTomahawk;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.reflect.Reflection;

class OnlySilverHandler<InUseWeapon> implements Function<DamageSource, InUseWeapon> {
	
	static void init() throws Throwable {
		Class.forName(OnlySilver.ONLY_SILVER_REGISTRY)
			.getMethod(OnlySilver.REGISTER_WEAPON_FUNCTION, String.class, Function.class)
			.invoke(null, "thrown", new OnlySilverHandler<>());
	}
	
	
	private final Class<InUseWeapon> inUseWeaponType;
	
	@SuppressWarnings("unchecked")
	private OnlySilverHandler() throws Throwable {
		inUseWeaponType = (Class<InUseWeapon>) Class.forName(OnlySilver.IN_USE_WEAPON);
	}
	
	@Override public InUseWeapon apply(DamageSource input) {
		Entity projectile = input.getSourceOfDamage();
		
		return !(projectile instanceof EntityTomahawk) ? null : newInUseWeapon((EntityTomahawk) projectile);
	}
	
	private final InUseWeapon newInUseWeapon(final EntityTomahawk hawk) {
		return Reflection.newProxy(
				inUseWeaponType,
				new InvocationHandler() {
					@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						switch(method.getName()) {
						case "getUser":
							return getUser(hawk);
							
						case "getItem":
							return getItem(hawk);
							
						case "update":
							return update(hawk, (ItemStack) args[0]);
							
						case "toString":
							return OnlySilverHandler.this.toString(hawk);
							
						default:
							throw new UnsupportedOperationException();
						}
					}
				}
		);
	}
	
	private Optional<EntityLivingBase> getUser(EntityTomahawk hawk) {
		return Utils.tryCast(EntityLivingBase.class, hawk.shootingEntity);
	}
	
	private Optional<ItemStack> getItem(EntityTomahawk hawk) {
		return Optional.fromNullable(hawk.item.get());
	}
	
	private Void update(EntityTomahawk hawk, ItemStack item) {
		if (item == null)
			hawk.onBroken();
		
		if (item != hawk.item.get())
			hawk.item.set(item);
		
		return null;
	}
	
	private String toString(EntityTomahawk hawk) {
		return String.format("[Tomahawk %s thrown by %s]",
				getItem(hawk).orNull(), getUser(hawk).orNull()
		);
	}
	
}
