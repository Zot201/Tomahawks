package zotmc.tomahawk.transform;

import static com.google.common.base.Preconditions.checkState;
import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import zotmc.tomahawk.data.AsmData;
import zotmc.tomahawk.util.Messod;
import zotmc.tomahawk.util.Typo;

import com.google.common.base.Throwables;

abstract class InsnCombinePointable implements IClassTransformer {
	
	private final Logger log = LogManager.getFormatterLogger(AsmData.MODID);
	
	private final int combiningOpcode;
	private final Typo targetType;
	private final Messod targetMethod;
	
	protected InsnCombinePointable(int combiningOpcode, Typo targetType, Messod targetMethod) {
		this.combiningOpcode = combiningOpcode;
		this.targetType = targetType;
		this.targetMethod = targetMethod;
	}
	
	protected abstract boolean isTargetInsn(AbstractInsnNode insnNode);
	
	
	@Override public byte[] transform(String name, String transformedName, byte[] basicClass) {
		try {
			return !targetType.isClass(transformedName) ? basicClass : patch(basicClass);
		} catch (Throwable e) {
			log.catching(e);
			throw Throwables.propagate(e);
		}
	}
	
	protected byte[] patch(byte[] basicClass) throws Throwable {
		log.info("Patching %s", targetMethod);
		
		ClassNode classNode = new ClassNode();
		new ClassReader(basicClass).accept(classNode, 0);

		for (MethodNode methodNode : classNode.methods)
			if (targetMethod.covers(methodNode)) {
				InsnList list = methodNode.instructions;
				
				int count = 0;
				for (AbstractInsnNode insnNode : list.toArray())
					if (isTargetInsn(insnNode)) {
						list.insertBefore(insnNode, new InsnNode(Opcodes.DUP));
						InsnList after = new InsnList();
						after.add(new InsnNode(Opcodes.SWAP));
						after.add(new TypeInsnNode(Opcodes.INSTANCEOF, AsmData.POINTABLE_DESC));
						after.add(new InsnNode(combiningOpcode));
						list.insert(insnNode, after);
						count++;
					}
				
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				classNode.accept(cw);
				
				LoadingPluginTomahawk.transformed.add(targetType);
				log.info("Wrapped %d instruction%s in %s", count, count == 1 ? "" : "s", targetMethod);
				return cw.toByteArray();
			}
		
		log.error("Failed to patch %s", targetMethod);
		return basicClass;
	}
	
	
	void checkTranformation() {
		try {
			targetType.toClass();
			checkState(LoadingPluginTomahawk.transformed.contains(targetType), "Failed to patch %s", targetMethod);
		} catch (ClassNotFoundException ignored) { }
	}

}
