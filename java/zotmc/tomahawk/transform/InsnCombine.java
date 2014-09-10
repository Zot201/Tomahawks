package zotmc.tomahawk.transform;

import static com.google.common.base.Preconditions.checkState;
import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import zotmc.tomahawk.data.AsmData;
import zotmc.tomahawk.util.init.Messod;

import com.google.common.base.Throwables;

abstract class InsnCombine implements IClassTransformer {
	
	private final Logger log = LogManager.getFormatterLogger(AsmData.CORE_MODID);
	private final Messod target;
	
	public InsnCombine(Messod target) {
		this.target = target;
	}
	
	protected abstract boolean isTargetInsn(AbstractInsnNode insnNode);
	
	
	@Override public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			return !target.getOwner().isClass(transformedName) ? basicClass : patch(basicClass);
		} catch (Throwable e) {
			log.catching(e);
			throw Throwables.propagate(e);
		}
	}
	
	protected abstract void combine(InsnList list, AbstractInsnNode insnNode);
	
	protected byte[] patch(byte[] basicClass) throws Throwable {
		log.info("Patching %s...", target);
		
		ClassNode classNode = new ClassNode();
		new ClassReader(basicClass).accept(classNode, 0);
		
		for (MethodNode methodNode : classNode.methods)
			if (target.covers(methodNode)) {
				InsnList list = methodNode.instructions;
				
				int count = 0;
				for (AbstractInsnNode insnNode : list.toArray())
					if (isTargetInsn(insnNode)) {
						combine(list, insnNode);
						count++;
					}
				
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				classNode.accept(cw);
				
				LoadingPluginTomahawk.transformed.add(target.getOwner());
				log.info("Wrapped %d instruction%s", count, count == 1 ? "" : "s");
				return cw.toByteArray();
			}
		
		log.error("Failed to patch %s", target);
		return basicClass;
	}
	
	
	void checkTranformation() {
		try {
			target.getOwner().initialize();
			checkState(LoadingPluginTomahawk.transformed.contains(target.getOwner()), "Failed to patch %s", target);
		} catch (ClassNotFoundException ignored) { }
	}
	
}
