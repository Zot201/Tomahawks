package zotmc.tomahawk.config;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class ConfigPacketHandler implements IMessageHandler<ConfigPacket, IMessage> {
	
	@Deprecated public ConfigPacketHandler() { }

	@Override public IMessage onMessage(ConfigPacket message, MessageContext ctx) {
		Config.onServerConnect(message);
		
		return null;
	}

}
