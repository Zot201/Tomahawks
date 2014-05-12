package zotmc.tomahawk.config;

import static cpw.mods.fml.common.Loader.isModLoaded;
import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraft.client.resources.I18n.format;
import static zotmc.tomahawk.TomahawkRegistry.isThrowableAxeRaw;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import zotmc.tomahawk.Tomahawk;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiConfigScreen extends GuiScreen {
	
	private final String title = format("tomahawk.gui.config.title", Tomahawk.NAME);
	private final GuiScreen parent;
	private final Config config;
	private GuiEmbededList list;
	
	public GuiConfigScreen(GuiScreen parent) {
		this.parent = parent;
		this.config = Config.preserved().copy();
	}
	
	
	@Override public void initGui() {
		super.initGui();
		
		new GuiButtonRunnable(this, "saveQuit")
			.setDisplay(format("gui.done"))
			.setLeftTop(width / 2 - 155, height - 29)
			.setWidthHeight(150, 20)
			.addTo(buttonList());
		
		new GuiButtonRunnable(this, "quit")
			.setDisplay(format("gui.cancel"))
			.setLeftTop(width / 2 - 155 + 160, height - 29)
			.setWidthHeight(150, 20)
			.addTo(buttonList());
		
		
		list = new GuiEmbededList(this);
		
		list.addEntries(
				new GuiPropCat(
						format("tomahawk.gui.config.general")),
				new GuiPropToggle(
						format("tomahawk.gui.config.commonAxesThrowing"),
						config.commonAxesThrowing),
				new GuiPropEdit(
						format("tomahawk.gui.config.throwableItems"),
						new Supplier<GuiEdit>() {
							@Override public GuiEdit get() {
								return new GuiEditItemSet(
										GuiConfigScreen.this,
										config,
										config.axeBlacklist,
										FluentIterable
											.from(Utils.itemList())
											.filter(isThrowableAxeRaw()),
										true);
							}
						})
		);
		
		if (isModLoaded("TConstruct"))
			list.addEntries(
					new GuiPropCat(
							format("tomahawk.gui.config.compatibilities")),
					new GuiPropToggle(
							format("tomahawk.gui.config.tiCHatchetsThrowing"),
							config.tiCHatchetsThrowing),
					new GuiPropToggle(
							format("tomahawk.gui.config.tiCFryingPansThrowing"),
							config.tiCFryingPansThrowing)
			);
		
		list.addEntries(
				new GuiPropCat(""),
				new GuiButtonRunnable(this, "resetAll")
					.setDisplay(format("tomahawk.gui.resetAll"))
					.setWidthHeight(150, 18)
		);
		
	}
	
	
	public void saveQuit() {
		Config.preserved().apply(config).save();
		Config.current().applyHot(config);
		quit();
	}
	public void quit() {
		mc().displayGuiScreen(parent);
	}
	
	public void resetAll() {
		config.apply(new Config());
	}
	
	
	@Override public void drawScreen(int mouseX, int mouseY, float tickFrac) {
		drawDefaultBackground();
		
		list.drawScreen(mouseX, mouseY, tickFrac);
		
		GuiConfigs.drawCenteredString(
				title,
				width / 2, 16,
				0xFFFFFF, true);
		
		super.drawScreen(mouseX, mouseY, tickFrac);
		
	}
	
	@Override protected void mouseClicked(int x, int y, int mouseEvent) {
		if (!list.func_148179_a(x, y, mouseEvent))
			super.mouseClicked(x, y, mouseEvent);
	}
	
	@Override protected void mouseMovedOrUp(int x, int y, int mouseEvent) {
		if (!list.func_148181_b(x, y, mouseEvent))
			super.mouseMovedOrUp(x, y, mouseEvent);
	}
	
	
	@SuppressWarnings("unchecked") protected List<GuiButton> buttonList() {
		return buttonList;
	}

}
