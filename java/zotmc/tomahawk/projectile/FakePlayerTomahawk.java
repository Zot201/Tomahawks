package zotmc.tomahawk.projectile;

import static net.minecraft.entity.SharedMonsterAttributes.attackDamage;
import static zotmc.tomahawk.util.Utils.PI;
import static zotmc.tomahawk.util.Utils.atan2;
import static zotmc.tomahawk.util.Utils.sqrt;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.ServersideAttributeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.FakePlayer;
import zotmc.tomahawk.data.ModData.AxeTomahawk;

public class FakePlayerTomahawk extends FakePlayer {
	
	private static final String NAME = AxeTomahawk.MODID + ".fakePlayer";
	
	public final EntityTomahawk tomahawk;
	
	public FakePlayerTomahawk(WorldServer world, EntityTomahawk tomahawk) {
		super(world, NAME);
		this.tomahawk = tomahawk;
		
		motionX = tomahawk.motionX;
		motionY = tomahawk.motionY;
		motionZ = tomahawk.motionZ;
		setLocationAndAngles(tomahawk.posX, tomahawk.posY, tomahawk.posZ,
				180 / PI * atan2(motionZ, motionX) - 90,
				-180 / PI * atan2(motionY, sqrt(motionX * motionX + motionZ * motionZ))
		);
		
		if (tomahawk.getIsCritical()) {
			fallDistance = Float.MIN_VALUE;
			onGround = false;
			ridingEntity = null;
		}
		
		if (tomahawk.isBurning())
			setFire(5);
		
	}
	
	@Override public ItemStack getHeldItem() {
		return tomahawk.item.get();
	}
	
	@Override public ItemStack getCurrentEquippedItem() {
		return getHeldItem();
	}
	
	@Override public void setCurrentItemOrArmor(int slot, ItemStack item) {
		if (slot == 0) {
			if (item != null)
				tomahawk.item.set(item);
			else
				tomahawk.setDead();
		}
	}
	
	@Override public AttributeInstance getEntityAttribute(Attribute attr) {
		if (attr != null && attr.equals(attackDamage)) {
			BaseAttributeMap attrs = new ServersideAttributeMap();
			attrs.func_111150_b(attackDamage);
			AttributeInstance ret = attrs.getAttributeInstance(attackDamage);
			ret.setAttribute(tomahawk.damageAttr);
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
