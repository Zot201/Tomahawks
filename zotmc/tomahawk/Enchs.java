package zotmc.tomahawk;

import static zotmc.tomahawk.Tomahawk.findField;
import static zotmc.tomahawk.Tomahawk.findMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class Enchs {
	
	public static float getEnchantmentModifierLiving(ItemStack item, EntityLivingBase victim) {
		livingModifier.set(0.0F);
		entityLiving.set(victim);
		applyEnchantmentModifier(emlAccess.get(), item);
		return livingModifier.get();
	}
	
	public static void applyEnchantmentDamageIterator(EntityLivingBase user, ItemStack item, Entity victim) {
		ediUser.set(user);
		ediVictim.set(victim);
		applyEnchantmentModifier(ediAccess.get(), item);
	}
	
	private static void applyEnchantmentModifier(Object imObj, ItemStack item) {
		try {
			applyEnchantmentModifier.invoke(null, imObj, item);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private static final FieldAccess<Object> emlAccess;
	private static final FieldAccess<Float> livingModifier;
	private static final FieldAccess<EntityLivingBase> entityLiving;

	private static final FieldAccess<Object> ediAccess;
	private static final FieldAccess<EntityLivingBase> ediUser;
	private static final FieldAccess<Entity> ediVictim;
	
	private static final Method applyEnchantmentModifier;
	
	static {
		Class<?> mlClz = null, diClz = null, imClz = null;
		try {
			mlClz = Class.forName("net.minecraft.enchantment.EnchantmentHelper$ModifierLiving");
			diClz = Class.forName("net.minecraft.enchantment.EnchantmentHelper$DamageIterator");
			imClz = Class.forName("net.minecraft.enchantment.EnchantmentHelper$IModifier");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		emlAccess = FieldAccess
				.of(findField(EnchantmentHelper.class, "enchantmentModifierLiving", "field_77521_c"), null);
		livingModifier = FieldAccess
				.nesting(findField(mlClz, "livingModifier", "field_77495_a"), emlAccess);
		entityLiving = FieldAccess
				.nesting(findField(mlClz, "entityLiving", "field_77494_b"), emlAccess);
		
		ediAccess = FieldAccess
				.of(findField(EnchantmentHelper.class, "field_151389_e"), null);
		ediUser = FieldAccess
				.nesting(findField(diClz, "field_151366_a"), ediAccess);
		ediVictim = FieldAccess
				.nesting(findField(diClz, "field_151365_b"), ediAccess);
		
		
		applyEnchantmentModifier =
				findMethod(EnchantmentHelper.class, "applyEnchantmentModifier", "func_77518_a")
					.withArgs(imClz, ItemStack.class);
		
	}

}
