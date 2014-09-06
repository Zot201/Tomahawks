package zotmc.tomahawk.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import zotmc.tomahawk.data.AsmData.Pointables;

public final class TransformerGetMouseOver extends InsnCombinePointable {
	
	public TransformerGetMouseOver() {
		super(Pointables.GET_MOUSE_OVER, Opcodes.IOR);
	}
	
	@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
		if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL)
			return Pointables.CAN_BE_COLLIDE_WITH.covers((MethodInsnNode) insnNode);
		
		if (insnNode.getOpcode() == Opcodes.INSTANCEOF)
			return Pointables.ENTITY_ITEM_FRAME.covers((TypeInsnNode) insnNode);
		
		return false;
	}
	
}
