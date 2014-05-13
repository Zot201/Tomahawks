package zotmc.tomahawk.util;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.RandomAccess;

import zotmc.tomahawk.util.StandardImpls.CollectionImpl;

import com.google.common.base.Objects;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.IEventListener;

public class ListenerArrayList extends AbstractList<IEventListener> implements RandomAccess {
	
	private static final Field BUS_ID = Refls.getDeclaredField(EventBus.class, "busID");
	
	private final Event event;
	private final int busID;
	
	public ListenerArrayList(Event event, EventBus bus) {
		this.event = event;
		this.busID = Refls.get(BUS_ID, bus);
	}
	
	protected IEventListener[] a() {
		return event.getListenerList().getListeners(busID);
	}
	
	public int size() {
		return a().length;
	}
	
	public Object[] toArray() {
		return a().clone();
	}
	
	public <T> T[] toArray(T[] a) {
		return CollectionImpl.toArray(this, a);
	}
	
	public IEventListener get(int index) {
		return a()[index];
	}
	public IEventListener set(int index, IEventListener element) {
		IEventListener oldValue = a()[index];
		a()[index] = element;
		return oldValue;
	}
	
	public int indexOf(Object o) {
		for (int i = 0; i < a().length; i++)
			if (Objects.equal(a()[i], o))
				return i;
		return -1;
	}
	
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}
	
}