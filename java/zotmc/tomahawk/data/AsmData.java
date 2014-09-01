package zotmc.tomahawk.data;

import java.util.List;

import zotmc.tomahawk.data.ModData.AxeTomahawk;

import com.google.common.collect.ImmutableList;

public class AsmData {
	
	public static final String
	MODID = AxeTomahawk.MODID,
	
	NET_HANDLER_PLAY_SERVER = "net.minecraft.network.NetHandlerPlayServer",
	ENTITY_RENDERER = "net.minecraft.client.renderer.EntityRenderer",
	
	ENTITY_ARROW_DESC = "net.minecraft.entity.projectile.EntityArrow".replace('.', '/'),
	POINTABLE_DESC = "zotmc.tomahawk.api.Pointable".replace('.', '/'),
	ENTITY_DESC = "net.minecraft.entity.Entity".replace('.', '/'),
	ENTITY_ITEM_FRAME_DESC = "net.minecraft.entity.item.EntityItemFrame".replace('.', '/');
	
	public static final List<String>
	PROCESS_USE_ENTITY = ImmutableList.of("processUseEntity", "func_147340_a"),
	GET_MOUSE_OVER = ImmutableList.of("getMouseOver", "func_78473_a"),
	CAN_BE_COLLIDE_WITH = ImmutableList.of("canBeCollidedWith", "func_70067_L"),
	
	CAN_BE_COLLIDE_WITH_DESC = ImmutableList.of("()Z");
	
}
