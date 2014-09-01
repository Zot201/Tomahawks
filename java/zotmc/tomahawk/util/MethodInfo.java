package zotmc.tomahawk.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

public class MethodInfo extends Method {
	
	private MethodInfo(String name, String desc) {
		super(name, desc);
	}
	
	public static MethodInfo of(String name, String desc) {
		return new MethodInfo(name, desc);
	}
	public static MethodInfo of(String name, Type returnType, Type... argumentTypes) {
		return new MethodInfo(name, Type.getMethodDescriptor(returnType, argumentTypes));
	}
	public static MethodInfo of(Method method) {
		return new MethodInfo(method.getName(), method.getDescriptor());
	}
	public static MethodInfo of(java.lang.reflect.Method method) {
		return new MethodInfo(method.getName(), Type.getMethodDescriptor(method));
	}
	
}
