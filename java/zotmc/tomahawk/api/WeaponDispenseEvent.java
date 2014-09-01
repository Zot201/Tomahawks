package zotmc.tomahawk.api;

import static com.google.common.base.Preconditions.checkNotNull;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

@Cancelable
public class WeaponDispenseEvent extends Event {
	
	public final ItemStack item;
	public final ItemHandler handler;
	public float initialSpeed = 1.1F;
	public float deviation = 6;
	private PickUpType pickUpType = PickUpType.SURVIVAL;
	public boolean isForwardSpin = true, isFragile;
	
	public final World world;
	public final IBlockSource blockSource;
	private IPosition position;
	private EnumFacing facing;
	
	public WeaponDispenseEvent(IBlockSource blockSource, IPosition position, EnumFacing facing,
			ItemStack item, ItemHandler handler) {
		this.world = blockSource.getWorld();
		this.blockSource = blockSource;
		this.position = checkNotNull(position);
		this.facing = checkNotNull(facing);
		
		this.item = checkNotNull(item);
		this.handler = checkNotNull(handler);
	}
	
	public boolean run() {
		if (!MinecraftForge.EVENT_BUS.post(this)) {
			if (!world.isRemote)
				world.spawnEntityInWorld(handler.createDispenserProjectile(this));
			
			return true;
		}
		return false;
	}
	
	
	public PickUpType getPickUpType() {
		return pickUpType;
	}
	public void setPickUpType(PickUpType pickUpType) {
		this.pickUpType = checkNotNull(pickUpType);
	}
	
	public IPosition getPosition() {
		return position;
	}
	public void setPosition(IPosition position) {
		this.position = checkNotNull(position);
	}
	
	public EnumFacing getFacing() {
		return facing;
	}
	public void setFacing() {
		this.facing = checkNotNull(facing);
	}

}
