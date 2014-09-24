package zotmc.tomahawk.data;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.util.Refls.findClass;

import java.lang.reflect.Field;

import net.minecraft.block.BlockDispenser;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.RegistrySimple;
import zotmc.tomahawk.util.Fields;
import zotmc.tomahawk.util.Fields.FieldAccess;
import zotmc.tomahawk.util.Refls;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.prop.Prop;

import com.google.common.base.Optional;
import com.google.common.reflect.Invokable;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.ASMEventHandler;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.relauncher.SideOnly;

public class ReflData {
	
	public static class Bootstraps {
		public static final Field MC_VERSION = Refls.findField(Loader.class, "MC_VERSION", "mccversion"); // -, 172
	}
	
	@SideOnly(CLIENT)
	public static class ClientRefls {
		public static final Optional<SoundManagers>
		soundManagers = Utils.constructIf(Utils.MC_VERSION.isBelow("1.7.2"), SoundManagers.class);
		
		public static class SoundManagers {
			public final Invokable<SoundManager, Void>
			addSound = Refls.findMethod(SoundManager.class, "addSound", "func_77372_a")
				.asInvokable(String.class)
				.returning(void.class);
		}
	}
	
	public static class EntityArrows {
		public static final Field
		X_TILE = Refls.findField(EntityArrow.class, "field_145791_d", "xTile", "field_70247_d"), // -, 164, 164
		Y_TILE = Refls.findField(EntityArrow.class, "field_145792_e", "yTile", "field_70248_e"), // -, 164, 164
		Z_TILE = Refls.findField(EntityArrow.class, "field_145789_f", "zTile", "field_70245_f"), // -, 164, 164
		TICKS_IN_GROUND = Refls.findField(EntityArrow.class, "ticksInGround", "field_70252_j"),
		TICKS_IN_AIR = Refls.findField(EntityArrow.class, "ticksInAir", "field_70257_an");
	}
	
	public static class EnchantmentHelpers {
		public static final Invokable<EnchantmentHelper, Void>
		applyEnchantmentModifier = Refls.findMethod(EnchantmentHelper.class, "applyEnchantmentModifier", "func_77518_a")
			.asInvokable(findClass(
					"net.minecraft.enchantment.EnchantmentHelper$IModifier",
					"net.minecraft.enchantment.IEnchantmentModifier" // 164
			), ItemStack.class)
			.returning(void.class);
		
		
		public static final Optional<ModifierLivings>
		modifierLivings = Utils.constructIf(Utils.MC_VERSION.isBelow("1.7.10"), ModifierLivings.class);
		
		public static final Optional<DamageIterators>
		damageIterators = Utils.constructIf(Utils.MC_VERSION.isAtLeast("1.7.2"), DamageIterators.class);
		
		public static class ModifierLivings {
			public final FieldAccess<?>
			instance = Fields.referTo(EnchantmentHelper.class, "enchantmentModifierLiving", "field_77521_c");
			
			private final Class<?>
			type = Refls.findClass(
					"net.minecraft.enchantment.EnchantmentHelper$ModifierLiving", // 172
					"net.minecraft.enchantment.EnchantmentModifierLiving" // 164
			);
			
			public final Prop<EntityLivingBase>
			victim = instance.downTo(type, "entityLiving", "field_77494_b").ofType(EntityLivingBase.class);
			
			public final Prop<Float>
			result = instance.downTo(type, "livingModifier", "field_77495_a").ofType(float.class);
		}
		
		public static class DamageIterators {
			public final FieldAccess<?>
			instance = Fields.referTo(EnchantmentHelper.class, "field_151389_e");
			
			private final Class<?>
			type = Refls.findClass("net.minecraft.enchantment.EnchantmentHelper$DamageIterator");
			
			public final Prop<EntityLivingBase>
			user = instance.downTo(type, "field_151366_a").ofType(EntityLivingBase.class);
			
			public final Prop<Entity>
			victim = instance.downTo(type, "field_151365_b").ofType(Entity.class);
		}
	}
	
	public static final String
	INSTANCE = "instance";
	
	public static final Field
	BUS_ID = Utils.getDeclaredField(EventBus.class, "busID"),
	HANDLER = Utils.getDeclaredField(ASMEventHandler.class, "handler"),
	REGISTRY_OBJECTS = Refls.findField(RegistrySimple.class, "registryObjects", "field_82596_a"),
	DISPENSE_BEHAVIOR_REGISTRY =
			Refls.findField(BlockDispenser.class, "dispenseBehaviorRegistry", "field_149943_a", "field_82527_a"), // -, -, 164
	SIN_TABLE = Refls.findField(MathHelper.class, "SIN_TABLE", "field_76144_a");
	
	public static final Optional<Field>
	OWNER = Utils.getDeclaredFieldIf(Utils.MC_VERSION.isAtLeast("1.7.2"), ASMEventHandler.class, "owner");
	
}
