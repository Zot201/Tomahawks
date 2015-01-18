package zotmc.tomahawk.api;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.V1_6;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import zotmc.tomahawk.util.Klas;
import zotmc.tomahawk.util.KlastWriter;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.init.MethodInfo;

import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

class AsmItemHandlers {
	
	private static final String BASE_HANDLER = "baseHandler";
	private static final Type ITEM_HANDLER_TYPE = Type.getType(ItemHandler.class);
	private static final MethodInfo ASM_ITEM_HANDLER_INIT = MethodInfo.of("<init>", Type.VOID_TYPE, ITEM_HANDLER_TYPE);
	private static final Klas<? extends ItemHandler> ASM_ITEM_HANDLER_TYPE;
	
	static {
		String s = AsmItemHandlers.class.getName();
		KlastWriter cw = new KlastWriter(Klas.ofName(s.substring(0, s.length() - 1)), Klas.ofClass(Object.class));
		
		cw.visit(V1_6, ACC_SUPER, null, ITEM_HANDLER_TYPE.getInternalName());
		cw.visitSource(".dynamic", null);
		
		cw.visitField(0, BASE_HANDLER, ITEM_HANDLER_TYPE.getDescriptor(), null, null);
		
		{
			GeneratorAdapter mg = new GeneratorAdapter(0, ASM_ITEM_HANDLER_INIT, null, null, cw);
			mg.loadThis();
			mg.invokeConstructor(Type.getType(Object.class), MethodInfo.of("<init>", Type.VOID_TYPE));
			mg.loadThis();
			mg.loadArgs();
			mg.putField(cw.target.toType(), BASE_HANDLER, ITEM_HANDLER_TYPE);
			mg.returnValue();
			mg.endMethod();
		}
		
		cw.visitEnd();
		ASM_ITEM_HANDLER_TYPE = Klas.ofClass(Utils.<ItemHandler>defineClass(cw.target.toName(), cw.toByteArray()));
	}
	
	private static int id;
	private static Klas<?> getUniqueName() {
		return Klas.ofName(ASM_ITEM_HANDLER_TYPE.toName() + "_" + id++);
	}
	
	static ItemHandler create(ItemHandler baseHandler, Map<Class<? extends Annotation>, Delegation> delegations) {
		KlastWriter cw = new KlastWriter(getUniqueName(), ASM_ITEM_HANDLER_TYPE);
		
		cw.visit(V1_6, ACC_SUPER, null);
		cw.visitSource(".dynamic", null);
		
		List<Class<?>> types = Lists.<Class<?>>newArrayList(ItemHandler.class);
		List<Object> objects = Lists.<Object>newArrayList(baseHandler);
		
		Iterator<Entry<Class<? extends Annotation>, Delegation>> itr = delegations.entrySet().iterator();
		for (int i = 0; itr.hasNext(); i++) {
			Entry<Class<? extends Annotation>, Delegation> entry = itr.next();
			entry.getValue().acceptInitArgs(types, objects);
			entry.getValue().acceptField(cw, i);
			Method m = ItemHandler.ANNOTATION_MAP.get(entry.getKey());
			entry.getValue().acceptMethod(cw, i, m);
		}
		
		for (Method method : Maps.filterKeys(ItemHandler.ANNOTATION_MAP, Utils.notIn(delegations.keySet())).values()) {
			MethodInfo m = MethodInfo.of(method);
			GeneratorAdapter mg = new GeneratorAdapter(ACC_PUBLIC, m, null, null, cw);
			mg.loadThis();
			mg.getField(cw.target.toType(), BASE_HANDLER, ITEM_HANDLER_TYPE);
			mg.loadArgs();
			mg.invokeInterface(ITEM_HANDLER_TYPE, m);
			mg.returnValue();
			mg.endMethod();
		}
		
		{
			MethodInfo m = MethodInfo.of("<init>",
					Type.VOID_TYPE,
					FluentIterable.from(types)
						.transform(Utils.asmTypeAdaptor())
						.toArray(Type.class)
			);
			GeneratorAdapter mg = new GeneratorAdapter(ACC_PRIVATE, m, null, null, cw);
			mg.loadThis();
			mg.loadArg(0);
			mg.invokeConstructor(cw.parent.toType(), ASM_ITEM_HANDLER_INIT);
			
			Iterator<Delegation> itr1 = delegations.values().iterator();
			for (int i = 0; itr1.hasNext(); i++)
				itr1.next().acceptInit(mg, i, cw.target.toType(), i + 1);
			
			mg.returnValue();
			mg.endMethod();
		}
		
		cw.visitEnd();
		Class<? extends ItemHandler> clz = Utils.defineClass(cw.target.toName(), cw.toByteArray());
		
		try {
			Constructor<? extends ItemHandler> ctor = clz.getDeclaredConstructor(Iterables.toArray(types, Class.class));
			ctor.setAccessible(true);
			return ctor.newInstance(objects.toArray());
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
}
