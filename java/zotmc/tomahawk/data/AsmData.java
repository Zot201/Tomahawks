package zotmc.tomahawk.data;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import zotmc.tomahawk.data.ModData.AxeTomahawk;
import zotmc.tomahawk.transform.AbstractInsnPatcher;
import zotmc.tomahawk.transform.InsnListBuilder;
import zotmc.tomahawk.transform.LoadingPluginTomahawk.ClientOnly;
import zotmc.tomahawk.transform.MethodPredicate;
import zotmc.tomahawk.transform.Patcher;
import zotmc.tomahawk.transform.TypePredicate;
import zotmc.tomahawk.util.init.SimpleVersion;

public class AsmData {
	
	public static final String CORE_MODID = AxeTomahawk.CORE_MODID;
	private static final SimpleVersion MC_STRING = new SimpleVersion(AxeTomahawk.MC_STRING);
	
	
	private static class Pointables {
		// type
		public static final TypePredicate TYPE = TypePredicate.of("zotmc/tomahawk/api/Pointable");
	}
	
	private static class EntityArrows {
		// type
		public static final TypePredicate TYPE = TypePredicate.of("net/minecraft/entity/projectile/EntityArrow");
	}
	
	public static class NetHandlerPlayServers {
		// targets
		private static final MethodPredicate
		PROCESS_USE_ENTITY = TypePredicate.of("net/minecraft/network/NetHandlerPlayServer")
			.method("processUseEntity", "func_147340_a")
			.desc("(Lnet/minecraft/network/play/client/C02PacketUseEntity;)V");
		
		// patches
		public static final Patcher
		PROCESS_USE_ENTITY_PATCHER = new AbstractInsnPatcher(PROCESS_USE_ENTITY) {
			@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
				return EntityArrows.TYPE.covers(Opcodes.INSTANCEOF, insnNode);
			}
			
