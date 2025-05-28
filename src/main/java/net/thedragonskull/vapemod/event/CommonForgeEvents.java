package net.thedragonskull.vapemod.event;

import net.minecraft.core.registries.Registries;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.catalog_offers.VapeOfferRegistry;

@Mod.EventBusSubscriber(modid = VapeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonForgeEvents {


    @SubscribeEvent
    public static void onTagsLoaded(TagsUpdatedEvent event) {
        event.getRegistryAccess().registryOrThrow(Registries.ITEM);
        VapeOfferRegistry.registerAll();
    }

}
