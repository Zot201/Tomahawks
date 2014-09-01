package zotmc.tomahawk.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multiset;
import com.google.common.collect.ObjectArrays;

/**
 * Standard implementations for collections. 
 */
public class StandardImpls {
	
	public static class CollectionImpl {

		/**
		 * Dependencies: {@link Collection#add(E)}
		 */
		public static <E> boolean addAll(Collection<E> collection, Collection<? extends E> c) {
			return Iterators.addAll(collection, c.iterator());
		}

		/**
		 * Dependencies: {@link Iterable#iterator()}
		 */
		public static boolean retainAll(Collection<?> collection, Collection<?> c) {
			return Iterators.retainAll(collection.iterator(), c);
		}

		/**
		 * Dependencies: {@link Collection#contains(Object)}
		 */
		public static boolean containsAll(Collection<?> collection, Collection<?> c) {
			for (Object e : c)
				if (!collection.contains(e))
					return false;
			return true;
		}

		/**
		 * Dependencies: {@link Iterable#iterator()}
		 */
		public static Object[] toArray(Collection<?> collection) {
			return Iterators.toArray(collection.iterator(), Object.class);
		}

		/**
		 * Dependencies: {@link Iterable#iterator()}
		 */
		@SuppressWarnings("unchecked")
		public static <E, T> T[] toArray(Collection<E> collection, T[] a) {
			int size = collection.size();
			if (a.length < size)
				a = ObjectArrays.newArray(a, size);
			
			int i = 0;
			for (Object element : collection)
				a[i++] = (T) element;
			
			if (a.length > size)
				a[size] = null; //marking end of a collection
			
			return a;
		}

		/**
		 * Dependencies: {@link Iterable#iterator()}
		 */
		public static String toString(Collection<?> collection) {
			return Iterators.toString(collection.iterator());
		}
		
		/**
		 * Dependencies: {@link Iterable#iterator()}
		 */
		public static boolean remove(Collection<?> collection, Object o) {
	        Iterator<?> it = collection.iterator();
            while (it.hasNext())
                if (Objects.equal(it.next(), o)) {
                    it.remove();
                    return true;
                }
	        return false;
		}

		/**
		 * Dependencies: {@link Iterable#iterator()}
		 */
		public static boolean removeAll(Collection<?> collection, Collection<?> c) {
			return Iterators.removeAll(collection.iterator(), c);
		}
	}
	
	
	public static class SetImpl extends CollectionImpl {

		/**
		 * Dependencies: {@link Collection#size()}, {@link Iterable#iterator()}, {@link Collection#remove()}
		 */
		public static boolean removeAll(Set<?> set, Collection<?> c) {
			if (c instanceof Multiset)
				c = ((Multiset<?>) c).elementSet();

			boolean changed = false;
			
			if (c instanceof Set && c.size() > set.size()) {
				Iterator<?> ite = set.iterator();
				while (ite.hasNext())
					if (c.contains(ite.next())) {
						changed = true;
						ite.remove();
					}
				return changed;
			}
			
			for (Object obj : c)
				changed |= set.remove(obj);
			return changed;
		}

		/**
		 * Dependencies: {@link Iterable#iterator()}
		 */
		public static int hashCode(Set<?> set) {
			int h = 0;
			for (Object o : set)
				if (o != null)
					h += o.hashCode();
			return h;
		}

		/**
		 * Dependencies: {@link Collection#size()}, {@link Collection#containsAll(Collection)}
		 */
		public static boolean equals(Set<?> set, Object obj) {
			if (obj == set)
				return true;
			
			if (obj instanceof Set)
				try {
					Set<?> o = (Set<?>) obj;
					return o.size() == set.size() && set.containsAll(o);
				} catch (NullPointerException ignored) {
				} catch (ClassCastException ignored) { }
			
			return false;
		}
		
	}

}
