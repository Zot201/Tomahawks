package zotmc.tomahawk.util;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

/**
 * A map with default values according to the function provided.</br>
 */
public abstract class FallbackingMap<K, V> extends ForwardingMap<K, V> implements Map<K, V>, Function<K, V> {
	
	public static <K, V> FallbackingMap<K, V> create(final Function<K, V> function) {
		final Map<K, V> delegatee = Maps.newHashMap();
		
		return new FallbackingMap<K, V>() {
			@Override protected Map<K, V> delegate() {
				return delegatee;
			}
			@Override public Function<K, V> function() {
				return function;
			}
		};
	}
	

	@SuppressWarnings("unchecked") protected K castKey(Object key) {
		return (K) key;
	}
	
	
	protected abstract Function<K, V> function();
	
	@Override public V put(K key, V value) {
		V ret = super.put(key, value);
		return ret != null ? ret : function().apply(key);
	}
	
	@Override public V remove(Object key) {
		V ret = super.remove(key);
		if (ret == null)
			try {
				ret = function().apply(castKey(key));
			} catch (ClassCastException ignored) { }
		return ret;
	}
	
	public void set(K key, V value) {
		super.put(key, value);
	}
	
	public void unset(K key) {
		super.remove(key);
	}
	
	@Override public V apply(K input) {
		V ret = super.get(input);
		return ret != null ? ret : function().apply(input);
	}
	
	@Override public V get(Object key) {
		V ret = super.get(key);
		if (ret == null)
			try {
				ret = function().apply(castKey(key));
			} catch (ClassCastException ignored) { }
		return ret;
	}
	
}
