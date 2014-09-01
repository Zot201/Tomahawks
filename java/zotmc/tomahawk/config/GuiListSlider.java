package zotmc.tomahawk.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static cpw.mods.fml.relauncher.Side.CLIENT;

import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiListSlider<E> extends GuiButtonChainable<GuiListSlider<E>> {
	
	private static final int W = 8;
	private final Configurable<E> value;
	private final List<E> universe;
	private Function<E, String> displayFunction = Utils.toStringFunction();
	private Runnable onPress = Utils.doNothing();
	private Runnable onSlide = Utils.doNothing();
	
	private int pointer;
	private boolean dragging;
	
	public GuiListSlider(Configurable<E> value, List<E> universe) {
		this.value = value;
		this.universe = universe;
		pointer = universe.indexOf(value.get());
		updateString();
	}
	
	@Override protected GuiListSlider<E> getThis() {
		return this;
	}
	
	public GuiListSlider<E> setDisplay(Function<E, String> displayFunction) {
		this.displayFunction = checkNotNull(displayFunction);
		updateString();
		return this;
	}
	public GuiListSlider<E> setOnPress(Runnable action) {
		onPress = checkNotNull(action);
		return this;
	}
	public GuiListSlider<E> setOnSlide(Runnable action) {
		onSlide = checkNotNull(action);
		return this;
	}
	
	
	private void updateString() {
		displayString = displayFunction.apply(value.get());
	}
	
	private int getPointer(int mouseX) {
		int w = width - W - 1;
		return (int) Math.rint(
				Utils.closed(0, w, mouseX - W/2 - xPosition) / (double) w * (universe.size() - 1));
	}
	
	private int getHandle() {
		return xPosition + (int) Math.rint(pointer / (double) (universe.size() - 1) * (width - W - 1));
	}
	
	@Override public int getHoverState(boolean isInArea) {
		return 0;
	}
	
	@Override public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			int handle = getHandle();
			if (mouseX >= handle && mouseX < handle + W)
				dragging = true;
			else {
				pointer += Integer.signum(getPointer(mouseX) - pointer);
				value.set(universe.get(pointer));
				updateString();
			}
			onPress.run();
			return true;
		}
		return false;
	}
	
	@Override public void mouseReleased(int mouseX, int mouseY) {
		dragging = false;
		onSlide.run();
	}
	
	@Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (!universe.get(pointer).equals(value.get())) {
			pointer = universe.indexOf(value.get());
			updateString();
		}
		super.drawButton(mc, mouseX, mouseY);
	}
	
	@Override protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (dragging) {
			pointer = getPointer(mouseX);
			value.set(universe.get(pointer));
			updateString();
		}
		
		GL11.glColor4f(1, 1, 1, 1);
		int handle = getHandle();
		drawTexturedModalRect(handle, yPosition, 0, 66, W/2, height);
		drawTexturedModalRect(handle + W/2, yPosition, 196, 66, W/2, height);
	}
	
}
