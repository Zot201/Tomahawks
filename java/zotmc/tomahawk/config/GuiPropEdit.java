package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraft.client.resources.I18n.format;

import com.google.common.base.Supplier;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiPropEdit extends GuiProp {

	private final Supplier<GuiEdit> factory;
	
	public GuiPropEdit(String name, Supplier<GuiEdit> factory) {
		super(name);
		this.factory = factory;
		
	}
	
	@Override protected void onActivate() {
		super.onActivate();
		
		factory.get().open(Config.current(), propName);
		
	}

	@Override protected String getButtonDisplay() {
		return format("selectServer.edit");
	}

}
