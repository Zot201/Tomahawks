package zotmc.tomahawk.api;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import cpw.mods.fml.common.eventhandler.Cancelable;

@Cancelable
public class WeaponLaunchEvent extends LivingEvent {
	
	public final ItemStack item;
	public final ItemHandler handler;
	public float initialSpeed;
	private PickUpType pickUpType = PickUpType.SURVIVAL;
	public boolean isForwardSpin = true, isFragile;
	
	public float exhaustion = 0.3F;
	public boolean stopSprinting = true, swingItem = true, playSound = true;
	
	public WeaponLaunchEvent(EntityLivingBase entity, ItemStack item, ItemHandler handler) {
		super(entity);
		this.item = checkNotNull(item);
		this.handler = checkNotNull(handler);
		initialSpeed = handler.getInitialSpeed(item);
	}
	
	public boolean run() {
		if (!MinecraftForge.EVENT_BUS.post(this)) {
			if (stopSprinting)
				entityLiving.setSprinting(false);
			if (swingItem)
				entityLiving.swingItem();
			if (entityLiving instanceof EntityPlayer)
				((EntityPlayer) entityLiving).addExhaustion(exhaustion);
			if (playSound)
				entity.worldObj.playSoundAtEntity(
						entityLiving, "random.bow", 1, 1 / (new Random().nextFloat() * 0.4F + 1.2F) + 0.5F
				);
			
			if (!entity.worldObj.isRemote)
				entity.worldObj.spawnEntityInWorld(handler.createProjectile(this));
			
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

}
