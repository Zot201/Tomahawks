package zotmc.tomahawk.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.Tessellator;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Supplier;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public abstract class GuiPropPress implements IGuiListEntry {
	
	protected final Supplier<String> propName;
	private final GuiButtonRunnable button;
	private Runnable onPress = Utils.doNothing();
	
	public GuiPropPress(Supplier<String> name) {
		propName = name;
		button = new GuiButtonRunnable(new Runnable() { public void run() {
				onPress();
			}})
			.setDisplay(new Supplier<String>() { public String get() {
				return getButtonDisplay();
			}})
			.setHeight(18);
	}
	
	public GuiPropPress setOnPress(Runnable action) {
		onPress = checkNotNull(action);
		return this;
	}
	
	protected void onPress() {
		button.func_146113_a(mc().getSoundHandler());
		onPress.run();
	}
	
	protected abstract String getButtonDisplay();
	
	
	@Override public void drawEntry(int index, int x, int y, int listWidth, int slotHeight,
			Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
		
		button.setLeftTop(listWidth * 3 / 5, y)
			.setWidth(listWidth * 2 / 5 - (45 + 18))
			.drawButton(mc(), mouseX, mouseY);
		
		mc().fontRenderer.drawString(
				propName.get(),
				45 + 18, y + slotHeight / 2 - mc().fontRenderer.FONT_HEIGHT / 2,
				0xFFFFFF);
	}
	
	@Override public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		return button.mousePressed(mc(), x, y);
	}
	
	@Override public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) { }

}
