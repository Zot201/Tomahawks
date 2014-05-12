/*
 * Copyright (c) 2014, Zothf, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package zotmc.tomahawk.util;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

/**
 * A map which has default values according to the function provided.</br>
 * 
 * @author zot
 */
public abstract class FallbackingMap<K, V> extends ForwardingMap<K, V>
		implements Map<K, V>, Function<K, V> {
	
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
