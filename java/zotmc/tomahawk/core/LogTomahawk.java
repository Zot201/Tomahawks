package zotmc.tomahawk.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.data.ModData.AxeTomahawk;

import com.google.common.reflect.Reflection;

public class LogTomahawk {
	
	public enum LogCategory {
		ENT, PHY, REN, API;
		
		private Logger logger;
		public Logger getLogger() {
			return logger != null ? logger : (logger = createLogger(this));
		}
		
		public boolean isDebugEnabled() {
			return Config.current().debugLoggings.get().contains(this);
		}
	}
	
	public static Logger ent4j() {
		return LogCategory.ENT.getLogger();
	}
	public static Logger phy4j() {
		return LogCategory.PHY.getLogger();
	}
	public static Logger ren4j() {
		return LogCategory.REN.getLogger();
	}
	public static Logger api4j() {
		return LogCategory.API.getLogger();
	}
	
	private static Logger createLogger(final LogCategory cat) {
		final Logger delegatee = LogManager.getFormatterLogger(AxeTomahawk.CORE_MODID + "." + cat);
		
		return Reflection.newProxy(Logger.class, new InvocationHandler() {
			@Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (method.getName().equals("debug"))
					return !cat.isDebugEnabled() ? null : Logger.class
							.getDeclaredMethod("info", method.getParameterTypes())
							.invoke(delegatee, args);
				
				if (args.length > 0 && args[0] == Level.DEBUG)
					if (!cat.isDebugEnabled())
						return null;
					else
						args[0] = Level.INFO;
				return method.invoke(delegatee, args);
			}
		});
	}
	
}
