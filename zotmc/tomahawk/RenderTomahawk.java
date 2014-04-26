package zotmc.tomahawk;

import static net.minecraft.init.Items.wooden_axe;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class RenderTomahawk extends Render {
	
	private static final float ITEM_Z = 0.0625f;
	
	@Override protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
	
	private static void renderIconIn3D(IIcon i, float thickness) {
		ItemRenderer.renderItemIn2D(Tessellator.instance,
				i.getMaxU(), i.getMinV(), i.getMinU(), i.getMaxV(),
				i.getIconWidth(), i.getIconHeight(), thickness);
	}
	
	@Override public void doRender(Entity entity, double x, double y, double z, float p, float q) {
		EntityTomahawk ea = (EntityTomahawk) entity;
		ItemStack item = ea.getItem();
		if (item == null)
			return;
		
		glPushMatrix();
		{
			glTranslatef((float) x, (float) y, (float) z);
			
			glRotatef(p - 90 - ea.deltaYaw, 0, 1, 0);
			glRotatef(-90 - ea.getRotation(), 0, 0, 1);
			
			
			glTranslatef(-7/16f, -12/16f, ITEM_Z / 2);
			
			bindTexture(TextureMap.locationItemsTexture);
			
			
			int n = item.getItem().getRenderPasses(item.getItemDamage());
			for (int pass = 0; pass < n; pass++) {
				IIcon icon = item.getItem().getIcon(item, pass);
				renderIconIn3D( icon != null ? icon : wooden_axe.getIconFromDamage(0),
						ITEM_Z);
			}
		}
		glPopMatrix();
		
	}


}
