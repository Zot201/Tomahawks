package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiConfigsCore implements IModGuiFactory {

	@Override public Class<? extends GuiScreen> mainConfigGuiClass() {
		return GuiConfigScreenCore.class;
	}
	
	@Override public void initialize(Minecraft minecraftInstance) { }
	
	@Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
	
	@Override public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
	
}
