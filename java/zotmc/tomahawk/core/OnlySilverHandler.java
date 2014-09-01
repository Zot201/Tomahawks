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

class OnlySilverHandler implements Function<DamageSource, Object> {
	
	static void init() throws Throwable {
		Class.forName(OnlySilver.ONLY_SILVER_REGISTRY)
			.getMethod(OnlySilver.REGISTER_WEAPON_FUNCTION, DamageSource.class, Function.class)
			.invoke(null, "thrown", new OnlySilverHandler());
	}
	
	
	private final Class<?> inUseWeapon;
	
	private OnlySilverHandler() throws Throwable {
		inUseWeapon = Class.forName(OnlySilver.IN_USE_WEAPON);
	}
	
	@Override public Object apply(DamageSource input) {
		Entity projectile = input.getSourceOfDamage();
		
		return !(projectile instanceof EntityTomahawk) ? null
				: newInUseWeapon((EntityTomahawk) projectile);
	}
	
	private final Object newInUseWeapon(final EntityTomahawk hawk) {
		return Reflection.newProxy(
				inUseWeapon,
				new InvocationHandler() {
					@Override public Object invoke(Object proxy, Method method, Object[] args)
							throws Throwable {
						switch(method.getName()) {
						case "getUser":
							return getUser(hawk);
							
						case "getItem":
							return getItem(hawk);
							
						case "update":
							update(hawk, (ItemStack) args[0]);
							return null;
							
						case "toString":
							return OnlySilverHandler.this.toString(hawk);
							
						default:
							throw new IllegalArgumentException();
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
	
	private void update(EntityTomahawk hawk, ItemStack item) {
		if (item == null) {
			hawk.setDead();
			hawk.playSound("random.break", 0.8F, 0.8F + hawk.worldObj.rand.nextFloat() * 0.4F);
		}
		
		if (item != hawk.item.get())
			hawk.item.set(item);
	}
	
	private String toString(EntityTomahawk hawk) {
		return String.format("[Tomahawk %s thrown by %s]",
				getItem(hawk).orNull(), getUser(hawk).orNull()
		);
	}
	
}
