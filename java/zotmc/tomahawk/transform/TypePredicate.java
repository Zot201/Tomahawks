package zotmc.tomahawk.transform;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public final class TypePredicate {
	
	private final String name;
	
	private TypePredicate(String name) {
		this.name = name;
	}
	
	public static TypePredicate of(String internalName) {
		return new TypePredicate(internalName);
	}
	
	public MethodPredicate method(String... names) {
		return new MethodPredicate(this, ImmutableList.copyOf(names), null);
	}
	
	
	public boolean covers(String typeDesc) {
		return name.equals(typeDesc) || FMLDeobfuscatingRemapper.INSTANCE.unmap(name).equals(typeDesc);
	}
	
	public boolean covers(int opcode, AbstractInsnNode insnNode) {
		return insnNode.getOpcode() == opcode && covers(((TypeInsnNode) insnNode).desc);
	}
	
	
	@Override public int hashCode() {
		return name.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		return obj instanceof TypePredicate && name.equals(((TypePredicate) obj).name);
	}
	
	/**
	 * Mapped internal name
	 */
	@Override public String toString() {
		return name;
	}
	
}
