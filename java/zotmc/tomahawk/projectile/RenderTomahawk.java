package zotmc.tomahawk.projectile;

import static net.minecraft.client.renderer.ItemRenderer.renderItemIn2D;
import static net.minecraft.init.Items.wooden_axe;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_EQUAL;
import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import zotmc.tomahawk.core.LogTomahawk;
import zotmc.tomahawk.projectile.EntityTomahawk.State;

public class RenderTomahawk extends Render {
	
	private static final float ITEM_Z = 0.0625f;
	private static final ResourceLocation RES_ITEM_GLINT =
			new ResourceLocation("textures/misc/enchanted_item_glint.png");
	
	@Override protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
	
	private static void renderIconIn3D(IIcon i, float thickness) {
		ItemRenderer.renderItemIn2D(Tessellator.instance,
				i.getMaxU(), i.getMinV(), i.getMinU(), i.getMaxV(),
				i.getIconWidth(), i.getIconHeight(), thickness
		);
	}

	
	private static final void debug(String s, Object... args) {
		LogTomahawk.ren4j().debug(s, args);
	}
	
	@Override public void doRender(Entity entity, double x, double y, double z, float p, float q) {
		EntityTomahawk et = (EntityTomahawk) entity;
		ItemStack item = et.item.get();
		if (item == null)
			return;
		
		glPushMatrix();
		{
			glTranslatef((float) x, (float) y, (float) z);
			
			
			glRotatef(p - 90 - et.aRoll, 0, 1, 0);
			
			State state = et.state.get();
			if (state == State.ON_GROUND) {
				if (et.pos.canBlockCollideCheck(et.worldObj, true)) {
					glRotatef(-8 * (Math.abs(et.aRoll) + 3), 1, 0, 0);
					glRotatef(-8 * (Math.abs(et.aRoll) + 3), 0, 0, 1);
				}
				
				glRotatef(90, 1, 0, 0);
			}
			
			glRotatef(-90, 0, 0, 1);

			//debug("isForwardSpin: " + et.isForwardSpin.get());
			//debug("isRolled: " + et.isRolled.get());
			
			if (et.isRolled.get())
				glRotatef(180, 0, 1, 0);
			
			glRotatef(-et.rotation.toDegrees(), 0, 0, 1);
			
			glRotatef(-et.bRoll, 0, 1, 0);
			
			
			glTranslatef(-7/16f, -12/16f, ITEM_Z / 2);
			
			if (state == State.ON_GROUND)
				glTranslatef(0, 0, ITEM_Z / 2 * (!et.isRolled.get() ? -1 : 1));
			
			
			bindTexture(TextureMap.locationItemsTexture);
			
			
			int n = item.getItem().getRenderPasses(item.getItemDamage());
			for (int pass = 0; pass < n; pass++) {
				IIcon icon = item.getItem().getIcon(item, pass);
				renderIconIn3D(icon != null ? icon : wooden_axe.getIconFromDamage(0), ITEM_Z);
				
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
		
		renderSpecial(et, item, x, y, z);
		
	}
	
	protected boolean toRenderDisplayName(EntityTomahawk et, ItemStack item) {
		return et == renderManager.field_147941_i && Minecraft.isGuiEnabled() && et.state.get().isStationary()
				&& item.hasDisplayName() && !et.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);
	}
	
	protected void renderSpecial(EntityTomahawk et, ItemStack item, double x, double y, double z) {
		glAlphaFunc(GL_GREATER, 0.1F);
		
		if (toRenderDisplayName(et, item)) {
			float f = 1.6F;
			float f1 = 0.016666668F * f;
			double d3 = et.getDistanceSqToEntity(renderManager.livingPlayer);
			float f2 = RendererLivingEntity.NAME_TAG_RANGE;
			
			if (d3 < f2 * f2) {
				String s = item.getDisplayName();
				
				FontRenderer fontrenderer = getFontRendererFromRenderManager();
				glPushMatrix();
				glTranslatef((float) x, (float) y + et.height + 0.5F, (float) z);
				glNormal3f(0, 1, 0);
				glRotatef(-renderManager.playerViewY, 0, 1, 0);
				glRotatef(renderManager.playerViewX, 1, 0, 0);
				glScalef(-f1, -f1, f1);
				glDisable(GL_LIGHTING);
				glTranslatef(0, 0.25F / f1, 0);
				glDepthMask(false);
				glEnable(GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				Tessellator tessellator = Tessellator.instance;
				glDisable(GL_TEXTURE_2D);
				tessellator.startDrawingQuads();
				int i = fontrenderer.getStringWidth(s) / 2;
				tessellator.setColorRGBA_F(0, 0, 0, 0.25F);
				tessellator.addVertex(-i - 1, -1, 0);
				tessellator.addVertex(-i - 1, 8, 0);
				tessellator.addVertex(i + 1, 8, 0);
				tessellator.addVertex(i + 1, -1, 0);
				tessellator.draw();
				glEnable(GL_TEXTURE_2D);
				glDepthMask(true);
				fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, 0xFFFFFF);
				glEnable(GL_LIGHTING);
				glDisable(GL_BLEND);
				glColor4f(1, 1, 1, 1);
				glPopMatrix();
			}
		}
	}

}
