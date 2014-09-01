package zotmc.tomahawk.transform;

import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

import java.util.List;

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

import com.google.common.base.Joiner;

abstract class InsnCombinePointable implements IClassTransformer {
	
	private final Logger log = LogManager.getFormatterLogger(AsmData.MODID);
	private final int combiningOpcode;
	
	protected InsnCombinePointable(int combiningOpcode) {
		this.combiningOpcode = combiningOpcode;
	}
	
	protected abstract String targetClass();
	
	protected abstract List<String> targetMethod();
	
	protected abstract boolean isTargetInsn(AbstractInsnNode insnNode);
	
	void checkTranformation() {
		try {
			Class.forName(targetClass());
			checkState(LoadingPluginTomahawk.transformed.contains(targetClass()));
		} catch (ClassNotFoundException ignored) { }
	}
	
	
	
	@Override public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return !transformedName.equals(targetClass()) ? basicClass : patch(basicClass);
	}
	
	private byte[] patch(byte[] basicClass) {
		log.info("Patching %s.{%s}...", targetClass(), Joiner.on(", ").join(targetMethod()));
		
		ClassNode classNode = new ClassNode();
		new ClassReader(basicClass).accept(classNode, 0);
		
		for (MethodNode methodNode : classNode.methods)
			if (targetMethod().contains(methodNode.name)) {
				InsnList list = methodNode.instructions;
				
				for (AbstractInsnNode insnNode : list.toArray())
					if (isTargetInsn(insnNode)) {
						list.insertBefore(insnNode, new InsnNode(Opcodes.DUP));
						InsnList after = new InsnList();
						after.add(new InsnNode(Opcodes.SWAP));
						after.add(new TypeInsnNode(Opcodes.INSTANCEOF, AsmData.POINTABLE_DESC));
						after.add(new InsnNode(combiningOpcode));
						list.insert(insnNode, after);
					}
				
				ClassWriter cw = new ClassWriter(COMPUTE_FRAMES | COMPUTE_MAXS);
				classNode.accept(cw);
				
				LoadingPluginTomahawk.transformed.add(targetClass());
				return cw.toByteArray();
			}
		
		String msg = String.format("Failed to patch %s.{%s}", targetClass(), Joiner.on(", ").join(targetMethod()));
		throw new RuntimeException(msg);
	}

}
