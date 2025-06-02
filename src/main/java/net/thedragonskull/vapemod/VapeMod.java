package net.thedragonskull.vapemod;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.thedragonskull.vapemod.block.ModBlocks;
import net.thedragonskull.vapemod.block.entity.ModBlockEntities;
import net.thedragonskull.vapemod.catalog_offers.VapeOfferRegistry;
import net.thedragonskull.vapemod.component.ModDataComponentTypes;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.item.ModCreativeModeTabs;
import net.thedragonskull.vapemod.item.ModItems;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.recipe.ModRecipes;
import net.thedragonskull.vapemod.screen.ModMenuTypes;
import net.thedragonskull.vapemod.sound.ModSounds;
import net.thedragonskull.vapemod.villager.ModVillagers;
import org.slf4j.Logger;

@Mod(VapeMod.MOD_ID)
public class VapeMod {
    public static final String MOD_ID = "vapemod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VapeMod(IEventBus modEventBus, ModContainer container) {

        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModParticles.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModVillagers.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModDataComponentTypes.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(VapeMod::onTagsLoaded);

        container.registerConfig(ModConfig.Type.COMMON, VapeCommonConfigs.CONFIG_SPEC, "vapemod-common.toml");
    }

    public static void onTagsLoaded(TagsUpdatedEvent event) {
        event.getRegistryAccess().registryOrThrow(Registries.ITEM);
        VapeOfferRegistry.registerAll();
    }
}
