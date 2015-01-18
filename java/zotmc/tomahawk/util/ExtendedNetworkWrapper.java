package zotmc.tomahawk.util;

import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class ExtendedNetworkWrapper extends SimpleNetworkWrapper {
	
	public ExtendedNetworkWrapper(String channelName) {
		super(channelName);
	}
	
	/**
	 * Dispatch from server side to notify entity updates.
	 */
	public void sendToAllTrackingPlayers(IMessage message, Entity entity) {
		if (entity instanceof EntityPlayerMP)
			sendTo(message, (EntityPlayerMP) entity);
		for (EntityPlayerMP player : getTrackingPlayers(entity))
			sendTo(message, player);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Set<EntityPlayerMP> getTrackingPlayers(Entity entity) {
		return (Set) ((WorldServer) entity.worldObj).getEntityTracker().getTrackingPlayers(entity);
	}
	
}
