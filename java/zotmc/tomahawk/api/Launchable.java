package zotmc.tomahawk.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Launchable {
	
	public WeaponCategory value();
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "category", desc = WeaponCategory.class)
	public @interface Category { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "isEnabled", desc = boolean.class)
	public @interface ConfigState { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "isLaunchable", desc = {boolean.class, ItemStack.class})
	public @interface Condition { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "createProjectile", desc = {Entity.class, WeaponLaunchEvent.class})
	public @interface LaunchFactory { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "createDispenserProjectile", desc = {Entity.class, WeaponDispenseEvent.class})
	public @interface DispenseFactory { }

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "getInitialSpeed", desc = {float.class, ItemStack.class})
	public @interface InitialSpeed { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "getHitSound", desc = {SoundType.class, ItemStack.class})
	public @interface HitSound { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "isReplicable", desc = {boolean.class, ItemStack.class, boolean.class})
	public @interface Replica { }

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@Usage(ref = "inheritGoldenSword", desc = {boolean.class, ItemStack.class})
	public @interface GoldenFusion { }
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.ANNOTATION_TYPE)
	public @interface Usage {
		/**
		 * @return the corresponding method name in {@link ItemHandler}
		 */
		String ref();
		
		/**
		 * @return the method return type and parameter types required
		 */
		Class<?>[] desc();
	}
	
}
