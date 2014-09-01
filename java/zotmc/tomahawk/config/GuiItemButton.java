package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraft.item.EnumRarity.common;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static zotmc.tomahawk.config.GuiConfigs.mc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.util.Holder;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiItemButton extends GuiButtonRunnable {
	
	private Supplier<Boolean> displayState;
	private ItemStack displayItem;
	
	private Holder<ItemStack> toolTipItem;
	
	public GuiItemButton(Runnable action) {
		super(action);
		setWidthHeight(22, 22);
	}
	
	public GuiItemButton setDisplayItem(Item displayItem) {
		return setDisplayItem(new ItemStack(displayItem));
	}
	public GuiItemButton setDisplayItem(ItemStack displayItem) {
		this.displayItem = displayItem;
		return this;
	}
	public ItemStack getDisplayItem() {
		return displayItem;
	}
	
	public GuiItemButton setDisplayState(Supplier<Boolean> displayState) {
		this.displayState = displayState;
		return this;
	}
	public boolean getDisplayState() {
		return displayState == null || displayState.get();
	}
	
	public GuiItemButton setToolTipItem(Holder<ItemStack> toolTipItem) {
		this.toolTipItem = toolTipItem;
		return this;
	}
	
	
	
	@Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (visible) {
			int x = xPosition, y = yPosition;
			RenderItem renderItem = GuiConfigs.renderItem();

			glPushMatrix();
			{
				boolean hoveringOver = xRange().contains(mouseX) && yRange().contains(mouseY);
				
				float brightness = hoveringOver ? 1 : 0.75F;
				if (!getDisplayState())
					brightness /= 2.5F;
				glColor4f(brightness, brightness, brightness, 1);
				
				glEnable(GL_BLEND);
				glDepthMask(false);
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				mc().getTextureManager().bindTexture(GuiConfigs.ACHIEVEMENT_BACKGROUND);
				renderItem.renderWithColor = false;
				
				if (displayItem == null || displayItem.getRarity() == common)
					drawTexturedModalRect(x - 2, y - 2, 0, 202, 26, 26);
				else
					drawTexturedModalRect(x - 2, y - 2, 26, 202, 26, 26);
				
				
				if (displayItem != null) {
					RenderHelper.enableStandardItemLighting();
					glEnable(GL_LIGHTING);
					glEnable(GL_CULL_FACE);
					renderItem.renderWithColor = getDisplayState();
					renderItem.renderItemAndEffectIntoGUI(
							mc().fontRenderer, mc().getTextureManager(),
							displayItem, x + 3, y + 3);
					
					RenderHelper.disableStandardItemLighting();
					
					if (hoveringOver && toolTipItem != null)
						toolTipItem.set(displayItem);
				}
				
			}
			glPopMatrix();
			
		}
	}
	
	protected Range<Integer> xRange() {
		return Range.closedOpen(xPosition, xPosition + width);
	}	
	protected Range<Integer> yRange() {
		return Range.closedOpen(yPosition, yPosition + height);
	}
	
}
