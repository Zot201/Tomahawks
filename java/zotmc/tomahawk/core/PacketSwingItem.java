package zotmc.tomahawk.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketSwingItem implements IMessage {
	
	@Deprecated public PacketSwingItem() { }
	
	private int entityId;
	private boolean isValid = true;

	public PacketSwingItem(int entityId) {
		this.entityId = entityId;
	}
	
	@Override public void toBytes(ByteBuf buf) {
		buf.writeInt(entityId);
	}
	
	@Override public void fromBytes(ByteBuf buf) {
		try {
			entityId = buf.readInt();
			
		} catch (Throwable t) {
			TomahawksCore.instance.log.catching(t);
			isValid = false;
		}
	}
	
	public static class Handler implements IMessageHandler<PacketSwingItem, IMessage> {
		@Deprecated public Handler() { }
		
		@Override public IMessage onMessage(PacketSwingItem message, MessageContext ctx) {
			if (message.isValid) {
				Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
				if (entity instanceof EntityLivingBase) ((EntityLivingBase) entity).swingItem();
			}
			return null;
		}
	}

}
