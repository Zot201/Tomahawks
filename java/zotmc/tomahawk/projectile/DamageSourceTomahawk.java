package zotmc.tomahawk.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.WorldServer;

public class DamageSourceTomahawk extends EntityDamageSourceIndirect {
	
	public DamageSourceTomahawk(EntityTomahawk tomahawk, Entity attacker) {
		super("thrown", tomahawk, attacker);
	}
	
	@Override public EntityTomahawk getSourceOfDamage() {
		return (EntityTomahawk) super.getSourceOfDamage();
	}
	
	public DamageSource adaptDamageType(WorldServer world) {
		return new DamageSourceFakePlayer(getSourceOfDamage().getFakePlayer(world));
	}
	
}
