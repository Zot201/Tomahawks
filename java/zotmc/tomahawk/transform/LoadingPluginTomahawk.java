package zotmc.tomahawk.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zotmc.tomahawk.data.AsmData;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LoadingPluginTomahawk implements IFMLLoadingPlugin {
	
	static final String MODID = AsmData.CORE_MODID;
	
	@Override public String[] getASMTransformerClass() {
		return new String[] {
				MappedTransformer.class.getName()
		};
	}
	
	@Override public String getModContainerClass() {
		return null;
	}
	
	@Override public String getSetupClass() {
		return null;
	}
	
	@Override public void injectData(Map<String, Object> data) { }
	
	@Override public String getAccessTransformerClass() {
		return null;
	}
	
	Iterable<Patcher> getPatchers() {
		return new Iterable<Patcher>() { public Iterator<Patcher> iterator() {
			return new AbstractIterator<Patcher>() {
				final boolean isClient = FMLLaunchHandler.side().isClient();
				final Class<?>[] classes = AsmData.class.getDeclaredClasses();
				int i = 0;
				
				Field[] fields = new Field[0];
				int j = 0;
				
				@Override protected Patcher computeNext() {
					while (true) {
						if (j < fields.length) {
							Field f = fields[j++];
							if (!Patcher.class.isAssignableFrom(f.getType())) continue;
							if (!isClient && f.getAnnotation(ClientOnly.class) != null) continue;
							
							try {
								f.setAccessible(true);
								return (Patcher) f.get(null);
							} catch (Throwable t) {
								Throwables.propagate(t);
							}
						}
						
						if (i < classes.length) {
							Class<?> clz = classes[i++];
							if (!isClient && clz.getAnnotation(ClientOnly.class) != null) continue;
							
							fields = clz.getDeclaredFields();
							j = 0;
							continue;
						}
						
						return endOfData();
					}
				}
			};
		}};
	}
	
	
	public static class Validating {
		private final Logger log = LogManager.getFormatterLogger(MODID);
		
		private Validating() {
			Set<String> errored = new MappedTransformer().transformAll();
			if (!errored.isEmpty()) {
				List<String> msg = ImmutableList.of(
						"Found type(s) being loaded before they can be transformed.",
						"Loading cannot be proceeded without causing in-game errors.",
						"Please tell your core mod authors to fix it.",
						"",
						"Type(s) affected:"
				);
				IllegalStateException e =
						new IllegalStateException(Joiner.on(" ").join(msg) + " " + errored.toString());
				
				throw FMLLaunchHandler.side().isServer() ? e : getClientException(e, errored, msg);
			}
			
			boolean removed = Iterables.removeIf(getEntireTransformerList(), Predicates.instanceOf(MappedTransformer.class));
			log.info(removed ? "Removed effected transformers." : "Failed to retrieve effected transformers.");
		}
		
		@SuppressWarnings("unchecked")
		private List<IClassTransformer> getEntireTransformerList() {
			try {
				ClassLoader cl = LoadingPluginTomahawk.class.getClassLoader();
				Field f = cl.getClass().getDeclaredField("transformers");
				f.setAccessible(true);
				return (List<IClassTransformer>) f.get(cl);
				
			} catch (Throwable t) {
				log.catching(t);
			}
			
			return ImmutableList.of();
		}
		
		@SideOnly(Side.CLIENT)
		private RuntimeException getClientException(
				IllegalStateException cause, Set<String> errored, List<String> msg) {
			
			try {
				// prevent directly calling new of a side only class.
				Constructor<? extends RuntimeException> ctor = TypesAlreadyLoadedErrorDisplayException.class
						.getConstructor(IllegalStateException.class, Set.class, List.class);
				
				return ctor.newInstance(cause, errored, msg);
				
			} catch (Throwable t) {
				throw Throwables.propagate(t);
			}
		}
	}
	
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.FIELD})
	public @interface ClientOnly { }
	
}
