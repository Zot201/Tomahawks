package zotmc.tomahawk.api;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.api.Launchable.Category;
import zotmc.tomahawk.api.Launchable.Condition;
import zotmc.tomahawk.api.Launchable.ConfigState;
import zotmc.tomahawk.api.Launchable.DispenseFactory;
import zotmc.tomahawk.api.Launchable.GoldenFusion;
import zotmc.tomahawk.api.Launchable.HitSound;
import zotmc.tomahawk.api.Launchable.InitialSpeed;
import zotmc.tomahawk.api.Launchable.LaunchFactory;
import zotmc.tomahawk.api.Launchable.Replica;
import zotmc.tomahawk.api.Launchable.Usage;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * Never implement this interface directly, or you will have to update your mod whenever new methods are added.<br/>
 * <br/>
 * Instead, implement desired methods with the annotations provided in a plain object.
 * Another choice is to implement this interface with a {@link Proxy}. In this case, please delegate methods either
 * unknown or unconcerned to {@link WeaponCategory}.
 * 
 * @see Launchable
 */
public interface ItemHandler {
	
	@Category public WeaponCategory category();
	
	@ConfigState public boolean isEnabled();
	
	@Condition public boolean isLaunchable(ItemStack item);
	
	@LaunchFactory public Entity createProjectile(WeaponLaunchEvent event);
	
	@DispenseFactory public Entity createDispenserProjectile(WeaponDispenseEvent event);
	
	@InitialSpeed public float getInitialSpeed(ItemStack item);
	
	@HitSound public SoundType getHitSound(ItemStack item);
	
	@Replica public boolean isReplicable(ItemStack item, boolean atEnchantmentTable);
	
	@GoldenFusion public boolean inheritGoldenSword(ItemStack item);
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" }) // manually checked
	static final Map<Class<? extends Annotation>, Method> ANNOTATION_MAP = Maps.toMap(
			(Iterable) Utils.filter(Launchable.class.getDeclaredClasses(),
					new Predicate<Class<?>>() { public boolean apply(Class<?> input) {
						return input != Usage.class && Modifier.isPublic(input.getModifiers());
					}}
			),
			new Function<Class<? extends Annotation>, Method>() { public Method apply(Class<? extends Annotation> input) {
				Usage u = input.getAnnotation(Usage.class);
				Method m = Utils.getDeclaredMethod(ItemHandler.class,
						u.ref(), Arrays.copyOfRange(u.desc(), 1, u.desc().length));
				checkArgument(m.getReturnType() == u.desc()[0]);
				return m;
			}}
	);
	
}
