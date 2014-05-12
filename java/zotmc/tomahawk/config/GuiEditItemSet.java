package zotmc.tomahawk.config;

import static com.google.common.collect.Iterators.singletonIterator;
import static cpw.mods.fml.relauncher.Side.CLIENT;
import static zotmc.tomahawk.config.GuiConfigs.mc;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.util.Holder;
import zotmc.tomahawk.util.PseudoIterator;
import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class GuiEditItemSet extends GuiEdit {
	
	private static final int SLOT_SIZE = 26;
	
	private final Configurable<Set<Item>> value;
	private final Iterable<Item> universe;
	private final boolean negate;
	
	private final Set<Item> mutable;
	private final Holder<ItemStack> toolTipItem = Holder.absent();
	
	private List<Category> categories;
	private EmbededList list;
	
	public GuiEditItemSet(GuiScreen parent, Config config,
			Configurable<Set<Item>> value, Iterable<Item> universe, boolean negate) {
		super(parent, config);
		
		this.value = value;
		this.universe = universe;
		this.negate = negate;
		
		mutable = Sets.newIdentityHashSet();
		mutable.addAll(value.get());
		
	}
	
	@Override protected void quit() {
		value.set(ImmutableSet.copyOf(mutable));
		
		super.quit();
	}
	
	@Override public void initGui() {
		super.initGui();
		
		Multimap<String, Item> classified = LinkedHashMultimap.create();
		for (Item i : universe)
			classified.put(Utils.getModid(i), i);
		
		categories = Lists.newLinkedList();
		for (Entry<String, Collection<Item>> entry : classified.asMap().entrySet())
			categories.add(new Category(
					entry.getKey(),
					ImmutableList.copyOf(entry.getValue())));
		
		list = new EmbededList();
		
	}
	
	@Override public void drawEmbeded(int mouseX, int mouseY, float tickFrac) {
		super.drawEmbeded(mouseX, mouseY, tickFrac);
		
		list.drawScreen(mouseX, mouseY, tickFrac);
	}
	
	@Override public void drawScreen(int mouseX, int mouseY, float tickFrac) {
		toolTipItem.clear();
		
		super.drawScreen(mouseX, mouseY, tickFrac);
		
		if (toolTipItem.isPresent())
			renderToolTip(toolTipItem.get(), mouseX, mouseY);
		
	}
	
	@Override protected void mouseClicked(int x, int y, int mouseEvent) {
		if (!list.func_148179_a(x, y, mouseEvent))
			super.mouseClicked(x, y, mouseEvent);
	}
	
	@Override protected void mouseMovedOrUp(int x, int y, int mouseEvent) {
		if (!list.func_148181_b(x, y, mouseEvent))
			super.mouseMovedOrUp(x, y, mouseEvent);
	}
	
	public int itemsPerRow() {
		return Math.max(1, (width - (45 + 18) * 2) / SLOT_SIZE);
	}
	
	
	
	private class EmbededList extends GuiListExtended {
		
		private final PseudoIterator<IGuiListEntry> iterator =
				PseudoIterator.of(Iterables.concat(categories));
		
		public EmbededList() {
			super(mc(), GuiEditItemSet.this.width, GuiEditItemSet.this.height,
					33, GuiEditItemSet.this.height - 32, SLOT_SIZE);
			setShowSelectionBox(false);
		}
		
		@Override public IGuiListEntry getListEntry(int index) {
			return iterator.next(index);
		}
		
		@Override protected int getSize() {
			return iterator.size();
		}
		
		@Override protected int getScrollBarX() {
			return width - 45;
		}
		
		@Override public int getListWidth() {
			return GuiEditItemSet.this.width;
		}
		
	}
	
	
	private class Category implements Iterable<IGuiListEntry> {
		
		private final IGuiListEntry titleEntry;
		private final List<ItemEntry> itemEntries;
		
		public Category(String modid, List<Item> items) {
			titleEntry = new GuiPropCat(
					modid.equals("minecraft") ? "Minecraft" :
						FMLCommonHandler.instance().findContainerFor(modid).getName());
			
			Function<List<Item>, ItemEntry> getItemEntry =
					new Function<List<Item>, ItemEntry>() {
				@Override public ItemEntry apply(List<Item> input) {
					return new ItemEntry(input);
				}
			};
			itemEntries = FluentIterable
					.from(Lists.partition(items, itemsPerRow()))
					.transform(getItemEntry)
					.toList();
					
		}

		@Override public Iterator<IGuiListEntry> iterator() {
			return Iterators.concat(
					singletonIterator(titleEntry),
					itemEntries.iterator());
		}
		
	}
	
	
	private class ItemEntry implements IGuiListEntry {

		private final List<GuiItemButton> itemButtons;
		
		public ItemEntry(List<Item> items) {
			itemButtons = FluentIterable
					.from(items)
					.transform(new Function<Item, GuiItemButton>() {
						@Override public GuiItemButton apply(Item input) {
							return new GuiItemButton(toggleState(input))
								.setDisplayItem(input)
								.setDisplayState(getState(input))
								.setToolTipItem(toolTipItem);
						}
					})
					.toList();
		}
		
		public Runnable toggleState(final Item item) {
			return new Runnable() {
				@Override public void run() {
					if (!mutable.add(item))
						mutable.remove(item);
				}
			};
		}
		
		public Supplier<Boolean> getState(final Item item) {
			return new Supplier<Boolean>() {
				@Override public Boolean get() {
					return negate ^ mutable.contains(item);
				}
			};
		}
		
		@Override public void drawEntry(
				int index, int x, int y, int listWidth, int slotHeight,
				Tessellator tessellator, int mouseX, int mouseY, boolean isSelected) {
			
			int n = itemButtons.size();
			x = listWidth / 2 - n * SLOT_SIZE / 2;
			
			for (int i = 0; i < n; i++) {
				drawSlot(i, x, y, mouseX, mouseY);
				x += SLOT_SIZE;
			}
		}
		
		public void drawSlot(int index, int x, int y, int mouseX, int mouseY) {
			itemButtons.get(index)
				.setLeftTop(x + (SLOT_SIZE - 22) / 2, y + (SLOT_SIZE - 22) / 2)
				.drawButton(mc(), mouseX, mouseY);
		}
		
		@Override public boolean mousePressed(
				int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
			for (GuiItemButton button : itemButtons)
				if (button.mousePressed(index, x, y, mouseEvent, relativeX, relativeY))
					return true;
			return false;
		}
		
		@Override public void mouseReleased(
				int index, int x, int y, int mouseEvent, int relativeX, int relativeY) { }
		
	}
	
}
