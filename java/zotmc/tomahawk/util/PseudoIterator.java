package zotmc.tomahawk.util;

import java.util.Iterator;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class PseudoIterator<E> {
	
	private final Iterable<E> iterable;
	
	private Iterator<E> backing;
	private int currentIndex;
	private E lastElement;
	
	private PseudoIterator(Iterable<E> iterable) {
		this.iterable = iterable;
		backing = iterable.iterator();
	}
	
	public static <E> PseudoIterator<E> of(Iterable<E> iterable) {
		return new PseudoIterator<E>(iterable);
	}
	
	
	public E next(int index) {
		if (index == currentIndex - 1)
			return lastElement;
		
		if (index > currentIndex)
			Iterators.advance(backing, index - currentIndex);
		else if (index < currentIndex) {
			backing = iterable.iterator();
			Iterators.advance(backing, index);
		}
		
		currentIndex = index + 1;
		return lastElement = backing.next();
	}
	
	public int size() {
		return Iterables.size(iterable);
	}

}
