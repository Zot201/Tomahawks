package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.Tessellator;
import zotmc.tomahawk.util.Refls;

import com.google.common.base.Supplier;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiButtonRunnable extends GuiButton implements IGuiListEntry {
	
	private final Runnable action;
	private Supplier<String> display;

	public GuiButtonRunnable(Object obj, String methodName) {
		this(Refls.asRunnable(obj, methodName));
	}
	public GuiButtonRunnable(Runnable action) {
		super(0, 0, 0, "");
		this.action = action;
	}
	
	
	public GuiButtonRunnable setDisplay(String string) {
		display = null;
		displayString = string;
		return this;
	}
	public GuiButtonRunnable setDisplay(Object obj, String methodName) {
		return setDisplay(Refls.<String>asSupplier(obj, methodName));
	}
	public GuiButtonRunnable setDisplay(Supplier<String> supplier) {
		display = supplier;
		return this;
	}
	

	public GuiButtonRunnable setLeftTop(int left, int top) {
		xPosition = left;
		yPosition = top;
		return this;
	}
	public GuiButtonRunnable setWidthHeight(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}
	public GuiButtonRunnable setWidth(int width) {
		this.width = width;
		return this;
	}
	public GuiButtonRunnable setHeight(int height) {
		this.height = height;
		return this;
	}
	public void addTo(List<GuiButton> list) {
		id = list.size();
		list.add(this);
	}
	
	
	
	//GuiButton
	
	@Override public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			action.run();
			return true;
		}
		return false;
	}
	
	@Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (display != null)
			displayString = display.get();
		super.drawButton(mc, mouseX, mouseY);
	}
	
	
	
	//IGuiListEntry

	@Override public void drawEntry(
			int index, int x, int y, int listWidth, int slotHeight,
			Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
		
		setLeftTop(listWidth / 2 - width - 5, y);
		drawButton(mc(), mouseX, mouseY);
		
	}
	
	@Override public boolean mousePressed(
			int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
		
		if (mousePressed(mc(), x, y)) {
			func_146113_a(mc().getSoundHandler());
			return true;
		}
		return false;
	}
	
	@Override public void mouseReleased(
			int index, int x, int y, int mouseEvent, int relativeX, int relativeY) { }

}
