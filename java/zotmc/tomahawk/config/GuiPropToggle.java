package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraft.client.resources.I18n.format;
import zotmc.tomahawk.Tomahawk;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiPropToggle extends GuiProp {
	
	private final Configurable<Boolean> value;
	
	public GuiPropToggle(String name, Configurable<Boolean> value) {
		super(name);
		this.value = value;
	}
	
	@Override protected void onActivate() {
		super.onActivate();
		
		value.set(!value.get());
	}
	
	@Override protected String getButtonDisplay() {
		return format("tomahawk.gui." + (value.get() ? "on" : "off"));
	}
	
}
