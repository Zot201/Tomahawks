package zotmc.tomahawk.util;

import org.objectweb.asm.ClassWriter;

public class KlastWriter extends ClassWriter {
	
	public final Klas<?> target, parent;

	public KlastWriter(Klas<?> target, Klas<?> parent) {
		super(COMPUTE_FRAMES | COMPUTE_MAXS);
		this.target = target;
		this.parent = parent;
	}
	
	public final void visit(final int version, final int access, final String signature, final String... interfaces) {
		visit(version, access, target.toType().getInternalName(), signature,
				parent.toType().getInternalName(), interfaces);
	}
	
}
