package zotmc.tomahawk.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.StatCollector;

public class DamageSourceFakePlayer extends EntityDamageSourceIndirect {
	
	DamageSourceFakePlayer(FakePlayerTomahawk fakePlayer) {
		super("player", fakePlayer, fakePlayer);
	}

	@Override public ChatMessageComponent getDeathMessage(EntityLivingBase living) {
		String s = (getEntity() == null ? damageSourceEntity : getEntity()).getTranslatedEntityName();
		ItemStack item = getEntity() instanceof EntityLivingBase ? ((EntityLivingBase) getEntity()).getHeldItem() : null;
		String s1 = "death.attack.thrown.item";
		String s2 = s1 + ".item";
		return item != null && item.hasDisplayName() && StatCollector.func_94522_b(s1) ?
				ChatMessageComponent.createFromTranslationWithSubstitutions(s2, living.getTranslatedEntityName(), s, item.getDisplayName())
				: ChatMessageComponent.createFromTranslationWithSubstitutions(s1, living.getTranslatedEntityName(), s);
	}
	
}
