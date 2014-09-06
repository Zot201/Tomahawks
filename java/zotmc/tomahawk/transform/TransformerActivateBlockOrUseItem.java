package zotmc.tomahawk.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import zotmc.tomahawk.data.AsmData.SetHits;

public class TransformerActivateBlockOrUseItem extends InsnCombine {
	
	public TransformerActivateBlockOrUseItem() {
		super(SetHits.ACTIVATE_BLOCK_OR_USE_ITEM);
	}
	
	@Override protected boolean isTargetInsn(AbstractInsnNode insnNode) {
		if (insnNode.getOpcode() == Opcodes.INVOKESTATIC)
			return SetHits.ON_PLAYER_INTERACT.covers((MethodInsnNode) insnNode);
		
		return false;
	}
	
	@Override protected void combine(InsnList list, AbstractInsnNode insnNode) {
		InsnList before = new InsnList();
		before.add(new VarInsnNode(Opcodes.FLOAD, 8));
		before.add(new VarInsnNode(Opcodes.FLOAD, 9));
		before.add(new VarInsnNode(Opcodes.FLOAD, 10));
		before.add(SetHits.INVOKE_SET_HIT.get());
		list.insertBefore(insnNode, before);
	}
	
}
