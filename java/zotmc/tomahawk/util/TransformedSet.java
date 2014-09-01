package zotmc.tomahawk.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import zotmc.tomahawk.util.StandardImpls.SetImpl;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

public abstract class TransformedSet<F, E> implements Set<E> {
	
	protected abstract Set<F> backing();
	
	protected abstract Function<F, E> transformation();
	
	
	
	@SuppressWarnings("unchecked") protected E cast(Object element) {
		return (E) element;
	}
	
	
	@Override public Iterator<E> iterator() {
		return Iterators.transform(backing().iterator(), transformation());
	}
	
	
	@Override public boolean addAll(Collection<? extends E> c) {
		return SetImpl.addAll(this, c);
	}
	

	@Override public void clear() {
		backing().clear();
	}
	@Override public boolean removeAll(Collection<?> c) {
		return SetImpl.removeAll(this, c);
	}
	@Override public boolean retainAll(Collection<?> c) {
		return SetImpl.retainAll(this, c);
	}
	
	
	@Override public boolean isEmpty() {
		return backing().isEmpty();
	}
	@Override public int size() {
		return backing().size();
	}
	@Override public boolean containsAll(Collection<?> c) {
		return SetImpl.containsAll(this, c);
	}
	
	
	@Override public Object[] toArray() {
		return SetImpl.toArray(this);
	}
	@Override public <T> T[] toArray(T[] a) {
		return SetImpl.toArray(this, a);
	}
	
	
	@Override public int hashCode() {
		return SetImpl.hashCode(this);
	}
	@Override public boolean equals(Object o) {
		return SetImpl.equals(this, o);
	}
	@Override public String toString() {
		return SetImpl.toString(this);
	}
	
}
