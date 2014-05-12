package zotmc.tomahawk.projectile;

import static net.minecraft.entity.SharedMonsterAttributes.attackDamage;
import static zotmc.tomahawk.util.Utils.PI;
import static zotmc.tomahawk.util.Utils.atan2;
import static zotmc.tomahawk.util.Utils.sqrt;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import zotmc.tomahawk.Tomahawk;

import com.mojang.authlib.GameProfile;

public class PlayerTomahawk extends FakePlayer {
	
	private static final GameProfile NAME = new GameProfile(Tomahawk.MODID + ".fakePlayer", "");
	
	private final EntityTomahawk tomahawk;
	
	public PlayerTomahawk(WorldServer world, EntityTomahawk tomahawk) {
		super(world, NAME);
		this.tomahawk = tomahawk;

		motionX = tomahawk.motionX;
		motionY = tomahawk.motionY;
		motionZ = tomahawk.motionZ;
		setLocationAndAngles(tomahawk.posX, tomahawk.posY, tomahawk.posZ,
				180 / PI * atan2(motionZ, motionX) - 90,
				-180 / PI * atan2(motionY, sqrt(motionX * motionX + motionZ * motionZ)));
		
		if (tomahawk.getIsCritical()) {
			fallDistance = Float.MIN_VALUE;
			onGround = false;
			ridingEntity = null;
		}
		
	}
	
	@Override public ItemStack getHeldItem() {
		return tomahawk.getItem();
	}
	
	@Override public ItemStack getCurrentEquippedItem() {
		return tomahawk.getItem();
	}
	
	@Override public void setCurrentItemOrArmor(int slot, ItemStack item) {
		if (slot == 0) {
			if (item != null)
				tomahawk.setItem(item);
			else
				tomahawk.setDead();
		}
	}
	
	@Override public IAttributeInstance getEntityAttribute(IAttribute attr) {
		if (attr != null && attr.equals(attackDamage)) {
			BaseAttributeMap attrs = new ServersideAttributeMap();
			attrs.registerAttribute(attackDamage);
			IAttributeInstance ret = attrs.getAttributeInstance(attackDamage);
			ret.setBaseValue(tomahawk.damageAttr);
			return ret;
		}
		return super.getEntityAttribute(attr);
	}
	
	@Override public boolean isSprinting() {
		return tomahawk.knockbackStr > 0;
	}
	
	@Override public boolean isOnLadder() {
		return !tomahawk.getIsCritical() && super.isOnLadder();
	}
	
	@Override public boolean isInWater() {
		return !tomahawk.getIsCritical() && super.isInWater();
	}
	
	@Override public boolean isPotionActive(Potion potion) {
		if (potion != null && potion.equals(Potion.blindness))
			return !tomahawk.getIsCritical() && super.isPotionActive(potion);
		return super.isPotionActive(potion);
	}
	
}
