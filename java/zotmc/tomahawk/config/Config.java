package zotmc.tomahawk.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.config.Configuration;
import zotmc.tomahawk.core.LogTomahawk.LogCategory;
import zotmc.tomahawk.core.TomahawksCore;
import zotmc.tomahawk.util.Fields;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Predicate;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.relauncher.Side;

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
	freeRetrieval = new ConfigurableBoolean(GENERAL, "freeRetrieval"),
	reduceEntityRestitution = new ConfigurableBoolean(GENERAL, "reduceEntityRestitution").set(true);
	
	@ApplyHot @Core
	public final Configurable<Boolean>
	goldenFusion = new ConfigurableBoolean(ENCHS, "goldenFusion"),
	igniteFireRespect = new ConfigurableBoolean(ENCHS, "igniteFireRepect").set(true);
	@Core
	public final Configurable<Integer>
	replica = new ConfigurableInteger(ENCHS, "replica").set(143);
	
	@ApplyHot
	public final Configurable<Boolean>
	tiCHatchetsThrowing = new ConfigurableBoolean(COMPS, "tiCHatchetsThrowing").set(true),
	tiCLumerAxesThrowing = new ConfigurableBoolean(COMPS, "tiCLumberAxesThrowing").set(true),
	tiCFryingPansThrowing = new ConfigurableBoolean(COMPS, "tiCFryingPansThrowing")/*,
	tiCHammersThrowing = new ConfigurableBoolean(COMPS, "tiCHammersThrowing").set(true)*/;
	
	@ApplyHot @Core @NoSync
	public final Configurable<Set<LogCategory>>
	debugLoggings = new ConfigurableEnumSet<>(LogCategory.class, DEBUG, "loggings");
	
	
	
	private static Configuration configFile;
	private static Config preserved, local, current;
	
	Config() { }
	
	public static void init(Configuration configFile) {
		checkState(Config.configFile == null, "Already initialized");
		Config.configFile = checkNotNull(configFile);
		preserved = new Config().load().save();
		local = preserved.copy();
		current = local;
		
		FMLCommonHandler.instance().bus().register(preserved);
		TomahawksCore.instance.network.registerMessage(ConfigPacketHandler.class, ConfigPacket.class, 0, Side.CLIENT);
	}
	
	@SubscribeEvent public void onClientConnect(PlayerLoggedInEvent event) {
		if (event.player instanceof EntityPlayerMP)
			TomahawksCore.instance.network.sendTo(new ConfigPacket(local), (EntityPlayerMP) event.player);
	}
	
	static void onServerConnect(ConfigPacket packet) {
		current = packet.value;
		TomahawksCore.instance.onServerStart(null);
	}
	
	@SubscribeEvent public void onServerDisconnect(ClientDisconnectionFromServerEvent event) {
		current = local;
		TomahawksCore.instance.onServerStop(null);
	}
	
	static Config preserved() {
		return preserved;
	}
	static Config local() {
		return local;
	}
	public static Config current() {
		return current;
	}
	
	
	
	private static Iterable<Field> configurableFields() {
		return Utils.asIterable(Config.class.getDeclaredFields())
				.filter(new Predicate<Field>() { public boolean apply(Field input) {
					return !Modifier.isStatic(input.getModifiers())
							&& Configurable.class.isAssignableFrom(input.getType());
				}});
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
	
	
	NBTTagCompound writeToNBT() {
		NBTTagCompound tags = new NBTTagCompound();
		for (Field f : configurableFields())
			if (f.getAnnotation(ApplyHot.class) == null && f.getAnnotation(NoSync.class) == null)
				tags.setTag(f.getName(), Fields.<Configurable<?>>get(this, f).writeToNBT());
		return tags;
	}
	
	@SuppressWarnings("unchecked")
	Config readFromNBT(NBTTagCompound tags) {
		for (String key : (Set<String>) tags.func_150296_c())
			try {
				Configurable<?> c = (Configurable<?>) Config.class.getField(key).get(this);
				c.readFromNBT(tags.getCompoundTag(key));
			} catch (Throwable e) {
				TomahawksCore.instance.log.catching(e);
			}
		
		return this;
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
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	private @interface NoSync { }
	
}
