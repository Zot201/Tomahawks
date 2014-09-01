package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import zotmc.tomahawk.data.I18nData.ConfigI18ns;

import com.google.common.base.Supplier;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiPropToggle extends GuiPropPress {
	
	private final Configurable<Boolean> value;
	
	public GuiPropToggle(Supplier<String> name, Configurable<Boolean> value) {
		super(name);
		this.value = value;
	}
	
	@Override protected void onPress() {
		value.set(!value.get());
		
		super.onPress();
	}
	
	@Override protected String getButtonDisplay() {
		return (value.get() ? ConfigI18ns.ON : ConfigI18ns.OFF).get();
	}
	
}
