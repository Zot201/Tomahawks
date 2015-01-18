package zotmc.tomahawk.transform;

import net.minecraft.launchwrapper.Launch;

import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class AbstractMethodPatcher implements Patcher {
	
	final MethodPredicate target;
	
	public AbstractMethodPatcher(MethodPredicate target) {
		this.target = target;
	}
	
	@Override public TypePredicate targetType() {
		return target.getOwner();
	}
	
	protected boolean useMcpNames() {
		return (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}
	
	protected void addMethod(ClassNode classNode) { }
	
	protected abstract void processMethod(MethodNode targetMethod);
	
	
	@Override public byte[] patch(byte[] basicClass, Logger log) throws Throwable {
		ClassNode classNode = new ClassNode();
		new ClassReader(basicClass).accept(classNode, 0);
		
		MethodNode targetNode = null;
		boolean newlyCreated = false;
		
		for (MethodNode methodNode : classNode.methods)
			if (target.covers(methodNode)) {
				targetNode = methodNode;
				break;
			}
		
		if (targetNode == null) {
			addMethod(classNode);
			
			for (MethodNode methodNode : classNode.methods)
				if (target.covers(methodNode)) {
					targetNode = methodNode;
					break;
				}
			
			if (targetNode == null) throw new MissingMethodException(target);
			
			log.info("Added %s.", target);
			newlyCreated = true;
		}
		
		return processMethod(newlyCreated, classNode, targetNode, log);
	}
	
	byte[] processMethod(boolean newlyCreated, ClassNode classNode, MethodNode targetMethod, Logger log) {
		processMethod(targetMethod);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(cw);
		
		if (!newlyCreated) log.info("Processed %s.", target);
		return cw.toByteArray();
	}
	
}
