package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiConfigs implements IModGuiFactory {

	@Override public Class<? extends GuiScreen> mainConfigGuiClass() {
		return GuiConfigScreen.class;
	}
	
	@Override public void initialize(Minecraft minecraftInstance) { }
	
	@Override public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}
	
	@Override public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
	
	
	
	public static final ResourceLocation ACHIEVEMENT_BACKGROUND =
			new ResourceLocation("textures/gui/achievement/achievement_background.png");
	
	public static Minecraft mc() {
		return Minecraft.getMinecraft();
	}
	
	private static final RenderItem renderItem = new RenderItem();
	static RenderItem renderItem() {
		return renderItem;
	}
	
	public static void drawCenteredString(
			String string, int x, int y, int colour, boolean withShadow) {
		mc().fontRenderer.drawString(
				string,
				x - mc().fontRenderer.getStringWidth(string) / 2, y,
				colour, withShadow);
	}

}
