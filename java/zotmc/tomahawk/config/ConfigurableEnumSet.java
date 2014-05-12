package zotmc.tomahawk.config;

import java.util.Set;

import zotmc.tomahawk.util.Utils;

import com.google.common.base.Enums;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class ConfigurableEnumSet<E extends Enum<E>> extends ConfigurableStringList<Set<E>, E> {
	
	private final Class<E> clz;
	
	private ConfigurableEnumSet(Class<E> clz, String category, String key) {
		super(category, key);
		this.clz = clz;
	}
	
	static <E extends Enum<E>> ConfigurableEnumSet<E> of(
			Class<E> clz, String category, String key) {
		return new ConfigurableEnumSet<E>(clz, category, key);
	}
	
	@Override protected Set<E> getInitialValue() {
		return ImmutableSet.of();
	}
	
	@Override protected Function<E, String> toStringFunction() {
		return Utils.toStringFunction();
	}

	@Override protected Function<String, E> valueOfFunction() {
		return Enums.valueOfFunction(clz);
	}

	@Override protected void setIterable(FluentIterable<E> iterable) {
		value = Sets.immutableEnumSet(iterable);
	}

}
