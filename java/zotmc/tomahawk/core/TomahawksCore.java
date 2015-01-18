package zotmc.tomahawk.core;

import static cpw.mods.fml.common.eventhandler.EventPriority.LOW;
import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.api.ItemHandler.EnchantmentAction.INHERIT_GOLDEN_SWORD;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_GUI_FACTORY;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_MODID;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_NAME;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.MC_STRING;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.MODID;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.VERSION;

import java.io.File;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zotmc.tomahawk.api.DamageTypeAdaptor;
import zotmc.tomahawk.api.TomahawkAPI;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.config.GuiConfigScreenResolver;
import zotmc.tomahawk.data.I18nData;
import zotmc.tomahawk.data.ModData;
import zotmc.tomahawk.data.ModData.AdditionalEnchantments;
import zotmc.tomahawk.data.ModData.MoreEnchants;
import zotmc.tomahawk.data.ModData.OnlySilver;
import zotmc.tomahawk.ench.EnchReplica;
import zotmc.tomahawk.projectile.EntityTomahawk;
import zotmc.tomahawk.projectile.FakePlayerTomahawk;
import zotmc.tomahawk.projectile.RenderTomahawk;
import zotmc.tomahawk.transform.LoadingPluginTomahawk;
import zotmc.tomahawk.util.DummyPlayer;
import zotmc.tomahawk.util.DummyWorld;
import zotmc.tomahawk.util.ExtendedNetworkWrapper;
import zotmc.tomahawk.util.Reserve;
import zotmc.tomahawk.util.Utils;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.MissingModsException;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = CORE_MODID, name = CORE_NAME, version = VERSION, guiFactory = CORE_GUI_FACTORY)
public class TomahawksCore {
	
	@Instance(CORE_MODID) public static TomahawksCore instance;
	
	public final Logger log = LogManager.getFormatterLogger(CORE_MODID);
	public final ExtendedNetworkWrapper network = new ExtendedNetworkWrapper(CORE_MODID);
	
	private final EventBus eventBus = new EventBus();
	
	
	@EventHandler public void onConstruct(FMLConstructionEvent event) {
		Set<ArtifactVersion> missing = Utils.checkRequirements(ModData.class, MC_STRING);
		if (!missing.isEmpty())
			throw new MissingModsException(missing);
		
		eventBus.register(Utils.construct(LoadingPluginTomahawk.Validating.class));
		eventBus.register(Utils.construct(TomahawkAPI.class));
		
		network.registerMessage(PacketSwingItem.Handler.class, PacketSwingItem.class, 1, Side.CLIENT);
	}
	
	
	@SideOnly(CLIENT)
	private void registerResolverFactory() {
		MinecraftForge.EVENT_BUS.register(new ResolverFactory());
	}
	
	@SideOnly(CLIENT)
	public class ResolverFactory { @SubscribeEvent public void onGuiOpen(GuiOpenEvent event) {
		if (event.gui instanceof GuiMainMenu)
			event.gui = new GuiConfigScreenResolver(event.gui);
		MinecraftForge.EVENT_BUS.unregister(this);
	}}
	
