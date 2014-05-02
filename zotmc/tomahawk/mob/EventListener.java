package zotmc.tomahawk.mob;

import static cpw.mods.fml.common.eventhandler.EventPriority.HIGH;
import static cpw.mods.fml.common.eventhandler.EventPriority.HIGHEST;
import static cpw.mods.fml.common.eventhandler.EventPriority.LOWEST;
import static net.minecraft.enchantment.EnchantmentHelper.addRandomEnchantment;
import static net.minecraft.util.MathHelper.clamp_int;
import static zotmc.tomahawk.LogTomahawk.mob4j;
import static zotmc.tomahawk.TomahawkRegistry.getEquipReplacement;
import static zotmc.tomahawk.TomahawkRegistry.hasEquipManipulation;
import static zotmc.tomahawk.TomahawkRegistry.isThrower;
import static zotmc.tomahawk.mob.MobStorage.DAMAGING_DROP;
import static zotmc.tomahawk.mob.MobStorage.DROP_MODE;
import static zotmc.tomahawk.mob.MobStorage.DROP_UNCHANGED;
import static zotmc.tomahawk.mob.MobStorage.NO_DROP;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.MOB;
import static zotmc.tomahawk.projectile.EntityTomahawk.PickUpType.SURVIVAL;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.SpecialSpawn;
import zotmc.tomahawk.Tomahawk;
import zotmc.tomahawk.projectile.EntityTomahawk;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventListener {

	public static final String
	SPAWN_HANDLED = Tomahawk.MODID + ".spawnHandled";
	
	private static final float TWEAK_CHANCE = 0.1F;
	
	
	private final Random rand = new Random();
	private int quadNextInt(float n) {
		return (int) (rand.nextFloat() * rand.nextFloat() * n);
	}
	
	@SubscribeEvent(priority = LOWEST)
	public void onSpecialSpawn(SpecialSpawn event) {
		if (!event.world.isRemote
				&& event.isCanceled()
				&& hasEquipManipulation(event.entityLiving.getClass()))
			event.entityLiving.getEntityData().setBoolean(SPAWN_HANDLED, true);
		
	}
	
	@SubscribeEvent public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.world.isRemote)
			return;
		
		if (hasEquipManipulation(event.entity.getClass())) {
			EntityLivingBase living = (EntityLivingBase) event.entity;
			
			if (!living.getEntityData().getBoolean(SPAWN_HANDLED)) {
				living.getEntityData().setBoolean(SPAWN_HANDLED, true);
				
				Item rep = getEquipReplacement(living);
				
				float difficulty = living.worldObj.func_147462_b(living.posX, living.posY, living.posZ);
				
				if (rep != null && rand.nextFloat() < TWEAK_CHANCE * difficulty) {
					
					living.setCurrentItemOrArmor(0, enchanting(rep, difficulty, rand));
					
					int n = quadNextInt(difficulty / 1.5F * 18);
					float dropChance = 0.085F / (4 * n);
					
					for (int i = 0; i < n; i++) {
						ItemStack item = enchanting(rep, difficulty, rand);
						
						if (rand.nextFloat() < dropChance)
							getStackTagCompound(item).setByte(DROP_MODE, DAMAGING_DROP);
						
						MobStorage.store(living, item);
					}
				}
			}
			
		}
		
	}
	
	@SubscribeEvent public void onLivingUpdate(LivingUpdateEvent event) {
		if (!event.entityLiving.worldObj.isRemote
				&& event.entityLiving.isEntityAlive()
				&& isThrower(event.entityLiving)) {
			
			EntityLiving living = (EntityLiving) event.entityLiving;
			
			MobAITasks.onUpdate(living);
			
			World world = living.worldObj;
			
			if (living.canPickUpLoot() && world.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
				ItemStack held = living.getHeldItem();
				
				if (held != null && held.getItem() instanceof ItemAxe) {
					
					@SuppressWarnings("unchecked")
					List<EntityTomahawk> etList = world.getEntitiesWithinAABB(
							EntityTomahawk.class, living.boundingBox.expand(1, 0, 1));
					
					for (EntityTomahawk et : etList)
						if (!et.isDead) {
							ItemStack item = et.getItem();
							
							if (item != null
									&& et.pickUpType.canBePickedUpBy(MOB)
									&& et.readyForPickUp()
									&& MobStorage.store(living, item)) {
								
								byte dropMode = NO_DROP;
							
								if (et.pickUpType.canBePickedUpBy(SURVIVAL)) {
									dropMode = DROP_UNCHANGED;
									getStackTagCompound(item).setByte(DROP_MODE, dropMode);
								}
								
								et.setDead();
								
								mob4j().debug("%s %s has picked up an EntityTomahawk %s [Drop Mode: %s]",
										living.getClass().getSimpleName(), living.getEntityId(),
										item, dropMode);
							}
						}
					
					@SuppressWarnings("unchecked")
					List<EntityItem> eiList = world.getEntitiesWithinAABB(
							EntityItem.class, living.boundingBox.expand(1, 0, 1));
					
					for (EntityItem ei : eiList)
						if (!ei.isDead) {
							ItemStack item = ei.getEntityItem();
							
							if (item != null
									&& item.getItem() instanceof ItemAxe
									&& MobStorage.store(living, item)) {
								
								getStackTagCompound(item).setByte(DROP_MODE, DROP_UNCHANGED);
								
								ei.setDead();
								
								mob4j().debug("%s %s has picked up an EntityItem %s [Drop Mode: %s]",
										living.getClass().getSimpleName(), living.getEntityId(),
										item, DROP_UNCHANGED);
							}
						}
					
				}
			}
		}
		
	}
	
	@SubscribeEvent(priority = HIGHEST)
	public void onDespawn(AllowDespawn event) {
		if (!event.entityLiving.worldObj.isRemote
				&& isThrower(event.entityLiving)
				&& MobStorage.isPersistenceRequired(event.entityLiving))
			event.setResult(Result.DENY);
		
	}
	
	@SubscribeEvent(priority = HIGH)
	public void onLivingDrops(LivingDropsEvent event) {
		EntityLivingBase elb = event.entityLiving;
		if (elb.worldObj.isRemote
				|| elb.isChild()
				|| !elb.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))
			return;
		
		MobStorage storage = MobStorage.getStorage(elb);
		if (storage != null)
			for (ItemStack item : storage.clear())
				if (item.stackTagCompound != null) {
					
					int dropMode = item.stackTagCompound.getByte(DROP_MODE);
					
					if (event.recentlyHit && dropMode != NO_DROP) {
						item.stackTagCompound.removeTag(DROP_MODE);
						
						if (dropMode == DAMAGING_DROP)
							item = damaging(item, rand);
						
						EntityItem ei = new EntityItem(event.entityLiving.worldObj,
								elb.posX, elb.posY, elb.posZ, item);
						ei.delayBeforeCanPickup = 10;
						event.drops.add(ei);

						mob4j().debug("%s %s has dropped an EntityItem %s",
								elb.getClass().getSimpleName(), elb.getEntityId(),
								item);
					}
				}
		
		
	}
	
	
	static final String
	MOB_STORAGE = Tomahawk.MODID + ".mobStorage",
	MOB_AI_TASKS = Tomahawk.MODID + ".mobAITasks";
	
	@SubscribeEvent public void onEntityConstruct(EntityConstructing event) {
		if (event.entity.worldObj != null && !event.entity.worldObj.isRemote
				&& isThrower(event.entity)) {
			
			event.entity.registerExtendedProperties(MOB_STORAGE, new MobStorage());
			event.entity.registerExtendedProperties(MOB_AI_TASKS, new MobAITasks());
			
		}
		
	}
	
	
	

	public static ItemStack enchanting(Item item, float difficulty, Random rand) {
		ItemStack stack = new ItemStack(item);
		if (rand.nextFloat() < difficulty * (item instanceof ItemArmor ? 0.5F : 0.25F))
			addRandomEnchantment(rand, stack, 5 + (int) (difficulty * rand.nextInt(18)));
		return stack;
	}
	
	public static ItemStack damaging(ItemStack item, Random rand) {
		if (item.isItemStackDamageable()) {
			int dMax = item.getMaxDamage() + 1;
			int d0 = clamp_int(dMax - item.getItemDamage(), 0, dMax);
			
			float k = Math.max(dMax - 26, 1) * d0 / (float) dMax;
			float d1 = k - k * rand.nextFloat() * rand.nextFloat();
			
			item.setItemDamage((int) Math.min(dMax - d1, k));
		}
		return item;
	}
	
	public static NBTTagCompound getStackTagCompound(ItemStack item) {
		return item.stackTagCompound != null ? item.stackTagCompound
				: (item.stackTagCompound = new NBTTagCompound());
	}
	
	public static NBTTagCompound getNestedTagCompound(NBTTagCompound tags, String key) {
		NBTTagCompound ret = tags.getCompoundTag(key);
		if (ret == null) {
			ret = new NBTTagCompound();
			tags.setTag(key, ret);
		}
		return ret;
	}

}
