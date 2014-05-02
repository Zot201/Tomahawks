package zotmc.tomahawk.config;

import static zotmc.tomahawk.Reflections.findField;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiSlot;
import zotmc.tomahawk.FieldAccess;

public class GuiPropertyList extends GuiListExtended {
	
	private static final Field HEIGHT = findField(GuiSlot.class, "height", "field_148158_l");
	
	private final GuiConfig parent;

	public GuiPropertyList(GuiConfig parent, Minecraft mc) {
		super(mc, parent.width, parent.height, 33, parent.height - 32, 20);
		this.parent = parent;
		setShowSelectionBox(false);
		
	}
	
	@Override public IGuiListEntry getListEntry(int var1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override protected int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override protected int getScrollBarX() {
		return width - 45;
	}
	
	@Override public int getListWidth() {
		return parent.width;
	}

}
