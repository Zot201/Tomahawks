package zotmc.tomahawk.core;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraftforge.event.EventPriority.LOW;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_DEPENDENCIES;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_MODID;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.CORE_NAME;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.MC_STRING;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.MODID;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.VERSION;

import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import zotmc.tomahawk.api.DamageTypeAdaptor;
import zotmc.tomahawk.api.PickUpType;
import zotmc.tomahawk.api.TomahawkAPI;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.api.WeaponLaunchEvent;
import zotmc.tomahawk.config.Config;
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
import zotmc.tomahawk.util.Reserve;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.geometry.CartesianVec3f;
import zotmc.tomahawk.util.geometry.Vec3f;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.MissingModsException;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.relauncher.SideOnly;

@NetworkMod(clientSideRequired=true, serverSideRequired=false)
@Mod(modid = CORE_MODID, name = CORE_NAME, version = VERSION, dependencies = CORE_DEPENDENCIES)
public class TomahawksCore {
	
	@Instance(CORE_MODID) public static TomahawksCore instance;
	
	public final Logger log = Logger.getLogger(CORE_MODID);
	
	//public final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(CORE_MODID);
	
	
	public TomahawksCore() {
		onConstruct();
	}
	/*@EventHandler*/ public void onConstruct() {
		Set<ArtifactVersion> missing = Utils.checkRequirements(ModData.class, MC_STRING);
		
		if (!missing.isEmpty())
			throw new MissingModsException(missing);
	}
	
	@EventHandler public void preInit(FMLPreInitializationEvent event) {
		ModData.initCore(event.getModMetadata());
		
		Config.init(new Configuration(new File(event.getModConfigurationDirectory(), MODID + ".cfg")));
		
		EntityRegistry.registerModEntity(EntityTomahawk.class, "tomahawk", 0, this, 64, 18, true);
		Utils.EntityLists.stringToClassMapping().put("axetomahawk.tomahawk", EntityTomahawk.class); // for backward compatibility
		
		int id = Config.current().replica.get();
		if (id != -1) {
			Reserve<Enchantment> replica = (Reserve<Enchantment>) TomahawkAPI.replica;
			replica.set(new EnchReplica(id).setName(I18nData.NAME_REPLICA));
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
		
		Utils.invokeIfExists(this, TomahawksCore.class, "registerHandlers");
		
		
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
				log.severe("catching");
				e.printStackTrace();
			}
	}
	
	@EventHandler public void postInit(FMLPostInitializationEvent event) {
		Utils.invokeDeclared(LoadingPluginTomahawk.class, "postInit");
		
		onAvailable();
	}
	
	/*@EventHandler*/ public void onAvailable() {
		Utils.invokeDeclared(TomahawkAPI.class, "onAvailable");
		
		try {
			DispenserHandler.init();
		} catch (Throwable e) {
			log.severe("catching");
			e.printStackTrace();
		}
		
		try {
			World world = new DummyWorld();
			new EntityTomahawk(world, new DummyPlayer(world), 1, new ItemStack(Item.arrow)).onUpdate();
			
		} catch (Throwable e) {
			log.severe("catching");
			e.printStackTrace();
		}
	}
	
	@EventHandler public void onServerStart(FMLServerAboutToStartEvent  event) {
		Utils.invokeDeclared(TomahawkAPI.class, "onServerStart");
	}
	
	@EventHandler public void onServerStop(FMLServerStoppingEvent event) {
		Utils.invokeDeclared(TomahawkAPI.class, "onServerStop");
	}
	
	
	
	// Forge Events
	
	final Vec3f hit = new CartesianVec3f();
	
	@ForgeSubscribe(priority = LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		boolean rightClickBlock = event.action == Action.RIGHT_CLICK_BLOCK;
		
		if (rightClickBlock || event.action == Action.RIGHT_CLICK_AIR) {
			EntityPlayer player = event.entityPlayer;
			PlayerTracker tracker = PlayerTracker.get(player);
			
			if (tracker.getAfterInteract() != 0) {
				ItemStack item = player.getHeldItem();
				
				if (item != null && item.stackSize > 0 && TomahawkAPI.isLaunchable(item)) {
					if (player.worldObj.isRemote)
						TomahawkImpls.setHit();
					
					if (rightClickBlock && TomahawkImpls.activateBlock(event, player, item, hit)) {
		        		event.useBlock = Event.Result.DENY;
		        		return;
					}
					
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

						event.useItem = Event.Result.DENY;
		        		event.useBlock = Event.Result.DENY;
		        		tracker.onInteract();
					}
				}
			}
		}
	}
	
	@ForgeSubscribe public void onLivingHurt(LivingHurtEvent event) {
		if (!event.entityLiving.worldObj.isRemote) {
			Entity sod = event.source.getSourceOfDamage();
			
			if ((sod instanceof EntityTomahawk || sod instanceof FakePlayerTomahawk) && sod.isBurning())
				event.entityLiving.setFire(5);
		}
	}
	
	@ForgeSubscribe public void onEntityConstruct(EntityConstructing event) {
		if (event.entity instanceof EntityPlayer)
			new PlayerTracker((EntityPlayer) event.entity).register();
	}
	
	@ForgeSubscribe public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.entity instanceof EntityPlayer)
			PlayerTracker.get((EntityPlayer) event.entity).onUpdate();
	}
	
}
