package zotmc.tomahawk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogTomahawk {
	
	private static Logger mob, proj, phy;
	
	public static Logger mob4j() {
		return mob != null ? mob : (mob = create("mob"));
	}
	public static Logger pro4j() {
		return proj != null ? proj : (proj = create("proj"));
	}
	public static Logger phy4j() {
		return phy != null ? phy : (phy = create("phy"));
	}
	
	public static boolean isDebugEnabled(String cat, Object[] args) {
		return false;
	}
	
	private static Logger create(final String cat) {
		return (Logger) Proxy.newProxyInstance(LogTomahawk.class.getClassLoader(),
				new Class[] {Logger.class}, new InvocationHandler() {
			
			final Logger delegatee = LogManager.getFormatterLogger(Tomahawk.MODID + "." + cat);
			
			@Override public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				
				if (method.getName().equals("debug"))
					return !isDebugEnabled(cat, args) ? null : Logger.class
							.getDeclaredMethod("info", method.getParameterTypes())
							.invoke(delegatee, args);
				
				if (args.length > 0 && args[0] == Level.DEBUG)
					if (!isDebugEnabled(cat, args))
						return null;
					else
						args[0] = Level.INFO;
				
				return method.invoke(delegatee, args);
			}
		});
		
	}
	
}
