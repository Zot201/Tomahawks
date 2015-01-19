package zotmc.tomahawk.projectile;

import static net.minecraft.client.renderer.ItemRenderer.renderItemIn2D;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import zotmc.tomahawk.projectile.EntityTomahawk.State;

public class RenderTomahawk extends Render {
	
	private static final float ITEM_Z = 0.0625f;
	private static final ResourceLocation RES_ITEM_GLINT =
			new ResourceLocation("textures/misc/enchanted_item_glint.png");
	
	@Override protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
	
	private static void renderIconIn3D(Icon i, float thickness) {
		ItemRenderer.renderItemIn2D(Tessellator.instance,
				i.getMaxU(), i.getMinV(), i.getMinU(), i.getMaxV(),
				i.getIconWidth(), i.getIconHeight(), thickness
		);
	}

	
	@Override public void doRender(Entity entity, double x, double y, double z, float p, float q) {
		EntityTomahawk et = (EntityTomahawk) entity;
		ItemStack item = et.item.get();
		if (item == null)
			return;
		
		glPushMatrix();
		{
			glTranslatef((float) x, (float) y, (float) z);
			
			float aRoll = et.aRoll.get();
			glRotatef(p - 90 - aRoll, 0, 1, 0);
			
			State state = et.state.get();
			if (state == State.ON_GROUND) {
				if (et.isInclined.get()) {
					float f = -8 * (Math.abs(aRoll) + 3);
					
					glRotatef(f, 1, 0, 0);
					glRotatef(f, 0, 0, 1);
				}
				
				glRotatef(90, 1, 0, 0);
			}
			
			glRotatef(-90, 0, 0, 1);

			//debug("isForwardSpin: " + et.isForwardSpin.get());
			//debug("isRolled: " + et.isRolled.get());
			
			if (et.isRolled.get())
				glRotatef(180, 0, 1, 0);
			
			glRotatef(-et.rotation.toDegrees(), 0, 0, 1);
			
			glRotatef(-et.bRoll.get(), 0, 1, 0);
			
			
			glTranslatef(-7/16f, -12/16f, ITEM_Z / 2);
			
			if (state == State.ON_GROUND)
				glTranslatef(0, 0, ITEM_Z / 2 * (!et.isRolled.get() ? -1 : 1));
			
			
			bindTexture(TextureMap.locationItemsTexture);
			
			
			int n = item.getItem().getRenderPasses(item.getItemDamage());
			for (int pass = 0; pass < n; pass++) {
				Icon icon = item.getItem().getIcon(item, pass);
				renderIconIn3D(icon != null ? icon : Item.axeWood.getIconFromDamage(0), ITEM_Z);
				
				if (item.hasEffect(pass)) {
					glDepthFunc(GL_EQUAL);
					glDisable(GL_LIGHTING);
					renderManager.renderEngine.bindTexture(RES_ITEM_GLINT);
					glEnable(GL_BLEND);
					glBlendFunc(GL_SRC_COLOR, GL_ONE);
					float f11 = 0.76F;
					glColor4f(0.5F * f11, 0.25F * f11, 0.8F * f11, 1);
					glMatrixMode(GL_TEXTURE);
					glPushMatrix();
					float f12 = 0.125F;
					glScalef(f12, f12, f12);
					float f13 = (float) (Minecraft.getSystemTime() % 3000) / 3000 * 8;
					glTranslatef(f13, 0, 0);
					glRotatef(-50, 0, 0, 1);
					renderItemIn2D(Tessellator.instance, 0, 0, 1, 1, 255, 255, ITEM_Z);
					glPopMatrix();
					glPushMatrix();
					glScalef(f12, f12, f12);
					f13 = (float) (Minecraft.getSystemTime() % 4873) / 4873 * 8;
					glTranslatef(-f13, 0, 0);
					glRotatef(10, 0, 0, 1);
					renderItemIn2D(Tessellator.instance, 0, 0, 1, 1, 255, 255, ITEM_Z);
					glPopMatrix();
					glMatrixMode(GL_MODELVIEW);
					glDisable(GL_BLEND);
					glEnable(GL_LIGHTING);
					glDepthFunc(GL_LEQUAL);
				}
			}
		}
		glPopMatrix();
		
		//renderSpecial(et, item, x, y, z);
		
	}

}
