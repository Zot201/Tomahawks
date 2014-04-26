package zotmc.tomahawk;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpw.mods.fml.common.Loader.isModLoaded;
import static cpw.mods.fml.common.ObfuscationReflectionHelper.remapFieldNames;
import static cpw.mods.fml.common.eventhandler.Event.Result.DENY;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_AIR;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;
import static zotmc.tomahawk.Tomahawk.MODID;
import static zotmc.tomahawk.Tomahawk.NAME;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.EntityRegistry;

@Mod(modid = MODID, name = NAME, version = "1.0.0.1-1.7.2")
public class Tomahawk {
	
	public static final String
	MODID = "axetomahawk",
	NAME = "Tomahawk",
	PACKAGE_NAME = "zotmc.tomahawk";
	
	@SidedProxy(
			clientSide = PACKAGE_NAME + ".ClientProxy",
			serverSide = PACKAGE_NAME + ".CommonProxy")
	public static CommonProxy proxy;

	@EventHandler public void preInit(FMLPreInitializationEvent event) {
		EntityRegistry.registerModEntity(EntityTomahawk.class,
				"tomahawk", 0, this, 64, 1, true);
		
	}
	
	@EventHandler public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
		proxy.registerRenderer();
		
		if (isModLoaded("onlysilver"))
			OnlySilverHandler.registerWeaponFunction();
		
	}
	
	
	
	private Random rand = new Random();
	
	@SubscribeEvent public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.action == RIGHT_CLICK_AIR || event.action == RIGHT_CLICK_BLOCK) {
			ItemStack item = event.entityPlayer.getHeldItem();
			
			if (item != null && item.getItem() instanceof ItemAxe) {
				EntityPlayer player = event.entityPlayer;
				World world = player.worldObj;
				
				if (event.action == RIGHT_CLICK_BLOCK) {
					int x = event.x, y = event.y, z = event.z;
					Block block = world.getBlock(x, y, z);
			        boolean isAir = block.isAir(world, x, y, z);
			        
			        boolean useBlock = !player.isSneaking();
			        if (!useBlock)
			        	useBlock = player.getHeldItem().getItem().doesSneakBypassUse(world, x, y, z, player);

			        if (useBlock && event.useBlock != DENY)
			        	if (block.onBlockActivated(world, x, y, z, player, event.face, 0.5F, 0.5F, 0.5F)) {
			        		event.useBlock = DENY;
			        		return;
			        	}
				}
				
				EntityTomahawk tomahawk = new EntityTomahawk(world, player, item);
				
				if (!player.capabilities.isCreativeMode)
					player.setCurrentItemOrArmor(0, null);
				else
					tomahawk.canBePickedUp = false;
				
				world.playSoundAtEntity(player, "random.bow", 1,
						1 / (rand.nextFloat() * 0.4F + 1.2F) + 0.5F);
				
				if (!world.isRemote)
					world.spawnEntityInWorld(tomahawk);
				
				player.swingItem();
			}
		}
	}
	
	

	public static void onCritical(Entity attacker, Entity victim) {
		if (attacker.worldObj instanceof WorldServer)
			((WorldServer) attacker.worldObj).getEntityTracker().func_151248_b(
					attacker, new S0BPacketAnimation(victim, 4));
	}
	
	public static void onEnchantmentCritical(Entity attacker, Entity victim) {
		if (attacker.worldObj instanceof WorldServer)
			((WorldServer) attacker.worldObj).getEntityTracker().func_151248_b(
					attacker, new S0BPacketAnimation(victim, 5));
	}
	
	
	
	

	public static Field findField(Class<?> clz, String... names) {
		Field f = null;
		for (String s : remapFieldNames(clz.getName(), names))
			try {
				f = clz.getDeclaredField(s);
				f.setAccessible(true);
				break;
			} catch (Exception ignored) { }
		
		return checkNotNull(f);
	}
	
	public static MethodFinder findMethod(Class<?> clz, String... names) {
		return new MethodFinder(clz, names);
	}
	
	public static class MethodFinder {
		private final Class<?> clz;
		private final String[] names;
		private MethodFinder(Class<?> clz, String[] names) {
			this.clz = clz;
			this.names = names;
		}
		public Method withArgs(Class<?>... parameterTypes) {
			Method m = null;
			for (String s : remapMethodNames(clz.getName(), names))
				try {
					m = clz.getDeclaredMethod(s, parameterTypes);
					m.setAccessible(true);
					break;
				} catch (Exception ignored) { }
			
			return checkNotNull(m);
		}
	}
	
    private static String[] remapMethodNames(String className, String... methodNames) {
        String internalClassName = FMLDeobfuscatingRemapper.INSTANCE.unmap(className.replace('.', '/'));
        String[] mappedNames = new String[methodNames.length];
        int i = 0;
        for (String mName : methodNames)
            mappedNames[i++] = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(
            		internalClassName, mName, null);
        return mappedNames;
    }

}
