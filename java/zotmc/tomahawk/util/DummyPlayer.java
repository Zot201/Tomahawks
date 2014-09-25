package zotmc.tomahawk.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class DummyPlayer extends EntityPlayer {
	
	public DummyPlayer(World world) {
		super(world, Utils.newGameProfile(null, "dummy player"));
	}
	
	@Override public void addChatMessage(IChatComponent msg) { }
	
	@Override public boolean canCommandSenderUseCommand(int i, String s) {
		return false;
	}
	
	@Override public ChunkCoordinates getPlayerCoordinates() {
		return new ChunkCoordinates(0, 0, 0);
	}
	
}
