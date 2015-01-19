package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import zotmc.tomahawk.data.I18nData.ConfigI18ns;
import zotmc.tomahawk.data.ModData.MekanismTools;
import zotmc.tomahawk.data.ModData.TConstruct;

import com.google.common.base.Supplier;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiConfigScreen extends GuiScreen {
	
	private final GuiScreen parent;
	protected final Config config;
	private GuiEmbededList list;
	protected boolean nonHotChanges;
	
	public GuiConfigScreen(GuiScreen parent) {
		this.parent = parent;
		this.config = Config.preserved().copy();
		updateNonHotChanges();
	}
	
	protected void initEntries(GuiEmbededList list) {
		list.addEntries(
				new GuiPropCat(ConfigI18ns.GENERAL),
				new GuiPropToggle(ConfigI18ns.COMMON_AXES_THROWING, config.commonAxesThrowing)
		);
		
		
		boolean tic = Loader.isModLoaded(TConstruct.MODID);
		boolean mek = Loader.isModLoaded(MekanismTools.MODID);
		
		if (tic || mek)
			list.addEntries(
					new GuiPropCat(ConfigI18ns.COMPATIBILITIES)
			);
		if (tic)
			list.addEntries(
					new GuiPropToggle(ConfigI18ns.TIC_HATCHETS_THROWING, config.tiCHatchetsThrowing),
					new GuiPropToggle(ConfigI18ns.TIC_LUMBER_AXES_THROWING, config.tiCLumerAxesThrowing),
					new GuiPropToggle(ConfigI18ns.TIC_FRYING_PANS_THROWING, config.tiCFryingPansThrowing)/*,
					new GuiPropToggle(ConfigI18ns.TIC_HAMMERS_THROWING, config.tiCHammersThrowing)*/
			);
		if (mek)
			list.addEntries(
					new GuiPropToggle(ConfigI18ns.MEK_AXES_THROWING, config.mekAxesThrowing),
					new GuiPropToggle(ConfigI18ns.MEK_PAXEL_THROWING, config.mekPaxelsThrowing)
			);
	}
	
	@Override public void initGui() {
		new GuiButtonRunnable(new Runnable() { public void run() {
				saveQuit();
			}})
			.setDisplay(new Supplier<String>() { public String get() {
				return (nonHotChanges ? ConfigI18ns.DONE_REMARKED : ConfigI18ns.DONE).get();
			}})
			.setLeftTop(width / 2 - 155, height - 29)
			.setWidthHeight(150, 20)
			.addTo(buttonList());
		
		new GuiButtonRunnable(new Runnable() { public void run() {
				quit();
			}})
			.setDisplay(ConfigI18ns.CANCEL)
			.setLeftTop(width / 2 - 155 + 160, height - 29)
			.setWidthHeight(150, 20)
			.addTo(buttonList());
		
		
		list = new GuiEmbededList(this);
		
		initEntries(list);
		
		list.addEntries(
				new GuiPropCat(""),
				new GuiButtonRunnable(new Runnable() { public void run() {
						resetAll();
					}})
					.setDisplay(ConfigI18ns.RESET_ALL)
					.setWidthHeight(150, 18)
		);
	}

	
	protected void updateNonHotChanges() {
		nonHotChanges = !config.nonCoreNonHotEqualTo(Config.local());
	}
	
	protected void saveQuit() {
		Config.preserved().apply(config).save();
		Config.local().applyHot(config);
		quit();
	}
	protected void quit() {
		mc().displayGuiScreen(parent);
	}
	
	protected void resetAll() {
		config.applyNonCore(new Config());
		updateNonHotChanges();
	}
	
	protected String getTitle() {
		return ConfigI18ns.TITLE.get();
	}
	
	
	@Override public void drawScreen(int mouseX, int mouseY, float tickFrac) {
		drawDefaultBackground();
		
		list.drawScreen(mouseX, mouseY, tickFrac);
		
		GuiConfigs.drawCenteredString(getTitle(), width / 2, 16, 0xFFFFFF, true);
		
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
