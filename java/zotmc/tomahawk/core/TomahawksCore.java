package zotmc.tomahawk.core;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_DEPENDENCIES;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_GUI_FACTORY;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_MODID;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_NAME;
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
import zotmc.tomahawk.api.PickUpType;
import zotmc.tomahawk.api.TomahawkAPI;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.api.WeaponLaunchEvent;
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
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = CORE_MODID, name = CORE_NAME, version = VERSION,
		dependencies = CORE_DEPENDENCIES, guiFactory = CORE_GUI_FACTORY)
public class TomahawksCore {
	
	@Instance(CORE_MODID) public static TomahawksCore instance;
	
	public final Logger log = LogManager.getFormatterLogger(MODID);
	
	
	@EventHandler public void onConstruct(FMLConstructionEvent event) {
		Set<ArtifactVersion> missing = Utils.checkRequirements(ModData.class);
		if (!missing.isEmpty())
			throw new MissingModsException(missing);
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
		
		EntityRegistry.registerModEntity(
				EntityTomahawk.class, "tomahawk", 0, this, 64, 18, true
		);
		Utils.EntityLists.stringToClassMapping().put("axetomahawk.tomahawk", EntityTomahawk.class); // for backward compatibility
		
		int id = Config.current().replica.get();
		if (id != -1 && (Enchantment.enchantmentsList[id] == null
				|| !Utils.invokeIfExists(this, TomahawksCore.class, "registerResolverFactory"))) {
			Reserve<Enchantment> replica = (Reserve<Enchantment>) TomahawkAPI.replica;
			replica.set(new EnchReplica(id).setName(I18nData.NAME_REPLICA));
		}
	}
	
	@SideOnly(CLIENT)
	private void registerRenderer() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTomahawk.class, new RenderTomahawk());
	}
	
	@EventHandler public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		
		Utils.invokeIfExists(this, TomahawksCore.class, "registerRenderer");
		
		try {
			DispenserHandler.init();
		} catch (Throwable e) {
			TomahawksCore.instance.log.catching(e);
		}
		
		if (Loader.isModLoaded(MoreEnchants.MODID))
			DamageTypeAdaptor.instance().delegateByNamePattern(MoreEnchants.NAME_PATTERN);
		
		
		if (Loader.isModLoaded(OnlySilver.MODID) && Utils.MC_VERSION.isAtLeast("1.7.2"))
			try {
				OnlySilverHandler.init();
			} catch (Throwable e) {
				log.catching(e);
			}
	}
	
	@EventHandler public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded(AdditionalEnchantments.MODID))
			try {
				DamageTypeAdaptor.instance().registerDirectly(
						Class.forName(AdditionalEnchantments.VORPAL_EVENT_HOOK)
							.getConstructor()
							.newInstance()
				);
				
			} catch (Throwable e) {
				TomahawksCore.instance.log.catching(e);
			}
		
		Utils.invokeDeclared(LoadingPluginTomahawk.class, "postInit");
	}
	
	@EventHandler public void onAvailable(FMLLoadCompleteEvent event) {
		Utils.invokeDeclared(TomahawkAPI.class, "onAvailable");
	}
	
	@EventHandler public void onServerStart(FMLServerStartingEvent event) {
		Utils.invokeDeclared(TomahawkAPI.class, "onServerStart");
	}
	
	@EventHandler public void onServerStop(FMLServerStoppingEvent event) {
		Utils.invokeDeclared(TomahawkAPI.class, "onServerStop");
	}
	
	
	@SubscribeEvent public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.action == Action.RIGHT_CLICK_AIR) {
			EntityPlayer player = event.entityPlayer;
			ItemStack item = player.getHeldItem();
			
			if (item != null && item.stackSize > 0 && TomahawkAPI.isLaunchable(item)) {
				WeaponLaunchEvent launchEvent = new WeaponLaunchEvent(
						player, Utils.itemStack(item, 1), TomahawkRegistry.getItemHandler(item));
				
				boolean replica = !player.isSneaking() && Utils.getEnchLevel(TomahawkAPI.replica.get(), item) > 0;
				boolean creative = player.capabilities.isCreativeMode;
				
				if (replica) {
					launchEvent.setPickUpType(PickUpType.ENCH);
					launchEvent.isFragile = true;
				}
				else if (creative)
					launchEvent.setPickUpType(PickUpType.CREATIVE);
				
				if (launchEvent.run()) {
					if (!creative) {
						if (replica)
							item.damageItem(2, player);
						else
							item.stackSize--;
						
						if (item.stackSize == 0)
							player.setCurrentItemOrArmor(0, null);
					}
					
					event.useItem = Result.DENY;
	        		event.useBlock = Result.DENY;
				}
			}
		}
	}

	@SubscribeEvent public void onLivingHurt(LivingHurtEvent event) {
		if (!event.entityLiving.worldObj.isRemote) {
			Entity sod = event.source.getSourceOfDamage();
			
			if ((sod instanceof EntityTomahawk || sod instanceof FakePlayerTomahawk) && sod.isBurning())
				event.entityLiving.setFire(5);
		}
	}
	
	@SubscribeEvent public void onEntityConstruct(EntityConstructing event) {
		if (event.entity.worldObj != null && !event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
			new PositionTracker((EntityPlayer) event.entity).register();
	}
	
	@SubscribeEvent public void onLivingUpdate(LivingUpdateEvent event) {
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer)
			PositionTracker.get((EntityPlayer) event.entity).onUpdate();
	}
	
	@SubscribeEvent public void onAnvilUpdate(AnvilUpdateEvent event) {
		//left and right are not null
		if (Config.current().goldenFusion.get()
				&& event.right.getItem() == Items.golden_sword
				&& TomahawkRegistry.getItemHandler(event.left).inheritGoldenSword(event.left)) {
			Map<Integer, Integer> enchs = Utils.getEnchs(event.right);
			
			if (!enchs.isEmpty())
				TomahawkImpls.fuseGoldenSword(event, enchs);
		}
	}
	
}
