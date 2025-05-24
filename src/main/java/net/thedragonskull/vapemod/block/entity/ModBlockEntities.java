package net.thedragonskull.vapemod.block.entity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, VapeMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<VapeExpositorBE>> VAPE_EXPOSITOR_BE =
            BLOCK_ENTITIES.register("vape_expositor_be", () ->
                    BlockEntityType.Builder.of(VapeExpositorBE::new,
                            ModBlocks.VAPE_EXPOSITOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<VapeCatalogBE>> VAPE_CATALOG_BE =
            BLOCK_ENTITIES.register("vape_catalog_be", () ->
                    BlockEntityType.Builder.of(VapeCatalogBE::new,
                            ModBlocks.VAPE_CATALOG.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
