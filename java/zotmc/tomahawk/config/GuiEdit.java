package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraft.client.resources.I18n.format;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiEdit extends GuiScreen {
	
	private final GuiScreen parent;
	private final Config config;
	private Config current;
	private String title;
	
	public GuiEdit(GuiScreen parent, Config config) {
		this.parent = parent;
		this.config = config;
	}

	public void open(Config current, String title) {
		this.current = current.beginInspect(config, this);
		this.title = title;
	}
	
	protected void quit() {
		current.endInspect(parent);
	}
	
	@Override public void initGui() {
		super.initGui();
		
		new GuiButtonRunnable(this, "quit")
			.setDisplay(format("gui.done"))
			.setLeftTop(width / 2 - 100, height - 29)
			.addTo(buttonList());
		
	}
	
	public void drawEmbeded(int mouseX, int mouseY, float tickFrac) {
		drawDefaultBackground();
	}
	
	@Override public void drawScreen(int mouseX, int mouseY, float tickFrac) {
		drawEmbeded(mouseX, mouseY, tickFrac);
		
		GuiConfigs.drawCenteredString(
				title,
				width / 2, 16,
				0xFFFFFF, true);
		
		super.drawScreen(mouseX, mouseY, tickFrac);
		
	}
	
	@SuppressWarnings("unchecked") protected List<GuiButton> buttonList() {
		return buttonList;
	}

}
