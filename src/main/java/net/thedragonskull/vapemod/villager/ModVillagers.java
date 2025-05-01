package net.thedragonskull.vapemod.villager;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.ModBlocks;
import net.thedragonskull.vapemod.sound.ModSounds;

import java.util.function.Supplier;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, VapeMod.MOD_ID);

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, VapeMod.MOD_ID);

    public static final Supplier<PoiType> VAPE_SHOPKEEPER_POI = POI_TYPES.register("vape_shopkeeper_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.VAPE_EXPOSITOR.get().getStateDefinition().getPossibleStates()),
                    1, 1));

    public static final Supplier<VillagerProfession> VAPE_SHOPKEEPER =
            VILLAGER_PROFESSIONS.register("vape_shopkeeper", () -> new VillagerProfession("vape_shopkeeper",
                    holder -> holder.value() == VAPE_SHOPKEEPER_POI.get(), poiTypeHolder -> poiTypeHolder.value() == VAPE_SHOPKEEPER_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(), ModSounds.VAPE_RESISTANCE.get()));


    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }
}
