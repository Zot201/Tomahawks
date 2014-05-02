package zotmc.tomahawk.config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiConfig extends GuiScreen {
	
	private final GuiScreen parent;
	private GuiPropertyList properties;
	
	public GuiConfig(GuiScreen parent) {
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	@Override public void initGui() {
		buttonList.add(new GuiButton(1, width / 2 - 100, height - 38, I18n.format("gui.done")));
		
		properties = new GuiPropertyList(this, mc);
	}
	
	@Override protected void actionPerformed(GuiButton button) {
		if (button.enabled && button.id == 1)
			mc.displayGuiScreen(parent);
	}
	
	@Override public void drawScreen(int mouseX, int mouseY, float tickFrac) {
		drawDefaultBackground();
		properties.drawScreen(mouseX, mouseY, tickFrac);
		drawCenteredString(fontRendererObj,
				"Tomahawk Configuration", width / 2, 16, 0xFFFFFF);
		super.drawScreen(mouseX, mouseY, tickFrac);
	}

}
