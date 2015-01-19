package zotmc.tomahawk.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import zotmc.tomahawk.data.ModData.OnlySilver;
import zotmc.tomahawk.projectile.EntityTomahawk;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.Reflection;

class OnlySilverHandler implements Function<DamageSource, Object> {
	
	static void init() throws Throwable {
		Class.forName(OnlySilver.ONLY_SILVER_REGISTRY)
			.getMethod(OnlySilver.REGISTER_WEAPON_FUNCTION, String.class, Function.class)
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
	
	private final List<String> switchMap = ImmutableList.of("getUser", "getItem", "update", "toString");
	
	private final Object newInUseWeapon(final EntityTomahawk hawk) {
		return Reflection.newProxy(
				inUseWeapon,
				new InvocationHandler() {
					@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						switch(switchMap.indexOf(method.getName())) {
						case 0:
							return getUser(hawk);
							
						case 1:
							return getItem(hawk);
							
						case 2:
							return update(hawk, (ItemStack) args[0]);
							
						case 3:
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
	
	private Void update(EntityTomahawk hawk, ItemStack item) {
		if (item == null) {
			hawk.setDead();
			hawk.playSound("random.break", 0.8F, 0.8F + hawk.worldObj.rand.nextFloat() * 0.4F);
		}
		
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
