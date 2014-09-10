package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.data.I18nData;
import zotmc.tomahawk.data.I18nData.ConfigI18ns;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiConfigScreenCore extends GuiConfigScreen {
	
	public GuiConfigScreenCore(GuiScreen parent) {
		super(parent);
	}
	
	@Override protected void initEntries(GuiEmbededList list) {
		list.addEntries(
				new GuiPropCat(ConfigI18ns.GENERAL),
				new GuiPropEdit(
						ConfigI18ns.THROWABLE_ITEMS,
						new Supplier<GuiEdit>() { public GuiEdit get() {
							return new GuiEditItemSet(
									GuiConfigScreenCore.this,
									config.itemBlacklist.asItems(),
									Iterables.filter(
											Utils.itemList(),
											new Predicate<Item>() { public boolean apply(Item input) {
												return TomahawkRegistry.getItemHandler(input).isEnabled();
											}}
									),
									true
							);
						}}
				),
				new GuiPropToggle(ConfigI18ns.FREE_RETRIEVAL, config.freeRetrieval),
				new GuiPropToggle(ConfigI18ns.ENTITY_RESTITUTION, config.reduceEntityRestitution) {
					@Override protected String getButtonDisplay() {
						return (value.get() ? ConfigI18ns.REDUCED : ConfigI18ns.CLASSICAL).get();
					}
				},
				
				new GuiPropCat(ConfigI18ns.ENCHANTMENTS),
				new GuiPropToggle(ConfigI18ns.IGNITE_FIRE_RESPECT, config.igniteFireRespect),
				new GuiPropToggle(ConfigI18ns.GOLDEN_FUSION, config.goldenFusion),
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
						updateNonHotChanges();
					}})
		);
	}
	
	
	@Override protected void updateNonHotChanges() {
		nonHotChanges = !config.coreNonHotEqualTo(Config.local());
	}
	
	@Override protected void resetAll() {
		config.applyCore(new Config());
		updateNonHotChanges();
	}
	
	@Override protected String getTitle() {
		return ConfigI18ns.CORE_TITLE.get();
	}
	
}
