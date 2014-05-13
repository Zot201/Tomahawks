package zotmc.tomahawk.projectile;

import java.lang.ref.WeakReference;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldServer;

public class TomahawkDamage extends EntityDamageSourceIndirect {
	
	public TomahawkDamage(EntityTomahawk tomahawk, Entity attacker) {
		super("thrown", tomahawk, attacker);
	}
	
	public DamageSource faking(WorldServer world) {
		EntityTomahawk tomahawk = (EntityTomahawk) getSourceOfDamage();
		PlayerTomahawk fakePlayer = tomahawk.fakePlayer.get();
		if (fakePlayer == null) {
			fakePlayer = new PlayerTomahawk(world, tomahawk);
			tomahawk.fakePlayer = new WeakReference<PlayerTomahawk>(fakePlayer);
		}
		return new Faking(fakePlayer);
	}
	
	
	private static class Faking extends EntityDamageSourceIndirect {
		
		private Faking(PlayerTomahawk fakePlayer) {
			super("player", fakePlayer, fakePlayer);
		}

		@Override public IChatComponent func_151519_b(EntityLivingBase living) {
			IChatComponent comp = getEntity() == null ?
					damageSourceEntity.func_145748_c_() : getEntity().func_145748_c_();
			ItemStack item = getEntity() instanceof EntityLivingBase ?
					((EntityLivingBase) getEntity()).getHeldItem() : null;
			String s = "death.attack.thrown.item";
			String s1 = s + ".item";
			return item != null && item.hasDisplayName() && StatCollector.canTranslate(s1) ?
					new ChatComponentTranslation(s1, living.func_145748_c_(), comp, item.func_151000_E()) :
						new ChatComponentTranslation(s, living.func_145748_c_(), comp);
		}
		
	}

}
