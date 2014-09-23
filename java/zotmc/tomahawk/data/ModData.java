package zotmc.tomahawk.data;

import zotmc.tomahawk.util.Utils.Modid;
import zotmc.tomahawk.util.Utils.Requirements;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModMetadata;

@Requirements({"1.7 = 1.7", "1.6 = [1.6, 1.7)"})
public class ModData {
	
	public static class AxeTomahawk {
		public static final String
		MODID = "axetomahawk",
		NAME = "Tomahawks",
		MC_STRING = Loader.MC_VERSION,
		VERSION = "1.4.9.3-" + MC_STRING,
		DOMAIN = "tomahawk",
		PACKAGE = "zotmc.tomahawk",
		GUI_FACTORY = PACKAGE + ".config.GuiConfigs",
		
		CORE_MODID = MODID + ".core",
		CORE_NAME = "Tomahawks Core",
		CORE_GUI_FACTORY = PACKAGE + ".config.GuiConfigsCore",
		
		API_MODID = MODID + ".api",
		
		DEPENDENCIES = "required-after:" + CORE_MODID,
		CORE_DEPENDENCIES = "required-after:" + Forge.MODID;
	}
	
	public static void init(ModMetadata metadata) {
		metadata.autogenerated = false;
		metadata.description = ""; // 162
		metadata.authorList = Lists.newArrayList("Zot");
		metadata.url = "https://github.com/Zot201/Tomahawk";
	}
	
	public static void initCore(ModMetadata metadata) {
		init(metadata);
	}
	
	
	@Requirements({"1.7.10 = 10.13.0.1207", "1.7.2 = 10.12.2.1121", "1.6.4 = 9.11.1.965", "1.6.2 = 9.10.1.804"})
	public static class Forge {
		@Modid public static final String MODID = "Forge";
	}
	
	@Requirements({"1.7.2 = 1.2b", "1.6.2 = 1.2a"})
	public static class AdditionalEnchantments {
		@Modid public static final String MODID = "AdditionalEnchantments";
		public static final String NAME_PATTERN = "^ak\\.AdditionalEnchantments\\..*$";
	}
	
	@Requirements({"1.7.2 = 1.3.0", "1.6.2 = 1.2.5"})
	public static class MoreEnchants {
		@Modid public static final String MODID = "MoreEnchants";
		public static final String NAME_PATTERN = "^com\\.demoxin\\.minecraft\\.moreenchants\\..*$";
	}
	
	@Requirements("1.7.2 = 1.9.4")
	public static class OnlySilver {
		@Modid public static final String MODID = "onlysilver";
		public static final String
		ONLY_SILVER_REGISTRY = "zotmc.onlysilver.api.OnlySilverRegistry",
		REGISTER_WEAPON_FUNCTION = "registerWeaponFunction",
		IN_USE_WEAPON = ONLY_SILVER_REGISTRY + "$InUseWeapon";
	}
	
	@Requirements("1.7.10 = 1.7.10-1.6.0.build611")
	public static class TConstruct {
		@Modid public static final String MODID = "TConstruct";
		public static final String
		HATCHET = "tconstruct.items.tools.Hatchet",
		LUMBER_AXE = "tconstruct.items.tools.LumberAxe",
		FRYING_PAN = "tconstruct.items.tools.FryingPan",
		HAMMER = "tconstruct.items.tools.Hammer",
		FRYPAN_HIT = "tinker:frypan_hit";
	}
	
}
