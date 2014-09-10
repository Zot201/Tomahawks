package zotmc.tomahawk.util.init;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

public final class SimpleVersion implements Comparable<SimpleVersion> {
	
	private static final Ordering<Iterable<Integer>>
	ORDER = Ordering.<Integer>natural().lexicographical();
	
	private final ImmutableList<Integer> parts;
	
	public SimpleVersion(String s) {
		this.parts = parse(s).toList();
	}
	
	private static FluentIterable<Integer> parse(String s) {
		return FluentIterable
				.from(Splitter.on('.').split(s))
				.transform(IntegerParser.INSTANCE);
	}
	
	private int compareTo(Iterable<Integer> parts) {
		return ORDER.compare(this.parts, parts);
	}
	@Override public int compareTo(SimpleVersion version) {
		return compareTo(version.parts);
	}
	
	public boolean isAtLeast(String version) {
		return compareTo(parse(version)) >= 0;
	}
	
	public boolean isBelow(String version) {
		return compareTo(parse(version)) < 0;
	}
	
	@Override public int hashCode() {
		return parts.hashCode();
	}
	
	@Override public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof SimpleVersion)
			return parts.equals(((SimpleVersion) obj).parts);
		return super.equals(obj);
	}
	
	@Override public String toString() {
		return Joiner.on('.').join(parts);
	}
	
	private enum IntegerParser implements Function<String, Integer> {
		INSTANCE;
		@Override public Integer apply(String input) {
			return Integer.parseInt(input);
		}
	}

}
