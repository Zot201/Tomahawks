package zotmc.tomahawk.util;

import com.google.common.collect.ForwardingMap;

public abstract class FallbackingMap<K, V> extends ForwardingMap<K, V> {
	
	protected abstract V fallback(Object input);
	
	
	@Override public V put(K key, V value) {
		V ret = super.put(key, value);
		if (ret == null)
			ret = fallback(key);
		return ret;
	}
	
	@Override public V remove(Object key) {
		V ret = super.remove(key);
		if (ret == null)
			ret = fallback(key);
		return ret;
	}
	
	public void set(K key, V value) {
		super.put(key, value);
	}
	
	public void unset(K key) {
		super.remove(key);
	}
	
	@Override public V get(Object key) {
		V ret = super.get(key);
		if (ret == null)
			ret = fallback(key);
		return ret;
	}
	
}
