package zotmc.tomahawk.config;

import java.util.List;

import net.minecraft.client.gui.GuiButton;

public abstract class GuiButtonChainable<T extends GuiButtonChainable<T>> extends GuiButton {

	GuiButtonChainable() {
		super(0, 0, 0, "");
	}
	
	protected abstract T getThis();
	
	public T setLeftTop(int left, int top) {
		xPosition = left;
		yPosition = top;
		return getThis();
	}
	
	public T setWidthHeight(int width, int height) {
		this.width = width;
		this.height = height;
		return getThis();
	}
	public T setWidth(int width) {
		this.width = width;
		return getThis();
	}
	public T setHeight(int height) {
		this.height = height;
		return getThis();
	}
	
	public void addTo(List<? super T> list) {
		id = list.size();
		list.add(getThis());
	}
	
}
