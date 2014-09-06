package zotmc.tomahawk.data;

import zotmc.tomahawk.util.Utils;

import com.google.common.base.Supplier;

public class I18nData {
	
	public static class ConfigI18ns {
		private static final Supplier<String>
		NAME = Utils.localize("tomahawk.name"),
		CORE_NAME = Utils.localize("tomahawk.core.name");

		public static final Supplier<String>
		TITLE = Utils.localize("tomahawk.gui.config.title", NAME),
		CORE_TITLE = Utils.localize("tomahawk.gui.config.title", CORE_NAME),
		RESOLVER_TITLE = Utils.localize("tomahawk.gui.config.resolver.title", NAME),
		
		GENERAL = Utils.localize("tomahawk.gui.config.general"),
		COMMON_AXES_THROWING = Utils.localize("tomahawk.gui.config.commonAxesThrowing"),
		THROWABLE_ITEMS = Utils.localize("tomahawk.gui.config.throwableItems"),
		FREE_RETRIEVAL = Utils.localize("tomahawk.gui.config.freeRetrieval"),
		
		ENCHANTMENTS = Utils.localize("tomahawk.gui.config.enchantments"),
		GOLDEN_FUSION = Utils.localize("tomahawk.gui.config.goldenFusion"),
		IGNITE_FIRE_RESPECT = Utils.localize("tomahawk.gui.config.igniteFireRespect"),
		
		COMPATIBILITIES = Utils.localize("tomahawk.gui.config.compatibilities"),
		TIC_HATCHETS_THROWING = Utils.localize("tomahawk.gui.config.tiCHatchetsThrowing"),
		TIC_LUMBER_AXES_THROWING = Utils.localize("tomahawk.gui.config.tiCLumberAxesThrowing"),
		TIC_FRYING_PANS_THROWING = Utils.localize("tomahawk.gui.config.tiCFryingPansThrowing"),
		TIC_HAMMERS_THROWING = Utils.localize("tomahawk.gui.config.tiCHammersThrowing"),
		
		RESET_ALL = Utils.localize("tomahawk.gui.resetAll"),
		
		EDIT = Utils.localize("selectServer.edit"),
		ON = Utils.localize("tomahawk.gui.on"),
		OFF = Utils.localize("tomahawk.gui.off"),
		DISABLED = Utils.localize("tomahawk.gui.disabled"),
		
		DONE = Utils.localize("gui.done"),
		DONE_REMARKED = Utils.localize("tomahawk.gui.config.doneRemarked", DONE),
		CANCEL = Utils.localize("gui.cancel"),
		SAVE_EXIT = Utils.localize("tomahawk.gui.config.saveExit"),
		SAVE_PROCEED = Utils.localize("tomahawk.gui.config.saveProceed");
	}
	
	public static final String
	NAME_REPLICA = "tomahawk.replica";
	
	public static final Supplier<String>
	REPLICA = Utils.localize("enchantment." + NAME_REPLICA);
	
}
