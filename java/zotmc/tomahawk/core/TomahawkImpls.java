package zotmc.tomahawk.core;

import static cpw.mods.fml.relauncher.Side.CLIENT;
import static net.minecraft.enchantment.Enchantment.fireAspect;
import static net.minecraft.enchantment.Enchantment.knockback;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet18Animation;
import net.minecraft.network.packet.Packet53BlockChange;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import zotmc.tomahawk.projectile.DamageSourceTomahawk;
import zotmc.tomahawk.projectile.EntityTomahawk;
import zotmc.tomahawk.projectile.FakePlayerTomahawk;
import zotmc.tomahawk.util.Utils;
import zotmc.tomahawk.util.geometry.Vec3d;
import zotmc.tomahawk.util.geometry.Vec3f;
import cpw.mods.fml.relauncher.SideOnly;

public class TomahawkImpls {
	
	@SideOnly(CLIENT)
	public static void setHit() {
		MovingObjectPosition m = Minecraft.getMinecraft().objectMouseOver;
		if (m != null) {
			Vec3 v = m.hitVec;
			setHit((float) v.xCoord - m.blockX, (float) v.yCoord - m.blockY, (float) v.zCoord - m.blockZ);
		}
	}
	
	public static void setHit(float x, float y, float z) {
		TomahawksCore.instance.hit.setValues(x, y, z);
	}
	
	
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
				
				
				Entity thrower = hawk.shootingEntity;
				
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
							TomahawksCore.instance.log.severe("catching");
							e.printStackTrace();
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
			((WorldServer) attacker.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(
					attacker, new Packet18Animation(victim, 6)
			);
	}
	
	private static void onEnchantmentCritical(Entity attacker, Entity victim) {
		if (attacker.worldObj instanceof WorldServer)
			((WorldServer) attacker.worldObj).getEntityTracker().sendPacketToAllAssociatedPlayers(
					attacker, new Packet18Animation(victim, 7)
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
			
			entity.worldObj.spawnParticle("iconcrack_" + item.getItem().itemID,
					v1.xCoord, v1.yCoord, v1.zCoord, v0.xCoord, v0.yCoord + 0.05, v0.zCoord
			);
		}
	}
	
	
	/**
	 * @see ItemInWorldManager#activateBlockOrUseItem
	 */
	public static boolean activateBlock(PlayerInteractEvent event, EntityPlayer player, ItemStack item, Vec3f hit) {
		World world = event.entity.worldObj;
		int blockId = world.getBlockId(event.x, event.y, event.z);
		Block block = Block.blocksList[blockId];
		boolean isAir = block != null;
		boolean useBlock = !player.isSneaking() || item == null
				|| item.getItem().shouldPassSneakingClickToBlock(world, event.x, event.y, event.z);
		
		if (useBlock) {
			if (event.useBlock != Event.Result.DENY)
				return block.onBlockActivated(
						world, event.x, event.y, event.z,
						player, event.face, hit.x(), hit.y(), hit.z()
				);
			else {
				if (player instanceof EntityPlayerMP)
					((EntityPlayerMP) player).playerNetServerHandler
						.sendPacketToPlayer(new Packet53BlockChange(event.x, event.y, event.z, world));
				return event.useItem != Event.Result.ALLOW;
			}
		}
		
		return false;
	}
	
}
