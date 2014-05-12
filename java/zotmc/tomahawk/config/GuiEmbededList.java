package zotmc.tomahawk.config;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;

import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiEmbededList extends GuiListExtended {
	
	private final GuiScreen parent;
	private final List<IGuiListEntry> entries = Lists.newArrayList();
	
	public GuiEmbededList(GuiScreen parent) {
		super(mc(), parent.width, parent.height, 33, parent.height - 32, 20);
		this.parent = parent;
		setShowSelectionBox(false);
		
	}
	
	public void addEntries(IGuiListEntry... entries) {
		this.entries.addAll(Arrays.asList(entries));
	}
	
	
	
	@Override public IGuiListEntry getListEntry(int index) {
		return entries.get(index);
	}

	@Override protected int getSize() {
		return entries.size();
	}
	
	@Override protected int getScrollBarX() {
		return width - 45;
	}
	
	@Override public int getListWidth() {
		return parent.width;
	}

}
