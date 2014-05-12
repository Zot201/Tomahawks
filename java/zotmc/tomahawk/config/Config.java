package zotmc.tomahawk.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.Config.ConfigState.AVAILABLE;
import static zotmc.tomahawk.config.Config.ConfigState.INSPECTING;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import zotmc.tomahawk.LogTomahawk.LogCategory;
import zotmc.tomahawk.util.FieldAccess;
import zotmc.tomahawk.util.Holder;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import cpw.mods.fml.relauncher.SideOnly;

public class Config {
	
	private static final String
	GENERAL = "general",
	COMPS = "compatibilities";
	
	public final Configurable<Boolean>
	commonAxesThrowing = new ConfigurableBoolean(GENERAL, "commonAxesThrowing").set(true);
	public final ConfigurableItemIdSet
	axeBlacklist = new ConfigurableItemIdSet(GENERAL, "axeBlacklist");
	public final Configurable<Set<LogCategory>>
	debugLoggings = ConfigurableEnumSet.of(LogCategory.class, GENERAL, "debugLoggings");
	
	public final Configurable<Boolean>
	tiCHatchetsThrowing = new ConfigurableBoolean(COMPS, "tiCHatchetsThrowing").set(true),
	tiCFryingPansThrowing = new ConfigurableBoolean(COMPS, "tiCFryingPansThrowing");
	
	
	
	public enum ConfigState {
		INSPECTING,
		AVAILABLE;
	}
	
	Config() { }
	
	private static Configuration configFile;
	private static Holder<ConfigState> configState;
	
	private static Config preserved, current;
	private Config inspect;
	
	public static void init(Configuration configFile, Holder<ConfigState> configState) {
		if (Config.configFile != null)
			throw new IllegalStateException("Already initialized");
		
		Config.configFile = checkNotNull(configFile);
		Config.configState = checkNotNull(configState);
		
		current = new Config().load().save();
		configState.set(AVAILABLE);
		
	}
	
	static Config preserved() {
		return preserved != null ? preserved : (preserved = current.copy());
	}
	public static Config current() {
		return configState.get() == AVAILABLE ? current : current.inspect;
	}
	
	
	@SideOnly(CLIENT)
	void endInspect(GuiScreen parent) {
		inspect = null;
		configState.set(AVAILABLE);
		
		mc().displayGuiScreen(parent);
	}
	
	@SideOnly(CLIENT)
	Config beginInspect(Config config, GuiEdit gui) {
		if (configState.get() != AVAILABLE)
			throw new IllegalStateException("Illegal Config State: " + configState.get());
		configState.set(INSPECTING);
		inspect = config;
		
		mc().displayGuiScreen(gui);
		
		return this;
	}
	
	
	
	
	
	private static Iterable<Field> getConfigurableFields() {
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
		for (Field f : getConfigurableFields())
			FieldAccess
				.<Configurable<Object>>of(f, this)
				.get()
				.set(FieldAccess
						.<Configurable<?>>of(f, config)
						.get()
						.get());
		return this;
	}
	
	Config applyHot(Config config) {
		return apply(config); //TODO: stub
	}
	
	Config load() {
		for (Field f : getConfigurableFields())
			FieldAccess
				.<Configurable<?>>of(f, this)
				.get()
				.load(configFile);
		return this;
	}
	
	Config save() {
		for (Field f : getConfigurableFields())
			FieldAccess
				.<Configurable<?>>of(f, this)
				.get()
				.save(configFile);
		return this;
	}
	
	Config copy() {
		return new Config().apply(this);
	}
	
}
