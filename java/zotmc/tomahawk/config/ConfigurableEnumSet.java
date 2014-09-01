package zotmc.tomahawk.config;

import java.util.Set;

import zotmc.tomahawk.util.Utils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ConfigurableEnumSet<E extends Enum<E>> extends ConfigurableStringList<Set<E>, E> {
	
	private final Class<E> clz;
	
	ConfigurableEnumSet(Class<E> clz, String category, String key) {
		super(category, key);
		this.clz = clz;
	}
	
	@Override protected Set<E> getInitialValue() {
		return ImmutableSet.of();
	}
	
	@Override protected Function<E, String> toStringFunction() {
		return Utils.enumToName();
	}
	
	@Override protected Function<String, E> valueOfFunction() {
		return new Function<String, E>() {
			@Override public E apply(String input) {
				return Enum.valueOf(clz, input);
			}
		};
	}
	
	@Override protected void setIterable(FluentIterable<E> iterable) {
		value = Sets.immutableEnumSet(iterable);
	}

}
