package zotmc.tomahawk.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import zotmc.tomahawk.data.AsmData;

public final class TransformerGetMouseOver extends InsnCombinePointable {
	
	public TransformerGetMouseOver() {
		super(Opcodes.IOR, AsmData.ENTITY_RENDERER, AsmData.GET_MOUSE_OVER);
	}
	
	@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
		if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL)
			return AsmData.CAN_BE_COLLIDE_WITH.covers((MethodInsnNode) insnNode);
		
		if (insnNode.getOpcode() == Opcodes.INSTANCEOF)
			return AsmData.ENTITY_ITEM_FRAME.covers((TypeInsnNode) insnNode);
		
		return false;
	}
	
}
