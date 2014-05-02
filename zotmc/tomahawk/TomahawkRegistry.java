package zotmc.tomahawk;

import static net.minecraft.init.Items.golden_axe;
import static net.minecraft.init.Items.golden_sword;
import static net.minecraft.init.Items.iron_axe;
import static net.minecraft.init.Items.iron_sword;
import static net.minecraft.init.Items.stone_axe;
import static net.minecraft.init.Items.stone_sword;

import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zotmc.tomahawk.mob.AITomahawkThrowing;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

public class TomahawkRegistry {
	
	//Feel free to access these fields through reflection, their names are not going to change unless stated.
	private static final Table<Class<? extends EntityLivingBase>, Item, Item>
	equipManipulations = HashBasedTable.create();
	
	private static final Set<Class<? extends EntityLiving>> throwers = Sets.newIdentityHashSet();
	
	//Be careful that there are some changes planned for the AI features. This one may change.
	private static final Map<Class<? extends EntityLiving>, Function<EntityLiving, EntityAITasks>>
	throwerAIFactories = Maps.newIdentityHashMap();
	
	
	
	public static void registerThrower(Class<? extends EntityLiving> living) {
		throwers.add(living);
	}
	
	public static void addEquipManipulation(Class<? extends EntityLivingBase> living,
			Item original, Item replacement) {
		equipManipulations.put(living, original, replacement);
	}
	
	public static void registerThrowerAIFactory(Class<? extends EntityLiving> living,
			Function<EntityLiving, EntityAITasks> aiFactory) {
		throwerAIFactories.put(living, (Function<EntityLiving, EntityAITasks>) aiFactory);
	}
	
	
	
	
	public static boolean isThrower(Entity living) {
		return throwers.contains(living.getClass());
	}
	
	public static boolean hasEquipManipulation(Class<? extends Entity> living) {
		return equipManipulations.containsRow(living);
	}
	
	public static Item getEquipReplacement(EntityLivingBase living, ItemStack original) {
		if (original == null)
			return null;
		
		return equipManipulations.get(living.getClass(), original.getItem());
	}
	
	public static Item getEquipReplacement(EntityLivingBase living) {
		return getEquipReplacement(living, living.getHeldItem());
	}
	
	public static EntityAITasks getThrowerAITasks(Entity entity) {
		Function<EntityLiving, EntityAITasks> factory = throwerAIFactories.get(entity.getClass());
		if (factory != null)
			return factory.apply((EntityLiving) entity);
		
		EntityAITasks tasks = new EntityAITasks(entity.worldObj.theProfiler);
		tasks.addTask(0, new AITomahawkThrowing(false, (EntityLiving) entity));
		return tasks;
	}
	
	
	
	static {
		registerThrower(EntityZombie.class);
		addEquipManipulation(EntityZombie.class, iron_sword, iron_axe);
		
		registerThrower(EntitySkeleton.class);
		addEquipManipulation(EntitySkeleton.class, stone_sword, stone_axe);
		
		registerThrower(EntityPigZombie.class);
		addEquipManipulation(EntityPigZombie.class, golden_sword, golden_axe);
		registerThrowerAIFactory(EntityPigZombie.class, new Function<EntityLiving, EntityAITasks>() {
			
			@Override public EntityAITasks apply(EntityLiving input) {
				final EntityPigZombie pigman = (EntityPigZombie) input;
				
				EntityAITasks tasks = new EntityAITasks(pigman.worldObj.theProfiler);
				tasks.addTask(0, new AITomahawkThrowing(false, input,
						new Supplier<EntityLivingBase>() {
					
					@Override public EntityLivingBase get() {
						Entity target = pigman.getEntityToAttack();
						
						return target instanceof EntityLivingBase ?
								(EntityLivingBase) target : null;
					}
				}));
				
				return tasks;
			}
		});
		
	}

}
