package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.Tessellator;

@SideOnly(CLIENT)
public class GuiPropCat implements IGuiListEntry {
	
	private final String catName;
	
	public GuiPropCat(String name) {
		catName = name;
	}
	
	@Override public void drawEntry(
			int index, int x, int y, int listWidth, int slotHeight,
			Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
		
		GuiConfigs.drawCenteredString(
				catName,
				listWidth / 2, y + slotHeight - mc().fontRenderer.FONT_HEIGHT - 1,
				0xFFFFFF, false);
		
	}
	
	@Override public boolean mousePressed(
			int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		return false;
	}
	
	@Override public void mouseReleased(
			int index, int x, int y, int mouseEvent, int relativeX, int relativeY) { }

}
