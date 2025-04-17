package net.thedragonskull.vapemod.villager;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.ModBlocks;
import net.thedragonskull.vapemod.sound.ModSounds;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, VapeMod.MOD_ID);

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, VapeMod.MOD_ID);

    public static final RegistryObject<PoiType> VAPE_SHOPKEEPER_POI = POI_TYPES.register("vape_shopkeeper_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.VAPE_EXPOSITOR.get().getStateDefinition().getPossibleStates()),
                    1, 1));

    public static final RegistryObject<VillagerProfession> VAPE_SHOPKEEPER =
            VILLAGER_PROFESSIONS.register("vape_shopkeeper", () -> new VillagerProfession("vape_shopkeeper",
                    holder -> holder.get() == VAPE_SHOPKEEPER_POI.get(), poiTypeHolder -> poiTypeHolder.get() == VAPE_SHOPKEEPER_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(), ModSounds.VAPE_RESISTANCE.get()));


    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }
}
