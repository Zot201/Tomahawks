package zotmc.tomahawk;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	
	@Override void registerRenderer() {
		RenderingRegistry.registerEntityRenderingHandler(EntityTomahawk.class, new RenderTomahawk());
	}
	
}
