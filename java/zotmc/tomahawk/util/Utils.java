package zotmc.tomahawk.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Splitter;

import cpw.mods.fml.common.registry.GameData;

public class Utils {
	
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
	

	
	@SuppressWarnings("unchecked")
	public static Iterable<Item> itemList() {
		return GameData.getItemRegistry();
	}
	
	private static final Splitter COLON = Splitter.on(':').limit(2);
	
	public static String getModid(Item item) {
		return COLON.split(GameData.getItemRegistry().getNameForObject(item))
				.iterator()
				.next();
	}
	
	
	
	public static final float PI = (float) Math.PI;
	
	public static float atan(double a) {
		return (float) Math.atan(a);
	}
	
	public static float atan2(double y, double x) {
		return (float) Math.atan2(y, x);
	}
	
	public static float sqrt(double a) {
		return MathHelper.sqrt_double(a);
	}
	
	public static float sqrt(float a) {
		return MathHelper.sqrt_float(a);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static <T> Function<T, String> toStringFunction() {
		return (Function<T, String>) Functions.toStringFunction();
	}
	
}
