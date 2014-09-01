package zotmc.tomahawk.transform;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class LoadingPluginTomahawk implements IFMLLoadingPlugin {
	
	@Override public String[] getASMTransformerClass() {
		return new String[] {
				TransformerProcessUseEntity.class.getName(),
				TransformerGetMouseOver.class.getName()
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
	
	
	static Set<String> transformed = Sets.newHashSet();
	
	private static void postInit() {
		for (String s : new LoadingPluginTomahawk().getASMTransformerClass())
			try {
				InsnCombinePointable t = (InsnCombinePointable)
						Class.forName(s).getConstructor().newInstance();
				t.checkTranformation();
				
			} catch (Throwable e) {
				throw Throwables.propagate(e);
			}
		
		transformed = null;
	}
	
}
