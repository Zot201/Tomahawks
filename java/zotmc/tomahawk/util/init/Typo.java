package zotmc.tomahawk.util.init;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.TypeInsnNode;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public final class Typo implements Predicate<Type> {
	
	private final String name, clz;
	
	private Typo(String name) {
		this.name = name;
		this.clz = name.replace('/', '.');
	}
	
	public static Typo of(String internalName) {
		return new Typo(internalName);
	}
	
	public Messod mess(String... names) {
		return new Messod(this, ImmutableList.copyOf(names), null);
	}
	
	
	@Deprecated @Override public boolean apply(Type input) {
		return covers(input);
	}
	public boolean covers(Type type) {
		return covers(type.getInternalName());
	}
	
	public boolean covers(String desc) {
		return name.equals(desc) || FMLDeobfuscatingRemapper.INSTANCE.unmap(name).equals(desc);
	}
	
	public boolean covers(TypeInsnNode node) {
		return covers(node.desc);
	}
	
	public boolean isClass(String transformedName) {
		return clz.equals(transformedName);
	}
	
	public void initialize() throws ClassNotFoundException {
		Class.forName(clz);
	}
	
	
	@Override public int hashCode() {
		return name.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		return obj instanceof Typo && name.equals(((Typo) obj).name);
	}
	
	/**
	 * Mapped internal name
	 */
	@Override public String toString() {
		return name;
	}
	
}
