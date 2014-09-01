package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import zotmc.tomahawk.data.I18nData.ConfigI18ns;

import com.google.common.base.Supplier;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiPropEdit extends GuiPropPress {

	private final Supplier<GuiEdit> factory;
	
	public GuiPropEdit(Supplier<String> name, Supplier<GuiEdit> factory) {
		super(name);
		this.factory = factory;
	}
	
	@Override protected void onPress() {
		factory.get().open(Config.current(), propName);
		
		super.onPress();
	}
	
	@Override protected String getButtonDisplay() {
		return ConfigI18ns.EDIT.get();
	}

}
