package zotmc.tomahawk.api;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import zotmc.tomahawk.util.KlastWriter;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.init.MethodInfo;

import com.google.common.base.Throwables;

public class Delegation {
	
	private static final MethodInfo INVOKE =
			MethodInfo.of(Utils.getDeclaredMethod(Delegation.class, "invoke", Object[].class));
	
	final Object delegatee;
	final Method method;
	private final Method asmMethod;
	
	Delegation(Object delegatee, Method method) {
		this.delegatee = delegatee;
		this.method = method;
		method.setAccessible(true);
		asmMethod = Utils.getPublic(method);
	}
	
	public Object invoke(Object... args) {
		try {
			return method.invoke(delegatee, args);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	@Override public String toString() {
		return method.getName();
	}
	
	
	void acceptField(KlastWriter cw, int i) {
		cw.visitField(ACC_PRIVATE, delegateeOf(i), Type.getDescriptor(asmFieldType()), null, null);
	}
	
	void acceptInitArgs(List<Class<?>> types, List<Object> objects) {
		types.add(asmFieldType());
		objects.add(asmMethod != null ? delegatee : this);
	}
	
	void acceptInit(GeneratorAdapter mg, int i, Type owner, int argumentIndex) {
		mg.loadThis();
		mg.loadArg(argumentIndex);
		mg.putField(owner, delegateeOf(i), Type.getType(asmFieldType()));
	}
	
	void acceptMethod(KlastWriter cw, int i, Method target) {
		MethodInfo m = MethodInfo.of(target);
		GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
		
		mg.loadThis();
		Class<?> fieldType = asmFieldType();
		Type type = Type.getType(fieldType);
		mg.getField(cw.target.toType(), delegateeOf(i), type);
		
		if (asmMethod != null) {
			mg.loadArgs();
			
			if (fieldType.isInterface())
				mg.invokeInterface(type, MethodInfo.of(asmMethod));
			else
				mg.invokeVirtual(type, MethodInfo.of(asmMethod));
			
			if (!target.getReturnType().isAssignableFrom(asmMethod.getReturnType()))
				mg.checkCast(m.getReturnType());
		}
		else {
			mg.loadArgArray();
			mg.invokeVirtual(type, INVOKE);
			mg.unbox(m.getReturnType());
		}
		
		mg.returnValue();
		mg.endMethod();
	}
	
	
	private static String delegateeOf(int i) {
		return "delegatee" + i;
	}
	
	private Class<?> asmFieldType() {
		return asmMethod != null ? asmMethod.getDeclaringClass() : Delegation.class;
	}
	
}
