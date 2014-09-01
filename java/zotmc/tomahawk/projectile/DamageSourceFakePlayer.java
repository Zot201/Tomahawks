package zotmc.tomahawk.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public class DamageSourceFakePlayer extends EntityDamageSourceIndirect {
	
	DamageSourceFakePlayer(FakePlayerTomahawk fakePlayer) {
		super("player", fakePlayer, fakePlayer);
	}

	@Override public IChatComponent func_151519_b(EntityLivingBase living) {
		IChatComponent comp = (getEntity() == null ? damageSourceEntity : getEntity()).func_145748_c_();
		ItemStack item = getEntity() instanceof EntityLivingBase ? ((EntityLivingBase) getEntity()).getHeldItem() : null;
		String s = "death.attack.thrown.item";
		String s1 = s + ".item";
		return item != null && item.hasDisplayName() && StatCollector.canTranslate(s1) ?
				new ChatComponentTranslation(s1, living.func_145748_c_(), comp, item.func_151000_E())
				: new ChatComponentTranslation(s, living.func_145748_c_(), comp);
	}
	
}
