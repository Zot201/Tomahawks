package zotmc.tomahawk.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import zotmc.tomahawk.data.AsmData;

public final class TransformerProcessUseEntity extends InsnCombinePointable {
	
	public TransformerProcessUseEntity() {
		super(Opcodes.IXOR, AsmData.NET_HANDLER_PLAY_SERVER, AsmData.PROCESS_USE_ENTITY);
	}
	
	@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
		if (insnNode.getOpcode() == Opcodes.INSTANCEOF)
			return AsmData.ENTITY_ARROW.covers((TypeInsnNode) insnNode);
		
		return false;
	}

}