			@Override protected void processInsn(InsnList list, AbstractInsnNode targetInsn) {
				InsnListBuilder pre = new InsnListBuilder(), post = new InsnListBuilder();
				
				pre.dup();
				
				post.swap();
				post.instanceOf(Pointables.TYPE);
				post.ixor();
				
				list.insertBefore(targetInsn, pre.build());
				list.insert(targetInsn, post.build());
			}
		};
	}
	
	private static class Entitys {
		// targets
		public static final MethodPredicate
		CAN_BE_COLLIDE_WITH = TypePredicate.of("net/minecraft/entity/Entity")
			.method("canBeCollidedWith", "func_70067_L")
			.desc("()Z");
	}
	
	private static class EntityItemFrames {
		// type
		public static final TypePredicate TYPE = TypePredicate.of("net/minecraft/entity/item/EntityItemFrame");
	}
	
	@ClientOnly
	public static class EntityRenderers {
		// targets
		private static final MethodPredicate
		GET_MOUSE_OVER = TypePredicate.of("net/minecraft/client/renderer/EntityRenderer")
			.method("getMouseOver", "func_78473_a")
			.desc("(F)V");
		
		// patches
		public static final Patcher
		GET_MOUSE_OVER_PATCHER = new AbstractInsnPatcher(GET_MOUSE_OVER) {
			@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
				return Entitys.CAN_BE_COLLIDE_WITH.covers(Opcodes.INVOKEVIRTUAL, insnNode)
						|| EntityItemFrames.TYPE.covers(Opcodes.INSTANCEOF, insnNode);
			}
			
			@Override protected void processInsn(InsnList list, AbstractInsnNode targetInsn) {
				InsnListBuilder pre = new InsnListBuilder(), post = new InsnListBuilder();
				
				pre.dup();
				
				post.swap();
				post.instanceOf(Pointables.TYPE);
				post.ior();
				
				list.insertBefore(targetInsn, pre.build());
				list.insert(targetInsn, post.build());
			}
		};
	}
	
	private static class TomahawkHookss {
		// type
		private static final TypePredicate TYPE = TypePredicate.of("zotmc/tomahawk/core/TomahawkHooks");
		
		// callbacks
		private static final MethodPredicate
		INTERACT_EVENT = TYPE.method("interactEvent")
			.desc("Ljava/lang/ThreadLocal;"),
		IS_LAUNCHABLE = TYPE.method("isLaunchable")
			.desc("()Z"),
		ACTIVATE_TOMAHAWK = TYPE.method("activateTomahawk")
			.desc("(Lnet/minecraft/world/World;FFF)Z");
	}
	
	private static class ForgeEventFactorys {
		// targets
		private static final MethodPredicate
		ON_PLAYER_INTERACT = TypePredicate.of("net/minecraftforge/event/ForgeEventFactory")
			.method("onPlayerInteract");
	}
	
	private static class ThreadLocals {
		// callbacks
		public static final MethodPredicate
		SET = TypePredicate.of("java/lang/ThreadLocal")
			.method("set")
			.desc("(Ljava/lang/Object;)V");
	}
	
	private static class Itemss {
		// targets
		public static final MethodPredicate
		ON_ITEM_USE_FIRST = TypePredicate.of("net/minecraft/item/Item")
			.method("onItemUseFirst")
			.desc("(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z");
	}
	
	private static class ItemStacks {
		// targets
		public static final MethodPredicate
		TRY_PLACE_ITEM_INTO_WORLD = TypePredicate.of("net/minecraft/item/ItemStack")
			.method("tryPlaceItemIntoWorld", "func_77943_a")
			.desc("(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z");
	}
	
	public static class ItemInWorldManagers {
		// targets
		private static final MethodPredicate
		ACTIVATE_BLOCK_OR_USE_ITEM = TypePredicate
			.of("net/minecraft/" + (MC_STRING.isAtLeast("1.7.2") ? "server/management" : "item") + "/ItemInWorldManager")
			.method("activateBlockOrUseItem", "func_73078_a")
			.desc("(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIIIFFF)Z");
		
		// patches
		public static final Patcher
		ACTIVATE_BLOCK_OR_USE_ITEM_PATCHER = new AbstractInsnPatcher(ACTIVATE_BLOCK_OR_USE_ITEM) {
			@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
				return ForgeEventFactorys.ON_PLAYER_INTERACT.covers(Opcodes.INVOKESTATIC, insnNode)
						|| Itemss.ON_ITEM_USE_FIRST.covers(Opcodes.INVOKEVIRTUAL, insnNode)
						|| ItemStacks.TRY_PLACE_ITEM_INTO_WORLD.covers(Opcodes.INVOKEVIRTUAL, insnNode);
			}
			
			@Override protected void processInsn(InsnList list, AbstractInsnNode insnNode) {
				if (ForgeEventFactorys.ON_PLAYER_INTERACT.covers(Opcodes.INVOKESTATIC, insnNode)) {
					InsnListBuilder post = new InsnListBuilder();
					
					post.dup();
					post.getstatic(TomahawkHookss.INTERACT_EVENT);
					post.swap();
					post.invokevirtual(ThreadLocals.SET, false);
					
					list.insert(insnNode, post.build());
				}
				else if (Itemss.ON_ITEM_USE_FIRST.covers(Opcodes.INVOKEVIRTUAL, insnNode)) {
					InsnListBuilder pre = new InsnListBuilder(), post = new InsnListBuilder();
					Label l0 = new Label(), l1 = new Label();
					
					pre.invokestatic(TomahawkHookss.IS_LAUNCHABLE, false);
					pre.ifne(l0);
					pre.pop(11);
					pre.iconst(0);
					pre.goTo(l1);
					pre.mark(l0);
					
					post.mark(l1);
					
					list.insertBefore(insnNode, pre.build());
					list.insert(insnNode, post.build());
				}
				else if (ItemStacks.TRY_PLACE_ITEM_INTO_WORLD.covers(Opcodes.INVOKEVIRTUAL, insnNode)) {
					InsnListBuilder pre = new InsnListBuilder(), post = new InsnListBuilder();
					Label l0 = new Label(), l1 = new Label();
					
					pre.aload(2);
					pre.fload(8);
					pre.fload(9);
					pre.fload(10);
					pre.invokestatic(TomahawkHookss.ACTIVATE_TOMAHAWK, false);
					pre.ifeq(l0);
					pre.pop(10);
					pre.iconst(1);
					pre.goTo(l1);
					pre.mark(l0);
					
					post.mark(l1);
					
					list.insertBefore(insnNode, pre.build());
					list.insert(insnNode, post.build());
				}
				else throw new AssertionError();
			}
		};
	}
	
}
