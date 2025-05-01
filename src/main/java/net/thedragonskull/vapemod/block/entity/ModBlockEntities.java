package net.thedragonskull.vapemod.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.ModBlocks;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, VapeMod.MOD_ID);

    public static final Supplier<BlockEntityType<VapeExpositorBE>> VAPE_EXPOSITOR_BE =
            BLOCK_ENTITIES.register("vape_expositor_be", () ->
                    BlockEntityType.Builder.of(VapeExpositorBE::new,
                            ModBlocks.VAPE_EXPOSITOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
