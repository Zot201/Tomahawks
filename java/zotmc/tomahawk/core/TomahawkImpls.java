package zotmc.tomahawk.core;

import static net.minecraft.enchantment.Enchantment.fireAspect;
import static net.minecraft.enchantment.Enchantment.knockback;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import zotmc.tomahawk.projectile.DamageSourceTomahawk;
import zotmc.tomahawk.projectile.EntityTomahawk;
import zotmc.tomahawk.projectile.FakePlayerTomahawk;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.geometry.Vec3d;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class TomahawkImpls {

	/**
	 * @see EntityPlayer#attackTargetEntityWithCurrentItem
	 */
	public static void onEntityImpactImpl(EntityTomahawk hawk, Vec3d motion, Entity target, Random rand) {
		ItemStack item = hawk.item.get();

		boolean handleAttack = true;
		if (!hawk.worldObj.isRemote && hawk.worldObj instanceof WorldServer) {
			FakePlayerTomahawk fakePlayer = hawk.createFakePlayer((WorldServer) hawk.worldObj);

			handleAttack = !MinecraftForge.EVENT_BUS.post(new AttackEntityEvent(fakePlayer, target))
					&& !item.getItem().onLeftClickEntity(item, fakePlayer, target);
		}

		if (handleAttack && target.canAttackWithItem() && !target.hitByEntity(hawk)) {

			float damage = hawk.damageAttr;
			float enchCrit = 0;
			int knock = hawk.knockbackStr;
			if (target instanceof EntityLivingBase) {
				enchCrit = Utils.getEnchantmentModifierLiving(item, (EntityLivingBase) target);
				knock += EnchantmentHelper.getEnchantmentLevel(knockback.effectId, item);
			}

			if (damage > 0 || enchCrit > 0) {
				boolean critical = hawk.getIsCritical() && target instanceof EntityLivingBase;
				if (critical && damage > 0)
					damage *= 1.5F;

				damage += enchCrit;

				boolean setFire = false;
				int fire = EnchantmentHelper.getEnchantmentLevel(fireAspect.effectId, item);
				if (fire > 0 && target instanceof EntityLivingBase && !target.isBurning()) {
					setFire = true;
					target.setFire(1);
				}


				Entity thrower = hawk.getShootingEntity();

				boolean attacked = target.attackEntityFrom(
						new DamageSourceTomahawk(hawk, thrower != null ? thrower : hawk),
						damage
				);

				if (attacked) {
					if (knock > 0) {
						float hv = motion.norm();
						target.addVelocity(
								(float) motion.x() / hv * knock * 0.5F,
								0.1,
								(float) motion.z() / hv * knock * 0.5F
						);

						motion.multiplyHorz(0.6);
					}

					if (critical)
						onCritical(hawk, target);

					if (enchCrit > 0)
						onEnchantmentCritical(hawk, target);

					if (damage >= 18 && thrower instanceof EntityPlayer)
						((EntityPlayer) thrower).triggerAchievement(AchievementList.overkill);

					//setLastAttacker?
					//hurt player i.e. thorns

					if (thrower instanceof EntityLivingBase)
						try {
							Utils.applyEnchantmentDamageIterator(
									(EntityLivingBase) thrower, item, target
							);
						} catch (Throwable e) {
							TomahawksCore.instance.log.catching(e);
						}

					Entity entity = target;
					if (target instanceof EntityDragonPart) {
						IEntityMultiPart dragon = ((EntityDragonPart) target).entityDragonObj;
						if (dragon != null && dragon instanceof EntityLivingBase)
							entity = (Entity) dragon;
					}

					if (item != null && entity instanceof EntityLivingBase) {
						if (thrower instanceof EntityPlayer)
							item.hitEntity((EntityLivingBase) entity, (EntityPlayer) thrower);
						else
							item.attemptDamageItem(2, rand);

						if (item.stackSize <= 0)
							hawk.onBroken();
					}

					if (target instanceof EntityLivingBase) {
						//addStat

						if (fire > 0)
							target.setFire(fire * 4);
					}

				}
				else if (setFire)
					target.extinguish();

			}
		}
	}

	private static void onCritical(Entity attacker, Entity victim) {
		if (attacker.worldObj instanceof WorldServer)
			((WorldServer) attacker.worldObj).getEntityTracker().func_151248_b(
					attacker, new S0BPacketAnimation(victim, 4)
			);
	}

	private static void onEnchantmentCritical(Entity attacker, Entity victim) {
		if (attacker.worldObj instanceof WorldServer)
			((WorldServer) attacker.worldObj).getEntityTracker().func_151248_b(
					attacker, new S0BPacketAnimation(victim, 5)
			);
	}


	/**
	 * @see EntityLivingBase#renderBrokenItemStack
	 */
	public static void renderBrokenItemStack(Entity entity, ItemStack item, Random rand) {
		entity.playSound("random.break", 0.8F, 0.8F + entity.worldObj.rand.nextFloat() * 0.4F);

		for (int i = 0; i < 5; ++i) {
			Vec3 v0 = Utils.vec3((rand.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0);
			v0.rotateAroundX(-entity.rotationPitch * Utils.PI / 180.0F);
			v0.rotateAroundY(-entity.rotationYaw * Utils.PI / 180.0F);

			Vec3 v1 = Utils.vec3((rand.nextFloat() - 0.5) * 0.3, -rand.nextFloat() * 0.6 - 0.3, 0.6);
			v1.rotateAroundX(-entity.rotationPitch * Utils.PI / 180.0F);
			v1.rotateAroundY(-entity.rotationYaw * Utils.PI / 180.0F);
			v1 = v1.addVector(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);

			entity.worldObj.spawnParticle("iconcrack_" + Item.getIdFromItem(item.getItem()),
					v1.xCoord, v1.yCoord, v1.zCoord, v0.xCoord, v0.yCoord + 0.05, v0.zCoord
			);
		}
	}


	/**
	 * @see ContainerRepair#updateRepairOutput
	 */
	public static void fuseGoldenSword(AnvilUpdateEvent event, Map<Integer, Integer> additionalEnchs) {
		event.output = event.left.copy();
		int c = event.left.getRepairCost();

		Map<Integer, Integer> enchs = Utils.getEnchs(event.output);
		int i = 0;

		for (Entry<Integer, Integer> entry : additionalEnchs.entrySet()) {
			int id = entry.getKey();
			Enchantment ench = Enchantment.enchantmentsList[id];

			int l = Objects.firstNonNull(enchs.get(id), 0);
			int r = entry.getValue();
			int lvl = l == r ? r + 1 : Math.max(l, r);
			int gain = lvl - l;

			boolean applicable = true;
			for (Entry<Integer, Integer> entry1 : enchs.entrySet()) {
				int id1 = entry1.getKey();
				if (id1 != id && !ench.canApplyTogether(Enchantment.enchantmentsList[id1])) {
					applicable = false;
					i += gain;
				}
			}

			if (applicable) {
				enchs.put(id, Math.max(lvl, ench.getMaxLevel()));
				i += getCostPerLvl(ench.getWeight()) * gain;
			}
		}

		int j = 0;
		if (Strings.isNullOrEmpty(event.name)) {
			if (event.left.hasDisplayName()) {
				j = event.left.isItemStackDamageable() ? 7 : event.left.stackSize * 5;
				i += j;
				event.output.func_135074_t();
			}
		}
		else if (!event.name.equals(event.left.getDisplayName())) {
			j = event.left.isItemStackDamageable() ? 7 : event.left.stackSize * 5;
			i += j;

			if (event.left.hasDisplayName())
				c += j / 2;

			event.output.setStackDisplayName(event.name);
		}

		int count = 0;
		for (Entry<Integer, Integer> entry : enchs.entrySet())
			c += ++count + entry.getValue()
					* getCostPerLvl(Enchantment.enchantmentsList[entry.getKey()].getWeight());

		c = Math.max(1, c / 2);

		try {
			if (!event.output.getItem().isBookEnchantable(event.output, event.right))
				event.output = null;
		} catch (Throwable e) {
			TomahawksCore.instance.log.catching(e);
		}

		event.cost = c + i;

		if (i <= 0)
			event.output = null;

		if (j == i && j > 0 && event.cost >= 40)
			event.cost = 39;

		if (event.cost >= 40) // && !thePlayer.capabilities.isCreativeMode
			event.output = null;

		if (event.output != null) {
			int l = event.output.getRepairCost();
			if (event.output.hasDisplayName())
				l = Math.max(0, l - 9);
			l += 2;

			event.output.setRepairCost(l);
			EnchantmentHelper.setEnchantments(enchs, event.output);
		}
	}

	private static int getCostPerLvl(int weight) {
		switch (weight) {
		case 1:
			return 4;
		case 2:
			return 2;
		default:
			return 1;
		}
	}

}
