package zotmc.tomahawk.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.Tessellator;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiButtonRunnable extends GuiButtonChainable<GuiButtonRunnable> implements IGuiListEntry {
	
	private final Runnable action;
	private Supplier<Boolean> enabledFactory = Suppliers.ofInstance(true);
	private Supplier<String> displayFactory = Suppliers.ofInstance("");
	
	public GuiButtonRunnable(Runnable action) {
		this.action = checkNotNull(action);
	}
	
	@Override protected GuiButtonRunnable getThis() {
		return this;
	}
	
	public GuiButtonRunnable setIsEnabled(Supplier<Boolean> isVisible) {
		this.enabledFactory = checkNotNull(isVisible);
		return this;
	}
	
	public GuiButtonRunnable setDisplay(Supplier<String> supplier) {
		displayFactory = checkNotNull(supplier);
		return this;
	}
	
	
	//GuiButton
	
	@Override public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		enabled = enabledFactory.get();
		if (super.mousePressed(mc, mouseX, mouseY)) {
			action.run();
			return true;
		}
		return false;
	}
	
	@Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		enabled = enabledFactory.get();
		displayString = displayFactory.get();
		super.drawButton(mc, mouseX, mouseY);
	}
	
	
	//IGuiListEntry

	@Override public void drawEntry(int index, int x, int y, int listWidth, int slotHeight,
			Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
		
		setLeftTop(listWidth / 2 - width - 5, y);
		drawButton(mc(), mouseX, mouseY);
	}
	
	@Override public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		if (mousePressed(mc(), x, y)) {
			func_146113_a(mc().getSoundHandler());
			return true;
		}
		return false;
	}
	
	@Override public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) { }

}
