package zotmc.tomahawk.transform;

import java.util.Set;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLCommonHandler;

public class MappedTransformer implements IClassTransformer {
	
	private static final Set<String> transformedTypes = Sets.newConcurrentHashSet();
	
	private final Logger log = LogManager.getFormatterLogger(LoadingPluginTomahawk.MODID);
	private final ListMultimap<String, Patcher> patchers;
	
	public MappedTransformer() {
		this.patchers = ArrayListMultimap.create();
		for (Patcher patcher : new LoadingPluginTomahawk().getPatchers())
			this.patchers.put(patcher.targetType().toString().replace('/', '.'), patcher);
	}
	
	@Override public byte[] transform(String name, String transformedName, byte[] basicClass) {
		for (Patcher p : patchers.get(transformedName))
			try {
				basicClass = p.patch(basicClass, log);
				transformedTypes.add(transformedName);
				
			} catch (Throwable t) {
				log.catching(t);
				FMLCommonHandler.instance().raiseException(t, "Fatal error.", true);
			}
		
		return basicClass;
	}
	
	Set<String> transformAll() {
		for (String s : patchers.keySet())
			try {
				Class.forName(s);
			} catch (ClassNotFoundException e) {
				throw Throwables.propagate(e);
			}
		
		return Sets.difference(patchers.keySet(), transformedTypes);
	}
	
}
