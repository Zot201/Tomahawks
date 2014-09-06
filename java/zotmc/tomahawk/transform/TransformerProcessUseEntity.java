package zotmc.tomahawk.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import zotmc.tomahawk.data.AsmData.Pointables;

public final class TransformerProcessUseEntity extends InsnCombinePointable {
	
	public TransformerProcessUseEntity() {
		super(Pointables.PROCESS_USE_ENTITY, Opcodes.IXOR);
	}
	
	@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
		if (insnNode.getOpcode() == Opcodes.INSTANCEOF)
			return Pointables.ENTITY_ARROW.covers((TypeInsnNode) insnNode);
		
		return false;
	}

}
