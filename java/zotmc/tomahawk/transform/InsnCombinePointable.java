package zotmc.tomahawk.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import zotmc.tomahawk.data.AsmData.Pointables;
import zotmc.tomahawk.util.init.Messod;

abstract class InsnCombinePointable extends InsnCombine {
	
	private final int combiningOpcode;
	
	protected InsnCombinePointable(Messod target, int combiningOpcode) {
		super(target);
		this.combiningOpcode = combiningOpcode;
	}
	
	@Override protected void combine(InsnList list, AbstractInsnNode insnNode) {
		list.insertBefore(insnNode, new InsnNode(Opcodes.DUP));
		InsnList after = new InsnList();
		after.add(new InsnNode(Opcodes.SWAP));
		after.add(new TypeInsnNode(Opcodes.INSTANCEOF, Pointables.POINTABLE_DESC));
		after.add(new InsnNode(combiningOpcode));
		list.insert(insnNode, after);
	}
	
}
