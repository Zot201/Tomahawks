package zotmc.tomahawk.projectile;

import static net.minecraft.block.material.Material.air;
import static net.minecraft.client.renderer.ItemRenderer.renderItemIn2D;
import static net.minecraft.init.Items.wooden_axe;
import static net.minecraft.util.MathHelper.floor_double;
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
import static zotmc.tomahawk.projectile.AbstractTomahawk.State.ON_GROUND;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import zotmc.tomahawk.projectile.AbstractTomahawk.State;

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
				i.getIconWidth(), i.getIconHeight(), thickness);
	}
	
	@Override public void doRender(Entity entity, double x, double y, double z, float p, float q) {
		EntityTomahawk et = (EntityTomahawk) entity;
		ItemStack item = et.getItem();
		if (item == null)
			return;
		
		glPushMatrix();
		{
			glTranslatef((float) x, (float) y, (float) z);
			
			
			glRotatef(p - 90 - et.aRoll, 0, 1, 0);
			
			State state = et.getState();
			if (state == ON_GROUND) {
				int etx = floor_double(et.posX);
				int ety = floor_double(et.posY);
				int etz = floor_double(et.posZ);
				int meta = et.worldObj.getBlockMetadata(etx, ety, etz);
				Block block = et.worldObj.getBlock(etx, ety, etz);
				
				if (block.getMaterial() != air && block.canCollideCheck(meta, true)) {
					glRotatef(-8 * (Math.abs(et.aRoll) + 3), 1, 0, 0);
					glRotatef(-8 * (Math.abs(et.aRoll) + 3), 0, 0, 1);
				}
				
				glRotatef(90, 1, 0, 0);
			}
			
			glRotatef(-90, 0, 0, 1);
			
			if (!et.getIsForwardSpin())
				glRotatef(180, 0, 1, 0);
			
			glRotatef(-et.getRotation(), 0, 0, 1);
			
			glRotatef(-et.bRoll, 0, 1, 0);
			
			
			glTranslatef(-7/16f, -12/16f, ITEM_Z / 2);
			
			if (state == ON_GROUND)
				glTranslatef(0, 0, ITEM_Z / 2 * (et.getIsForwardSpin() ? -1 : 1));
			
			
			bindTexture(TextureMap.locationItemsTexture);
			
			
			int n = item.getItem().getRenderPasses(item.getItemDamage());
			for (int pass = 0; pass < n; pass++) {
				IIcon icon = item.getItem().getIcon(item, pass);
				renderIconIn3D(icon != null ? icon : wooden_axe.getIconFromDamage(0), ITEM_Z);
				
				if (item.hasEffect(pass)) {
					glDepthFunc(GL11.GL_EQUAL);
					glDisable(GL11.GL_LIGHTING);
					renderManager.renderEngine.bindTexture(RES_ITEM_GLINT);
					glEnable(GL11.GL_BLEND);
					glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
					float f11 = 0.76F;
					glColor4f(0.5F * f11, 0.25F * f11, 0.8F * f11, 1.0F);
					glMatrixMode(GL11.GL_TEXTURE);
					glPushMatrix();
					float f12 = 0.125F;
					glScalef(f12, f12, f12);
					float f13 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
					glTranslatef(f13, 0.0F, 0.0F);
					glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
					renderItemIn2D(Tessellator.instance, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, ITEM_Z);
					glPopMatrix();
					glPushMatrix();
					glScalef(f12, f12, f12);
					f13 = (float)(Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
					glTranslatef(-f13, 0.0F, 0.0F);
					glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
					renderItemIn2D(Tessellator.instance, 0.0F, 0.0F, 1.0F, 1.0F, 255, 255, ITEM_Z);
					glPopMatrix();
					glMatrixMode(GL11.GL_MODELVIEW);
					glDisable(GL11.GL_BLEND);
					glEnable(GL11.GL_LIGHTING);
					glDepthFunc(GL11.GL_LEQUAL);
				}
			}
		}
		glPopMatrix();
		
	}


}
