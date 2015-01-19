package zotmc.tomahawk.util;

import net.minecraft.nbt.NBTTagCompound;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A pair representation for block and metadata that identity (==) comparison is safe to use.
 */
public final class IdentityBlockMeta {
	
	private static final LoadingCache<IdentityBlockMeta, IdentityBlockMeta>
	cache = CacheBuilder.newBuilder()
		.weakValues()
		.build(new CacheLoader<IdentityBlockMeta, IdentityBlockMeta>() {
				@Override public IdentityBlockMeta load(IdentityBlockMeta key) {
				// give a new instance in order to allow proper garbage collection
				return new IdentityBlockMeta(key.blockId, key.meta);
			}
		});
	
	public static final IdentityBlockMeta AIR = of(0, 0);
	
	public final int blockId, meta;
	
	private IdentityBlockMeta(int blockId, int meta) {
		this.blockId = blockId;
		this.meta = meta;
	}
	
	public static IdentityBlockMeta of(int blockId, int meta) {
		return cache.getUnchecked(new IdentityBlockMeta(blockId, meta));
	}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound tags = new NBTTagCompound();
		tags.setShort("blockId", (short) blockId);
		tags.setByte("meta", (byte) meta);
		return tags;
	}
	
	public static IdentityBlockMeta readFromNBT(NBTTagCompound tags) {
		return of(tags.getShort("blockId"), tags.getByte("meta"));
	}
	
	@Override public int hashCode() {
		return 31 * blockId + meta;
	}
	
	@Override public boolean equals(Object obj) {
		if (obj instanceof IdentityBlockMeta) {
			IdentityBlockMeta o = (IdentityBlockMeta) obj;
			return o.blockId == blockId && o.meta == meta;
		}
		return false;
	}
	
	@Override public String toString() {
		return blockId + "@" + meta;
	}
	
}
