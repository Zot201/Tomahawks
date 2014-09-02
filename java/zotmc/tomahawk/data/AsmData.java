package zotmc.tomahawk.data;

import org.objectweb.asm.Type;

import zotmc.tomahawk.data.ModData.AxeTomahawk;
import zotmc.tomahawk.util.Messod;
import zotmc.tomahawk.util.Typo;

public class AsmData {
	
	public static final String
	MODID = AxeTomahawk.MODID,
	POINTABLE_DESC = "zotmc/tomahawk/api/Pointable";
	

	public static final Typo
	NET_HANDLER_PLAY_SERVER = Typo.of("net/minecraft/network/NetHandlerPlayServer"),
	ENTITY_ARROW = Typo.of("net/minecraft/entity/projectile/EntityArrow");
	
	public static final Messod
	PROCESS_USE_ENTITY = NET_HANDLER_PLAY_SERVER
		.mess("processUseEntity", "func_147340_a")
		.desc(Type.VOID_TYPE, "net/minecraft/network/play/client/C02PacketUseEntity");
	
	
	public static final Typo
	ENTITY_RENDERER = Typo.of("net/minecraft/client/renderer/EntityRenderer"),
	ENTITY = Typo.of("net/minecraft/entity/Entity"),
	ENTITY_ITEM_FRAME = Typo.of("net/minecraft/entity/item/EntityItemFrame");
	
	public static final Messod
	GET_MOUSE_OVER = ENTITY_RENDERER
		.mess("getMouseOver", "func_78473_a")
		.desc(Type.VOID_TYPE, Type.FLOAT_TYPE),
	
	CAN_BE_COLLIDE_WITH = ENTITY
		.mess("canBeCollidedWith", "func_70067_L")
		.desc(Type.BOOLEAN_TYPE);
	
}
