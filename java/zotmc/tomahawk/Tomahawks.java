package zotmc.tomahawk;

import static zotmc.tomahawk.data.ModData.AxeTomahawk.DEPENDENCIES;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.GUI_FACTORY;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.MODID;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.NAME;
import static zotmc.tomahawk.data.ModData.AxeTomahawk.VERSION;

import java.util.Random;

import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zotmc.tomahawk.api.ItemHandler.EnchantmentAction;
import zotmc.tomahawk.api.ItemHandler.PlaybackType;
import zotmc.tomahawk.api.Launchable.Category;
import zotmc.tomahawk.api.Launchable.ConfigState;
import zotmc.tomahawk.api.Launchable.DispenseFactory;
import zotmc.tomahawk.api.Launchable.Enchanting;
import zotmc.tomahawk.api.Launchable.InitialSpeed;
import zotmc.tomahawk.api.Launchable.LaunchFactory;
import zotmc.tomahawk.api.Launchable.Sound;
import zotmc.tomahawk.api.TomahawkRegistry;
import zotmc.tomahawk.api.WeaponCategory;
import zotmc.tomahawk.api.WeaponDispenseEvent;
import zotmc.tomahawk.api.WeaponLaunchEvent;
import zotmc.tomahawk.config.Config;
import zotmc.tomahawk.data.ModData;
import zotmc.tomahawk.data.ModData.TConstruct;
import zotmc.tomahawk.projectile.EntityTomahawk;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;

@Mod(modid = MODID, name = NAME, version = VERSION, dependencies = DEPENDENCIES, guiFactory = GUI_FACTORY)
public class Tomahawks {

	private final Logger log = LogManager.getFormatterLogger(MODID);

	@EventHandler public void preInit(FMLPreInitializationEvent event) {
		ModData.init(event.getModMetadata());
	}

	@EventHandler public void init(FMLInitializationEvent event) {
		TomahawkRegistry.registerItemHandler(ItemAxe.class, new Object() {
			@Category public WeaponCategory category() {
				return WeaponCategory.AXE;
			}
			@ConfigState public boolean isEnabled() {
				return Config.current().commonAxesThrowing.get();
			}
		});

		if (Loader.isModLoaded(TConstruct.MODID))
			try {
				TomahawkRegistry.registerItemHandler(Class.forName(TConstruct.HATCHET), new Object() {
					@Category public WeaponCategory category() {
						return WeaponCategory.AXE;
					}
					@ConfigState public boolean isEnabled() {
						return Config.current().tiCHatchetsThrowing.get();
					}
					@Enchanting public boolean isEnchantable(ItemStack item, EnchantmentAction action) {
						return false;
					}
				});

				EntityRegistry.registerModEntity(EntityLumberAxe.class, "lumberAxe", 0, this, 64, 18, true);
				TomahawkRegistry.registerItemHandler(Class.forName(TConstruct.LUMBER_AXE), new Object() {
					@Category public WeaponCategory category() {
						return WeaponCategory.AXE;
					}
					@ConfigState public boolean isEnabled() {
						return Config.current().tiCLumerAxesThrowing.get();
					}
					@Enchanting public boolean isEnchantable(ItemStack item, EnchantmentAction action) {
						return false;
					}
					@InitialSpeed public float getInitialSpeed(ItemStack item) {
						return 0.7F * category().getInitialSpeed(item);
					}
					@LaunchFactory public Entity createProjectile(WeaponLaunchEvent event) {
						return new EntityLumberAxe(event);
					}
					@DispenseFactory public Entity createDispenserProjectile(WeaponDispenseEvent event) {
						return new EntityLumberAxe(event);
					}
				});

				/*TomahawkRegistry.registerItemHandler(Class.forName(TConstruct.HAMMER), new Object() {
					@Category public WeaponCategory category() {
						return WeaponCategory.HAMMER;
					}
					@ConfigState protected boolean isEnabled() {
						return Config.current().tiCHammersThrowing.get();
					}
					@Enchanting public boolean isEnchantable(ItemStack item, EnchantmentAction action) {
						return false;
					}
				});*/

				final Random rand = new Random();
				final SoundType hitSound = new SoundType(TConstruct.FRYPAN_HIT, 1, 1) {
					@Override public float getPitch() {
						return (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1.0F;
					}
				};
				TomahawkRegistry.registerItemHandler(Class.forName(TConstruct.FRYING_PAN), new Object() {
					@Category public WeaponCategory category() {
						return WeaponCategory.FRYPAN;
					}
					@ConfigState public boolean isEnabled() {
						return Config.current().tiCFryingPansThrowing.get();
					}
					@Sound public SoundType getSound(ItemStack item, PlaybackType type) {
						return type == PlaybackType.HIT_BLOCK || type == PlaybackType.HIT_ENTITY ?
								hitSound : category().getSound(item, type);
					}
					@Enchanting public boolean isEnchantable(ItemStack item, EnchantmentAction action) {
						return false;
					}
				});

			} catch (Throwable e) {
				log.catching(e);
			}

		if (Loader.isModLoaded(ModData.Mekanism.MODID))
			try {
				TomahawkRegistry.registerItemHandler(Class.forName(ModData.Mekanism.AXE), new Object() {
					@Category public WeaponCategory category() { return WeaponCategory.AXE; }
					@ConfigState public boolean isEnabled() { return Config.current().mekAxesThrowing.get(); }
				});

				TomahawkRegistry.registerItemHandler(Class.forName(ModData.Mekanism.PAXEL), new Object() {
					@Category public WeaponCategory category() { return WeaponCategory.AXE; }
					@ConfigState public boolean isEnabled() { return Config.current().mekPaxelsThrowing.get(); }
				});
			} catch (Throwable e) {
				log.catching(e);
			}

	}

	public static class EntityLumberAxe extends EntityTomahawk {
		public EntityLumberAxe(World world) {
			super(world);
		}
		public EntityLumberAxe(WeaponLaunchEvent event) {
			super(event);
		}
		public EntityLumberAxe(WeaponDispenseEvent event) {
			super(event);
		}
		@Override protected float getDragFactor() {
			return 1.2F * super.getDragFactor();
		}
	}

}
