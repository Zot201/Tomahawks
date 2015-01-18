package zotmc.tomahawk.transform;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import zotmc.tomahawk.util.init.MethodInfo;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public final class MethodPredicate {
	
	private final TypePredicate owner;
	private final List<String> names;
	private final String desc;
	
	MethodPredicate(TypePredicate owner, List<String> names, String desc) {
		this.owner = owner;
		this.names = names;
		this.desc = desc;
	}
	
	public MethodPredicate desc(String desc) {
		checkState(this.desc == null);
		return new MethodPredicate(owner, names, desc);
	}
	
	public TypePredicate getOwner() {
		return owner;
	}
	
	public MethodInfo toMethodInfo() {
		checkState(names.size() == 1);
		return toMethodInfo(0);
	}
	
	public MethodInfo toMethodInfo(int nameIndex) {
		checkState(desc != null);
		return MethodInfo.of(names.get(nameIndex), desc);
	}
	
	
	public boolean covers(String name, String desc) {
		String owner = FMLDeobfuscatingRemapper.INSTANCE.unmap(this.owner.toString());
		String mappedName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner, name, desc);
		String mappedDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(desc);
		
		if (this.desc == null || this.desc.equals(mappedDesc))
			for (String s : names)
				if (s.equals(mappedName))
					return true;
		
		return false;
	}
	
	public boolean covers(MethodNode node) {
		return covers(node.name, node.desc);
	}
	
	public boolean covers(String owner, String name, String desc) {
		return this.owner.covers(owner) && covers(name, desc);
	}
	
	public boolean covers(MethodInsnNode insnNode) {
		return covers(insnNode.owner, insnNode.name, insnNode.desc);
	}
	
	public boolean covers(int opcode, AbstractInsnNode insnNode) {
		return insnNode.getOpcode() == opcode && covers((MethodInsnNode) insnNode);
	}
	
	
	@Override public int hashCode() {
		return Objects.hashCode(owner, names, desc);
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof MethodPredicate) {
			MethodPredicate o = (MethodPredicate) obj;
			return owner.equals(o.owner) && names.equals(o.names) && Objects.equal(desc, o.desc);
		}
		return false;
	}
	
	@Override public String toString() {
		return String.format("%s/[%s]", owner, Joiner.on(", ").join(names));
	}
	
}
