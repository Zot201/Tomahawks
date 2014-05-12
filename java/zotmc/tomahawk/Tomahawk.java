package zotmc.tomahawk;

import static cpw.mods.fml.common.Loader.isModLoaded;
import static cpw.mods.fml.common.eventhandler.Event.Result.DENY;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
import static zotmc.tomahawk.Tomahawk.GUI_FACTORY;
import static zotmc.tomahawk.Tomahawk.MODID;
import static zotmc.tomahawk.Tomahawk.NAME;
import static zotmc.tomahawk.config.Config.ConfigState.AVAILABLE;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.CREATIVE;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.SURVIVAL;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.config.Config.ConfigState;
import zotmc.tomahawk.projectile.EntityTomahawk;
import zotmc.tomahawk.projectile.EntityTomahawk.PickUpType;
import zotmc.tomahawk.util.Holder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;

@Mod(modid = MODID, name = NAME, version = "1.2.0.1-1.7.2", guiFactory = GUI_FACTORY)
public class Tomahawk {
	
	public static final String
	MODID = "axetomahawk",
	NAME = "Tomahawk",
	PACKAGE_NAME = "zotmc.tomahawk",
	GUI_FACTORY = PACKAGE_NAME + ".config.GuiConfigs";
	
	@SidedProxy(
			clientSide = PACKAGE_NAME + ".ClientProxy",
			serverSide = PACKAGE_NAME + ".CommonProxy")
	public static CommonProxy proxy;
	
	@Instance(MODID) public static Tomahawk axetomahawk;
	
	
	public Logger log = LogManager.getFormatterLogger(MODID);
	
	private final Holder<ConfigState> configState = Holder.absent();
	
	
	@EventHandler public void preInit(FMLPreInitializationEvent event) {
		Config.init(new Configuration(event.getSuggestedConfigurationFile()), configState);
		
		EntityRegistry.registerModEntity(EntityTomahawk.class,
				"tomahawk", 0, this, 64, 10, true);
		
	}
	
	@EventHandler public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		//MinecraftForge.EVENT_BUS.register(new EventListener()); //temporary deactivated Mob AI
		
		proxy.registerRenderer();
		
		DispenserHandler.init();
		
		if (isModLoaded("onlysilver"))
			OnlySilverHandler.init();
		if (isModLoaded("TConstruct"))
			try {
				final Class<?> hatchet = Class.forName("tconstruct.items.tools.Hatchet");
				
				TomahawkRegistry.registerThrowableAxes(new Predicate<Item>() {
					@Override public boolean apply(Item input) {
						return Config.current().tiCHatchetsThrowing.get()
								&& hatchet.isInstance(input);
					}
				});
				
				
				final Class<?> fryingPan = Class.forName("tconstruct.items.tools.FryingPan");

				TomahawkRegistry.registerThrowableAxes(new Predicate<Item>() {
					@Override public boolean apply(Item input) {
						return Config.current().tiCFryingPansThrowing.get()
								&& fryingPan.isInstance(input);
					}
				});
				
				TomahawkRegistry.registerHitSounds(new Function<Item, SoundType>() {
					final Random rand = new Random();
					final SoundType sound = new SoundType("tinker:frypan_hit", 1, 1) {
						@Override public float getPitch() {
							return (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F;
						}
					};
					@Override public SoundType apply(Item input) {
						return fryingPan.isInstance(input) ? sound : null;
					}
				});
				
				
			} catch (ClassNotFoundException e) {
				log.catching(e);
			}
		
	}
	
	@EventHandler public void onServerStart(FMLServerStartedEvent event) {
		configState.set(AVAILABLE);
		
		TomahawkRegistry.refreshThrowableAxes();
		TomahawkRegistry.refreshHitSounds();
		
	}
	
	
	
	
	private Random rand = new Random();
	
	@SubscribeEvent public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.action == RIGHT_CLICK_AIR || event.action == RIGHT_CLICK_BLOCK) {
			ItemStack item = event.entityPlayer.getHeldItem();
			
			if (item != null
					&& TomahawkRegistry.isThrowableAxe(item.getItem())) {
				
				EntityPlayer player = event.entityPlayer;
				World world = player.worldObj;
				
				if (event.action == RIGHT_CLICK_BLOCK) {
					int x = event.x, y = event.y, z = event.z;
					Block block = world.getBlock(x, y, z);
			        
			        boolean useBlock = !player.isSneaking();
			        if (!useBlock)
			        	useBlock = player.getHeldItem().getItem().doesSneakBypassUse(world, x, y, z, player);

			        if (useBlock && event.useBlock != DENY)
			        	if (block.onBlockActivated(world, x, y, z, player, event.face, 0.5F, 0.5F, 0.5F)) {
			        		event.useBlock = DENY;
			        		return;
			        	}
				}
				
				boolean isCreative = player.capabilities.isCreativeMode;
				throwTomahawk(player, world, true, isCreative ? CREATIVE : SURVIVAL);
				if (!isCreative)
					player.setCurrentItemOrArmor(0, null);
				
				event.useItem = DENY;
			}
		}
	}

	public void throwTomahawk(EntityLivingBase thrower, World world,
			boolean isForwardSpin, PickUpType pickUpType) {
		throwTomahawk(thrower, thrower.getHeldItem(), world, isForwardSpin, pickUpType);
	}
	public void throwTomahawk(EntityLivingBase thrower, ItemStack tomahawk, World world,
			boolean isForwardSpin, PickUpType pickUpType) {
		
		EntityTomahawk hawk = new EntityTomahawk(world, thrower, tomahawk);
		hawk.setIsForwardSpin(isForwardSpin);
		hawk.pickUpType = pickUpType;
		
		thrower.setSprinting(false);
		thrower.swingItem();
		if (thrower instanceof EntityPlayer)
			((EntityPlayer) thrower).addExhaustion(0.3F);
		
		world.playSoundAtEntity(thrower, "random.bow", 1,
				1 / (rand.nextFloat() * 0.4F + 1.2F) + 0.5F);
		
		if (!world.isRemote)
			world.spawnEntityInWorld(hawk);
		
	}

}
