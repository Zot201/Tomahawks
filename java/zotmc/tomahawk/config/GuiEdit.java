package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import zotmc.tomahawk.data.I18nData.ConfigI18ns;

import com.google.common.base.Supplier;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiEdit extends GuiScreen {
	
	private final GuiScreen parent;
	private Supplier<String> title;
	
	public GuiEdit(GuiScreen parent) {
		this.parent = parent;
	}
	
	public void open(Config current, Supplier<String> title) {
		mc().displayGuiScreen(this);
		this.title = title;
	}
	
	protected void quit() {
		mc().displayGuiScreen(parent);
	}
	
	@Override public void initGui() {
		new GuiButtonRunnable(new Runnable() { public void run() {
				quit();
			}})
			.setDisplay(ConfigI18ns.DONE)
			.setLeftTop(width / 2 - 100, height - 29)
			.addTo(buttonList());
	}
	
	public void drawEmbeded(int mouseX, int mouseY, float tickFrac) {
		drawDefaultBackground();
	}
	
	@Override public void drawScreen(int mouseX, int mouseY, float tickFrac) {
		drawEmbeded(mouseX, mouseY, tickFrac);
		
		GuiConfigs.drawCenteredString(
				title.get(),
				width / 2, 16,
				0xFFFFFF, true);
		
		super.drawScreen(mouseX, mouseY, tickFrac);
	}
	
	@SuppressWarnings("unchecked") protected List<GuiButton> buttonList() {
		return buttonList;
	}

}
