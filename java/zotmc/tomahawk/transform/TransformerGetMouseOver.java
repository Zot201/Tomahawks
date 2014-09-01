package zotmc.tomahawk.transform;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import zotmc.tomahawk.data.AsmData;

public final class TransformerGetMouseOver extends InsnCombinePointable {
	
	public TransformerGetMouseOver() {
		super(Opcodes.IOR);
	}
	
	@Override protected String targetClass() {
		return AsmData.ENTITY_RENDERER;
	}
	
	@Override protected List<String> targetMethod() {
		return AsmData.GET_MOUSE_OVER;
	}
	
	@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
		if (insnNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
			MethodInsnNode m = (MethodInsnNode) insnNode;
			return AsmData.CAN_BE_COLLIDE_WITH.contains(m.name)
					&& m.owner.equals(AsmData.ENTITY_DESC)
					&& AsmData.CAN_BE_COLLIDE_WITH_DESC.contains(m.desc);
		}
		
		if (insnNode.getOpcode() == Opcodes.INSTANCEOF)
			return ((TypeInsnNode) insnNode).desc.equals(AsmData.ENTITY_ITEM_FRAME_DESC);
		
		return false;
	}
	
}
