package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.Tessellator;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public abstract class GuiProp implements IGuiListEntry {
	
	protected final String propName;
	protected final GuiButtonRunnable mainButton;
	
	public GuiProp(String name) {
		propName = name;
		
		mainButton = new GuiButtonRunnable(this, "onActivate")
			.setDisplay(this, "getButtonDisplay")
			.setHeight(18);
		
	}
	
	protected void onActivate() {
		mainButton.func_146113_a(mc().getSoundHandler());
	}
	
	protected abstract String getButtonDisplay();
	
	@Override public void drawEntry(
			int index, int x, int y, int listWidth, int slotHeight,
			Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
		
		mainButton
			.setLeftTop(listWidth * 2 / 3, y)
			.setWidth(listWidth / 3 - (45 + 18))
			.drawButton(mc(), mouseX, mouseY);
		
		mc().fontRenderer.drawString(
				propName,
				45 + 18, y + slotHeight / 2 - mc().fontRenderer.FONT_HEIGHT / 2,
				0xFFFFFF);
		
	}
	
	@Override public boolean mousePressed(
			int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		
		return mainButton.mousePressed(mc(), x, y);
	}
	
	@Override public void mouseReleased(
			int index, int x, int y, int mouseEvent, int relativeX, int relativeY) { }

}
