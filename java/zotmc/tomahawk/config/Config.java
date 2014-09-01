package zotmc.tomahawk.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;
import zotmc.tomahawk.core.LogTomahawk.LogCategory;
import zotmc.tomahawk.util.Fields;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

public class Config {
	
	private static final String
	GENERAL = "general",
	ENCHS = "enchantments",
	COMPS = "compatibilities",
	DEBUG = "debug";
	
	@ApplyHot
	public final Configurable<Boolean>
	commonAxesThrowing = new ConfigurableBoolean(GENERAL, "commonAxesThrowing").set(true);
	@ApplyHot @Core
	public final ConfigurableItemIdSet
	itemBlacklist = new ConfigurableItemIdSet(GENERAL, "axeBlacklist");
	
	@ApplyHot @Core
	public final Configurable<Boolean>
	goldenFusion = new ConfigurableBoolean(ENCHS, "goldenFusion"),
	igniteFireRespect = new ConfigurableBoolean(ENCHS, "igniteFireRepect");
	@Core
	public final Configurable<Integer>
	replica = new ConfigurableInteger(ENCHS, "replica").set(143);
	
	@ApplyHot
	public final Configurable<Boolean>
	tiCHatchetsThrowing = new ConfigurableBoolean(COMPS, "tiCHatchetsThrowing").set(true),
	tiCLumerAxesThrowing = new ConfigurableBoolean(COMPS, "tiCLumberAxesThrowing").set(true),
	tiCFryingPansThrowing = new ConfigurableBoolean(COMPS, "tiCFryingPansThrowing")/*,
	tiCHammersThrowing = new ConfigurableBoolean(COMPS, "tiCHammersThrowing").set(true)*/;
	
	@ApplyHot @Core
	public final Configurable<Set<LogCategory>>
	debugLoggings = new ConfigurableEnumSet<>(LogCategory.class, DEBUG, "loggings");
	
	
	
	private static Configuration configFile;
	private static Config preserved, current;
	
	Config() { }
	
	public static void init(Configuration configFile) {
		if (Config.configFile != null)
			throw new IllegalStateException("Already initialized");
		
		Config.configFile = checkNotNull(configFile);
		
		current = new Config().load().save();
		
	}
	
	static Config preserved() {
		return preserved != null ? preserved : (preserved = current.copy());
	}
	
	public static Config current() {
		return current;
	}
	
	
	
	private static Iterable<Field> configurableFields() {
		return FluentIterable
				.from(Arrays.asList(Config.class.getDeclaredFields()))
				.filter(new Predicate<Field>() {
					@Override public boolean apply(Field input) {
						return !Modifier.isStatic(input.getModifiers())
								&& Configurable.class.isAssignableFrom(input.getType());
					}
				});
	}
	
	Config apply(Config config) {
		for (Field f : configurableFields())
			Fields.<Configurable<Object>>get(this, f).set(
					Fields.<Configurable<?>>get(config, f).get()
			);
		return this;
	}
	
	Config applyCore(Config config) {
		for (Field f : configurableFields())
			if (f.getAnnotation(Core.class) != null)
				Fields.<Configurable<Object>>get(this, f).set(
						Fields.<Configurable<?>>get(config, f).get()
				);
		return this;
	}
	
	Config applyNonCore(Config config) {
		for (Field f : configurableFields())
			if (f.getAnnotation(Core.class) == null)
				Fields.<Configurable<Object>>get(this, f).set(
						Fields.<Configurable<?>>get(config, f).get()
				);
		return this;
	}
	
	Config applyHot(Config config) {
		for (Field f : configurableFields())
			if (f.getAnnotation(ApplyHot.class) != null)
				Fields.<Configurable<Object>>get(this, f).set(
						Fields.<Configurable<?>>get(config, f).get()
				);
		return this;
	}
	
	Config load() {
		for (Field f : configurableFields())
			Fields.<Configurable<?>>get(this, f).load(configFile);
		return this;
	}
	
	Config save() {
		for (Field f : configurableFields())
			Fields.<Configurable<?>>get(this, f).save(configFile);
		return this;
	}
	
	Config copy() {
		return new Config().apply(this);
	}
	
	boolean nonCoreNonHotEqualTo(Config config) {
		for (Field f : configurableFields())
			if (f.getAnnotation(ApplyHot.class) == null && f.getAnnotation(Core.class) != null
					&& !Fields.get(this, f).equals(Fields.get(config, f)))
				return false;
		return true;
	}
	
	boolean coreNonHotEqualTo(Config config) {
		for (Field f : configurableFields())
			if (f.getAnnotation(ApplyHot.class) == null && f.getAnnotation(Core.class) != null
					&& !Fields.get(this, f).equals(Fields.get(config, f)))
				return false;
		return true;
	}
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private @interface ApplyHot { }
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private @interface Core { }
	
}
