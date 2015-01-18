package zotmc.tomahawk.transform;

import static com.google.common.base.Preconditions.checkState;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import zotmc.tomahawk.util.init.MethodInfo;

public final class InsnListBuilder extends InstructionAdapter {
	
	public InsnListBuilder() {
		super(Opcodes.ASM4, new MethodNode());
	}
	
	public InsnList build() {
		MethodNode mn = (MethodNode) mv;
		InsnList ret = mn.instructions;
		mn.instructions = null;
		checkState(ret != null);
		return ret;
	}
	
	public void pop(int counts) {
		while (counts >= 2) {
			pop2();
			counts -= 2;
		}
		if (counts > 0) pop();
	}
	
	public void aload(int var) {
		visitVarInsn(Opcodes.ALOAD, var);
	}
	
	public void iload(int var) {
		visitVarInsn(Opcodes.ILOAD, var);
	}
	
	public void fload(int var) {
		visitVarInsn(Opcodes.FLOAD, var);
	}
	
	public void _return() {
		visitInsn(Opcodes.RETURN);
	}
	
	public void ireturn() {
		visitInsn(Opcodes.IRETURN);
	}
	
	public void iadd() {
		visitInsn(Opcodes.IADD);
	}
	
	public void ixor() {
		visitInsn(Opcodes.IXOR);
	}
	
	public void ior() {
		visitInsn(Opcodes.IOR);
	}
	
	public void instanceOf(TypePredicate type) {
		visitTypeInsn(Opcodes.INSTANCEOF, type.toString());
	}
	
	public void invokestatic(MethodPredicate method, boolean itf) {
		MethodInfo m = method.toMethodInfo();
		invokestatic(method.getOwner().toString(), m.getName(), m.getDescriptor());
	}
	
	public void invokevirtual(MethodPredicate method, boolean itf) {
		MethodInfo m = method.toMethodInfo();
		invokevirtual(method.getOwner().toString(), m.getName(), m.getDescriptor());
	}
	
	public void getstatic(MethodPredicate field) {
		MethodInfo m = field.toMethodInfo();
		getstatic(field.getOwner().toString(), m.getName(), m.getDescriptor());
	}
	
	public void getfield(MethodPredicate field) {
		MethodInfo m = field.toMethodInfo();
		getfield(field.getOwner().toString(), m.getName(), m.getDescriptor());
	}
	
}
