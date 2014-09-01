package zotmc.tomahawk.transform;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import zotmc.tomahawk.data.AsmData;

public final class TransformerProcessUseEntity extends InsnCombinePointable {
	
	public TransformerProcessUseEntity() {
		super(Opcodes.IXOR);
	}
	
	@Override protected String targetClass() {
		return AsmData.NET_HANDLER_PLAY_SERVER;
	}
	
	@Override protected List<String> targetMethod() {
		return AsmData.PROCESS_USE_ENTITY;
	}
	
	@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
		return insnNode.getOpcode() == Opcodes.INSTANCEOF
				&& ((TypeInsnNode) insnNode).desc.equals(AsmData.ENTITY_ARROW_DESC);
	}

}
