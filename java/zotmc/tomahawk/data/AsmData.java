package zotmc.tomahawk.data;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import zotmc.tomahawk.data.ModData.AxeTomahawk;
import zotmc.tomahawk.util.Messod;
import zotmc.tomahawk.util.Typo;

import com.google.common.base.Supplier;

public class AsmData {
	
	public static final String
	MODID = AxeTomahawk.MODID;
	
	public static class Pointables {
		public static final String
		POINTABLE_DESC = "zotmc/tomahawk/api/Pointable";
		
		public static final Typo
		ENTITY_ARROW = Typo.of("net/minecraft/entity/projectile/EntityArrow"),
		ENTITY_ITEM_FRAME = Typo.of("net/minecraft/entity/item/EntityItemFrame");
		
		public static final Messod
		PROCESS_USE_ENTITY = Typo.of("net/minecraft/network/NetHandlerPlayServer")
			.mess("processUseEntity", "func_147340_a")
			.desc(Type.VOID_TYPE, "net/minecraft/network/play/client/C02PacketUseEntity"),
		GET_MOUSE_OVER = Typo.of("net/minecraft/client/renderer/EntityRenderer")
			.mess("getMouseOver", "func_78473_a")
			.desc(Type.VOID_TYPE, Type.FLOAT_TYPE),
		CAN_BE_COLLIDE_WITH = Typo.of("net/minecraft/entity/Entity")
			.mess("canBeCollidedWith", "func_70067_L")
			.desc(Type.BOOLEAN_TYPE);
	}
	
	public static class SetHits {
		public static final Supplier<AbstractInsnNode>
		INVOKE_SET_HIT = new Supplier<AbstractInsnNode>() { public AbstractInsnNode get() {
			return new MethodInsnNode(Opcodes.INVOKESTATIC, "zotmc/tomahawk/core/TomahawkImpls", "setHit", "(FFF)V");
		}};
		
		public static final Messod
		ON_PLAYER_INTERACT = Typo.of("net/minecraftforge/event/ForgeEventFactory").mess("onPlayerInteract"),
		ACTIVATE_BLOCK_OR_USE_ITEM = Typo.of("net/minecraft/server/management/ItemInWorldManager")
			.mess("activateBlockOrUseItem", "func_73078_a")
			.desc(Type.BOOLEAN_TYPE, "net/minecraft/entity/player/EntityPlayer", "net/minecraft/world/World",
					"net/minecraft/item/ItemStack", Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE,
					Type.FLOAT_TYPE, Type.FLOAT_TYPE, Type.FLOAT_TYPE);
	}
	
}
