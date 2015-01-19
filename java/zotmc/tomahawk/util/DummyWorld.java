package zotmc.tomahawk.util;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.logging.ILogAgent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import com.google.common.collect.ImmutableList;

public class DummyWorld extends World {
	
	public DummyWorld() {
		super(new DummySavehandler(), "dummy world",
				new WorldSettings(new WorldInfo(new NBTTagCompound())),
				new WorldProviderSurface(), new Profiler(), new DummyLogAgent());
	}
	
	@Override protected IChunkProvider createChunkProvider() {
		return new DummyChunkProvider(this);
	}
	/*@Override protected int func_152379_p() {
		return 0;
	}*/
	@Override public Entity getEntityByID(int i) {
		return EntityList.createEntityByID(i, this);
	}
	
	private static class DummySavehandler implements ISaveHandler {
		@Override public WorldInfo loadWorldInfo() {
			return null;
		}
		@Override public void checkSessionLock() throws MinecraftException { }
		@Override public IChunkLoader getChunkLoader(WorldProvider provide) {
			return null;
		}
		@Override public void saveWorldInfoWithPlayer(WorldInfo info, NBTTagCompound nbt) { }
		@Override public void saveWorldInfo(WorldInfo info) { }
		@Override public IPlayerFileData getSaveHandler() {
			return null;
		}
		@Override public void flush() { }
		/*@Override public File getWorldDirectory() {
			return null;
		}*/
		@Override public File getMapFileFromName(String s) {
			return null;
		}
		@Override public String getWorldDirectoryName() {
			return "none";
		}
	}
	
	private static class DummyChunkProvider implements IChunkProvider {
		private final World world;
		private DummyChunkProvider(World world) {
			this.world = world;
		}
		
		@Override public boolean chunkExists(int x, int y) {
			return true;
		}
		@Override public Chunk provideChunk(int x, int y) {
			return new Chunk(world, x, y);
		}
		@Override public Chunk loadChunk(int x, int y) {
			return new Chunk(world, x, y);
		}
		@Override public void populate(IChunkProvider provider, int x, int y) { }
		@Override public boolean saveChunks(boolean flag, IProgressUpdate progress) {
			return false;
		}
		@Override public boolean unloadQueuedChunks() {
			return false;
		}
		@Override public boolean canSave() {
			return false;
		}
		@Override public String makeString() {
			return "dummy chunk provider";
		}
		@SuppressWarnings("rawtypes")
		@Override public List getPossibleCreatures(EnumCreatureType type, int i, int j, int k) {
			return ImmutableList.of();
		}
		@Override public ChunkPosition findClosestStructure(World world, String s, int i, int j, int k) {
			return null;
		}
		@Override public int getLoadedChunkCount() {
			return 0;
		}
		@Override public void recreateStructures(int x, int y) { }
		@Override public void saveExtraData() { }
	}
	
	private static class DummyLogAgent implements ILogAgent {
		private final Logger logger = Logger.getLogger("DummyLogAgent");
		
		@Override public void logInfo(String s) { }
		@Override public Logger func_120013_a() {
			return logger;
		}
		@Override public void logWarning(String s) { }
		@Override public void logWarningFormatted(String s, Object... args) { }
		@Override public void logWarningException(String s, Throwable throwable) { }
		@Override public void logSevere(String s) { }
		@Override public void logSevereException(String s, Throwable throwable) { }
		@Override public void logFine(String s) { }
	}
	
}
