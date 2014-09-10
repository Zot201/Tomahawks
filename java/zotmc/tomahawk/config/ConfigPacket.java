package zotmc.tomahawk.config;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import zotmc.tomahawk.core.TomahawksCore;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

public class ConfigPacket implements IMessage {
	
	Config value;
	
	@Deprecated public ConfigPacket() { }
	
	ConfigPacket(Config value) {
		this.value = value;
	}
	
	@Override public void fromBytes(ByteBuf buf) {
		try {
			value = Config.local().copy().readFromNBT(new PacketBuffer(buf).readNBTTagCompoundFromBuffer());
		} catch (IOException e) {
			TomahawksCore.instance.log.catching(e);
		}
	}
	
	@Override public void toBytes(ByteBuf buf) {
		try {
			new PacketBuffer(buf).writeNBTTagCompoundToBuffer(value.writeToNBT());
		} catch (IOException e) {
			TomahawksCore.instance.log.catching(e);
		}
	}

}
