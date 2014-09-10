package zotmc.tomahawk.util.init;

import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import zotmc.tomahawk.util.MethodInfo;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public final class Messod implements Predicate<MethodInfo> {
	
	private final Typo owner;
	private final List<String> names;
	private final String desc;
	
	Messod(Typo owner, List<String> names, String desc) {
		this.owner = owner;
		this.names = names;
		this.desc = desc;
	}
	
	public Typo getOwner() {
		return owner;
	}
	
	public Messod desc(Object returnType, Object... argumentTypes) {
		checkState(desc == null);
		
		Function<Object, Type> convert = new Function<Object, Type>() { public Type apply(Object input) {
			if (input instanceof String)
				return Type.getObjectType((String) input);
			
			if (input instanceof Type)
				return (Type) input;
			
			throw new IllegalArgumentException();
		}};
		
		String newDesc = Type.getMethodDescriptor(
				convert.apply(returnType),
				FluentIterable.from(Arrays.asList(argumentTypes))
					.transform(convert)
					.toArray(Type.class)
		);
		
		return new Messod(owner, names, newDesc);
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
	
	@Deprecated @Override public boolean apply(MethodInfo input) {
		return covers(input);
	}
	public boolean covers(MethodInfo input) {
		return covers(input.getName(), input.getDescriptor());
	}
	
	public boolean covers(MethodNode node) {
		return covers(node.name, node.desc);
	}
	
	public boolean covers(String owner, String name, String desc) {
		return this.owner.covers(owner) && covers(name, desc);
	}
	
	public boolean covers(MethodInsnNode node) {
		return covers(node.owner, node.name, node.desc);
	}
	
	
	@Override public int hashCode() {
		return Objects.hash(owner, names, desc);
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Messod) {
			Messod o = (Messod) obj;
			return owner.equals(o.owner) && names.equals(o.names) && desc.equals(o.desc);
		}
		return false;
	}
	
	@Override public String toString() {
		return String.format("%s.[%s]", owner, Joiner.on(", ").join(names));
	}
	
}