	@EventHandler public void preInit(FMLPreInitializationEvent event) {
		ModData.initCore(event.getModMetadata());
		
		Config.init(new Configuration(new File(event.getModConfigurationDirectory(), MODID + ".cfg")));
		
		EntityRegistry.registerModEntity(EntityTomahawk.class, "tomahawk", 0, this, 64, 18, true);
		Utils.EntityLists.stringToClassMapping().put("axetomahawk.tomahawk", EntityTomahawk.class); // for backward compatibility
		
		int id = Config.current().replica.get();
		if (id != -1) {
			if (Enchantment.enchantmentsList[id] != null && event.getSide() == Side.CLIENT)
				registerResolverFactory();
			else {
				Reserve<Enchantment> replica = (Reserve<Enchantment>) TomahawkAPI.replica;
				replica.set(new EnchReplica(id).setName(I18nData.NAME_REPLICA));
			}
		}
	}
	
	
	@SideOnly(CLIENT)
	private void registerHandlers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTomahawk.class, new RenderTomahawk());
		
		if (Utils.MC_VERSION.isBelow("1.7.2"))
			MinecraftForge.EVENT_BUS.register(new SoundLoadHandler());
	}
	
	@EventHandler public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		
		if (event.getSide() == Side.CLIENT)
			registerHandlers();
		
		
		if (Loader.isModLoaded(AdditionalEnchantments.MODID)) {
			if (Utils.MC_VERSION.isAtLeast("1.7.2"))
				DamageTypeAdaptor.instance().delegateByModid(AdditionalEnchantments.MODID);
			else
				DamageTypeAdaptor.instance().delegateByNamePattern(AdditionalEnchantments.NAME_PATTERN);
		}
		
		if (Loader.isModLoaded(MoreEnchants.MODID)) {
			if (Utils.MC_VERSION.isAtLeast("1.7.2"))
				DamageTypeAdaptor.instance().delegateByModid(MoreEnchants.MODID);
			else
				DamageTypeAdaptor.instance().delegateByNamePattern(MoreEnchants.NAME_PATTERN);
		}
		
		if (Loader.isModLoaded(OnlySilver.MODID) && Utils.MC_VERSION.isAtLeast("1.7.2"))
			try {
				OnlySilverHandler.init();
			} catch (Throwable e) {
				log.catching(e);
			}
	}
	
	
	public class PostInitialization extends Event { private PostInitialization() { }}
	public class LoadComplete extends Event { private LoadComplete() { }}
	public class ServerAboutToStart extends Event { private ServerAboutToStart() { }}
	public class ServerStopping extends Event { private ServerStopping() { }}
	
	@EventHandler public void postInit(FMLPostInitializationEvent event) {
		eventBus.post(new PostInitialization());
	}
	
	@EventHandler public void onAvailable(FMLLoadCompleteEvent event) {
		eventBus.post(new LoadComplete());
		
		try {
			DispenserHandler.init();
		} catch (Throwable e) {
			log.catching(e);
		}
		
		try {
			World world = new DummyWorld();
			new EntityTomahawk(world, new DummyPlayer(world), 1, new ItemStack(Items.arrow)).onUpdate();
			
		} catch (Throwable e) {
			log.catching(e);
		}
	}
	
	@EventHandler public void onServerStart(FMLServerAboutToStartEvent unused) {
		eventBus.post(new ServerAboutToStart());
	}
	
	@EventHandler public void onServerStop(FMLServerStoppingEvent unused) {
		eventBus.post(new ServerStopping());
	}
	
	
	
	// Forge Events
	
	@SubscribeEvent(priority = LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.world.isRemote && event.action == Action.RIGHT_CLICK_AIR && TomahawkHooks.isLaunchable(event))
			TomahawkHooks.activateTomahawk(event);
	}
	
	@SubscribeEvent public void onLivingHurt(LivingHurtEvent event) {
		if (!event.entityLiving.worldObj.isRemote) {
			Entity sod = event.source.getSourceOfDamage();
			
			if ((sod instanceof EntityTomahawk || sod instanceof FakePlayerTomahawk) && sod.isBurning())
				event.entityLiving.setFire(5);
		}
	}
	
	@SubscribeEvent public void onEntityConstruct(EntityConstructing event) {
		if (event.entity instanceof EntityPlayer)
			new PlayerTracker((EntityPlayer) event.entity).register();
	}
	
	@SubscribeEvent public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.entity instanceof EntityPlayer)
			PlayerTracker.get((EntityPlayer) event.entity).onUpdate();
	}
	
	@SubscribeEvent public void onAnvilUpdate(AnvilUpdateEvent event) {
		// left and right are known not null
		if (Config.current().goldenFusion.get()
				&& event.right.getItem() == Items.golden_sword
				&& TomahawkRegistry.getItemHandler(event.left).isEnchantable(event.left, INHERIT_GOLDEN_SWORD)) {
			Map<Integer, Integer> enchs = Utils.getEnchs(event.right);
			
			if (!enchs.isEmpty())
				TomahawkImpls.fuseGoldenSword(event, enchs);
		}
	}
	
}
