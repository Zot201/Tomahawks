package zotmc.tomahawk;

import zotmc.tomahawk.projectile.EntityTomahawk;
import zotmc.tomahawk.projectile.RenderTomahawk;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	
	@Override void registerRenderer() {
		RenderingRegistry.registerEntityRenderingHandler(
				EntityTomahawk.class, new RenderTomahawk());
		
	}
	
}
