package zotmc.tomahawk.transform;

import org.apache.logging.log4j.Logger;

public interface Patcher {
	
	public TypePredicate targetType();
	
	public byte[] patch(byte[] basicClass, Logger log) throws Throwable;
	
}
