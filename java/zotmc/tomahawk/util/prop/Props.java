package zotmc.tomahawk.util.prop;

import static com.google.common.base.Preconditions.checkArgument;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.util.geometry.AbsNormalizedAngle;
import zotmc.tomahawk.util.geometry.Angle;
import zotmc.tomahawk.util.geometry.Angle.Unit;

public class Props {
	
	public static void increment(Prop<Integer> prop) {
		prop.set(prop.get() + 1);
	}
	public static void increment(IntProp prop) {
		prop.set(prop.get() + 1);
	}
	
	public static void toggle(BooleanProp prop) {
		prop.set(!prop.get());
	}
	
	public static int toSignum(BooleanProp prop) {
		return prop.get() ? 1 : -1;
	}
	
	
	
	// factories
	
	public static <T extends Enum<T>> Prop<T> ofEnum(final Class<T> clz, final ByteProp byteProp) {
		return new Prop<T>() {
			@Override public T get() {
				return clz.getEnumConstants()[byteProp.get()];
			}
			@Override public void set(T value) {
				byteProp.set((byte) value.ordinal());
			}
		};
	}
	
	public static Angle ofAngle(Unit unit, final Entity entity, final int key) {
		return new AbsNormalizedAngle(unit) {
			@Override protected float getValue() {
				return entity.getDataWatcher().getWatchableObjectFloat(key);
			}
			@Override protected void setValue(float value) {
				entity.getDataWatcher().updateObject(key, value);
			}
		};
	}
	
	public static IntProp ofInt(final Entity entity, final int key) {
		return new IntProp() {
			@Override public int get() {
				return entity.getDataWatcher().getWatchableObjectInt(key);
			}
			@Override public void set(int value) {
				entity.getDataWatcher().updateObject(key, value);
			}
		};
	}
	
	public static BooleanProp ofBoolean(final Entity entity, final int key, int shift) {
		checkArgument(shift < 8);
		final int mask = 1 << shift;
		
		return new BooleanProp() {
			@Override public boolean get() {
				return (getByte() & mask) != 0;
			}
			private byte getByte() {
				return entity.getDataWatcher().getWatchableObjectByte(key);
			}
			
			@Override public void set(boolean value) {
				setByte((byte) (value ? getByte() | mask : getByte() & ~mask));
			}
			private void setByte(byte value) {
				entity.getDataWatcher().updateObject(key, value);
			}
		};
	}
	
	public static ByteProp ofByte(final Entity entity, final int key) {
		return new ByteProp() {
			@Override public byte get() {
				return entity.getDataWatcher().getWatchableObjectByte(key);
			}
			@Override public void set(byte value) {
				entity.getDataWatcher().updateObject(key, value);
			}
		};
	}
	
	public static Prop<ItemStack> ofItemStack(final Entity entity, final int key) {
		return new Prop<ItemStack>() {
			@Override public void set(ItemStack value) {
				entity.getDataWatcher().updateObject(key, value);
				entity.getDataWatcher().setObjectWatched(key);
			}
			@Override public ItemStack get() {
				return entity.getDataWatcher().getWatchableObjectItemStack(key);
			}
		};
	}
	
	public static FloatProp ofFloat(final Entity entity, final int key) {
		return new FloatProp() {
			@Override public float get() {
				return entity.getDataWatcher().getWatchableObjectFloat(key);
			}
			@Override public void set(float value) {
				entity.getDataWatcher().updateObject(key, value);
			}
		};
	}

}
