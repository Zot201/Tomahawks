package zotmc.tomahawk.transform;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public abstract class AbstractInsnPatcher extends AbstractMethodPatcher {
	
	public AbstractInsnPatcher(MethodPredicate target) {
		super(target);
	}
	
	protected abstract boolean isTargetInsn(AbstractInsnNode insnNode);
	
	protected abstract void processInsn(InsnList list, AbstractInsnNode targetInsn);
	
	@Override protected void processMethod(MethodNode targetMethod) { }
	
	
	@Override byte[] processMethod(boolean newlyCreated, ClassNode classNode, MethodNode targetMethod, Logger log) {
		processMethod(targetMethod);
		InsnList list = targetMethod.instructions;
		
		int count = 0;
		for (AbstractInsnNode insnNode : list.toArray())
			if (isTargetInsn(insnNode)) {
				processInsn(list, insnNode);
				count++;
			}
		
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(cw);
		
		if (!newlyCreated) log.info("Processed %d insn%s in %s.", count, count == 1 ? "" : "s", target);
		return cw.toByteArray();
	}
	
}
