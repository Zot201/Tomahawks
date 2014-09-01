package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import zotmc.tomahawk.data.I18nData;
import zotmc.tomahawk.data.I18nData.ConfigI18ns;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiConfigScreenResolver extends GuiScreen {
	
	private final GuiScreen parent;
	private final Config config;
	private GuiEmbededList list;
	
	private boolean isValid, continuable;
	
	public GuiConfigScreenResolver(GuiScreen parent) {
		this.parent = parent;
		config = Config.current();
	}
	
	@Override public void initGui() {
		new GuiButtonRunnable(new Runnable() { public void run() {
				if (continuable) {
					config.save();
					mc().displayGuiScreen(parent);
				}
				else {
					config.save();
					FMLCommonHandler.instance().handleExit(0);
				}
			}})
			.setIsEnabled(new Supplier<Boolean>() { public Boolean get() {
				return isValid;
			}})
			.setDisplay(new Supplier<String>() { public String get() {
				return (continuable ? ConfigI18ns.SAVE_PROCEED : ConfigI18ns.SAVE_EXIT).get();
			}})
			.setLeftTop(width / 2 - 155, height - 29)
			.setWidthHeight(150, 20)
			.addTo(buttonList());
		
		new GuiButtonRunnable(new Runnable() { public void run() {
				config.replica.set(new Config().replica.get());
				updateState();
			}})
			.setDisplay(ConfigI18ns.RESET_ALL)
			.setLeftTop(width / 2 - 155 + 160, height - 29)
			.setWidthHeight(150, 20)
			.addTo(buttonList());
		
		
		list = new GuiEmbededList(this);
		
		list.addEntries(new GuiPropCat(ConfigI18ns.ENCHANTMENTS));
		
		list.addEntries(
				new GuiPropSlide<>(
							I18nData.REPLICA,
							config.replica,
							Utils.asList(
									new Function<Integer, Integer>() {public Integer apply(Integer input) {
										return input - 1;
									}},
									Enchantment.enchantmentsList.length + 1
							),
							new Function<Integer, String>() { public String apply(Integer input) {
								if (input == -1)
									return ConfigI18ns.DISABLED.get();
								
								Enchantment ench = Enchantment.enchantmentsList[input];
								String s = "ID: " + input;
								return ench == null ? s : s + " / " + I18n.format(ench.getName());
							}}
					)
					.setOnSlide(new Runnable() { public void run() {
						updateState();
					}})
		);
	}
	
	private void updateState() {
		int id = config.replica.get();
		continuable = id == -1;
		isValid = continuable || Enchantment.enchantmentsList[id] == null;
	}
	
	
	@Override public void drawScreen(int mouseX, int mouseY, float tickFrac) {
		drawDefaultBackground();
		
		list.drawScreen(mouseX, mouseY, tickFrac);
		
		GuiConfigs.drawCenteredString(
				ConfigI18ns.RESOLVER_TITLE.get(),
				width / 2, 16,
				0xFFFFFF, true
		);
		
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
