package zotmc.tomahawk.transform;

import java.util.Map;
import java.util.Set;

import zotmc.tomahawk.core.TomahawksCore.PostInitialization;
import zotmc.tomahawk.util.init.Typo;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions({"zotmc.tomahawk.transfrom", "zotmc.tomahawk.data.AsmData", "zotmc.tomahawk.util.init"})
public class LoadingPluginTomahawk implements IFMLLoadingPlugin {
	
	@Override public String[] getASMTransformerClass() {
		return new String[] {
				TransformerProcessUseEntity.class.getName(),
				TransformerGetMouseOver.class.getName(),
				TransformerActivateBlockOrUseItem.class.getName()
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
	
	
	static final Set<Typo> transformed = Sets.newHashSet();
	
	public static class Subscriber {
		private Subscriber() { }
		@SubscribeEvent public void postInit(PostInitialization event) {
			for (String s : new LoadingPluginTomahawk().getASMTransformerClass())
				try {
					Object o = Class.forName(s).getConstructor().newInstance();
					if (o instanceof InsnCombine)
						((InsnCombine) o).checkTranformation();
					
				} catch (Throwable e) {
					throw Throwables.propagate(e);
				}
		}
	}
	
}
