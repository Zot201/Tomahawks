package zotmc.tomahawk.util;

import static com.google.common.base.Preconditions.checkNotNull;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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
				return new IdentityBlockMeta(key.block, key.meta);
			}
		});
	
	public static final IdentityBlockMeta AIR = of(Blocks.air, 0);
	
	public final Block block;
	public final int meta;
	
	private IdentityBlockMeta(Block block, int meta) {
		this.block = checkNotNull(block);
		this.meta = meta;
	}
	
	public static IdentityBlockMeta of(Block block, int meta) {
		return cache.getUnchecked(new IdentityBlockMeta(block, meta));
	}
	
	public NBTTagCompound toNBT() {
		NBTTagCompound tags = new NBTTagCompound();
		tags.setString("block", Utils.getNameForBlock(block));
		tags.setByte("meta", (byte) meta);
		return tags;
	}
	
	public static IdentityBlockMeta readFromNBT(NBTTagCompound tags) {
		return of(Utils.getBlock(tags.getString("block")), tags.getByte("meta"));
	}
	
	@Override public int hashCode() {
		return 31 * block.hashCode() + meta;
	}
	
	@Override public boolean equals(Object obj) {
		if (obj instanceof IdentityBlockMeta) {
			IdentityBlockMeta o = (IdentityBlockMeta) obj;
			return o.block == block && o.meta == meta;
		}
		return false;
	}
	
	@Override public String toString() {
		return Utils.getNameForBlock(block) + "@" + meta;
	}
	
}
