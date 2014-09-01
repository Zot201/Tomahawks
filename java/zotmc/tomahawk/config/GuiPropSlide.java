package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.List;

import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.Tessellator;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiPropSlide<E> implements IGuiListEntry {
	
	private final Supplier<String> propName;
	private final GuiListSlider<E> slider;
	
	public GuiPropSlide(Supplier<String> name, Configurable<E> value,
			List<E> universe, Function<E, String> displayFunction) {
		propName = name;
		slider = new GuiListSlider<E>(value, universe)
				.setDisplay(displayFunction)
				.setOnPress(new Runnable() { public void run() {
					onPress();
				}})
				.setHeight(18);
	}
	
	public GuiPropSlide<E> setOnSlide(Runnable action) {
		slider.setOnSlide(action);
		return this;
	}
	
	protected void onPress() {
		slider.func_146113_a(mc().getSoundHandler());
	}
	
	
	@Override public void drawEntry(int index, int x, int y, int listWidth, int slotHeight,
			Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
		
		slider.setLeftTop(listWidth * 3 / 7, y)
			.setWidth(listWidth * 4 / 7 - (45 + 18))
			.drawButton(mc(), mouseX, mouseY);
		
		mc().fontRenderer.drawString(
				propName.get(),
				45 + 18, y + slotHeight / 2 - mc().fontRenderer.FONT_HEIGHT / 2,
				0xFFFFFF);
	}
	
	@Override public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		return slider.mousePressed(mc(), x, y);
	}
	
	@Override public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		slider.mouseReleased(x, y);
	}

}
