package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.Tessellator;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiPropCat implements IGuiListEntry {
	
	private final Supplier<String> catName;
	
	public GuiPropCat(String name) {
		this(Suppliers.ofInstance(name));
	}
	public GuiPropCat(Supplier<String> name) {
		catName = name;
	}
	
	@Override public void drawEntry(
			int index, int x, int y, int listWidth, int slotHeight,
			Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
		
		GuiConfigs.drawCenteredString(
				catName.get(),
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
