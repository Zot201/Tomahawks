package zotmc.tomahawk;

import static zotmc.tomahawk.LogTomahawk.LogCategory.MOB;
import static zotmc.tomahawk.LogTomahawk.LogCategory.PHY;
import static zotmc.tomahawk.LogTomahawk.LogCategory.PROJ;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zotmc.tomahawk.config.Config;

public class LogTomahawk {
	
	public enum LogCategory {
		MOB, PROJ, PHY;
	}
	
	private static Logger mob, proj, phy;
	
	
	public static Logger mob4j() {
		return mob != null ? mob : (mob = create(MOB));
	}
	
	public static Logger pro4j() {
		return proj != null ? proj : (proj = create(PROJ));
	}
	
	public static Logger phy4j() {
		return phy != null ? phy : (phy = create(PHY));
	}
	
	
	public static boolean isDebugEnabled(LogCategory cat, Object[] args) {
		return Config.current().debugLoggings.get().contains(cat);
	}
	
	
	private static Logger create(final LogCategory cat) {
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
