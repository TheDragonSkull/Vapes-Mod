package net.thedragonskull.vapemod.event;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.animation.VapeAnimation;
import net.thedragonskull.vapemod.block.entity.ModBlockEntities;
import net.thedragonskull.vapemod.block.entity.renderer.VapeExpositorBERenderer;
import net.thedragonskull.vapemod.item.ModItems;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.particle.custom.VapeSmokeParticles;
import net.thedragonskull.vapemod.screen.ModMenuTypes;
import net.thedragonskull.vapemod.screen.VapeExpositorScreen;
import net.thedragonskull.vapemod.util.ModTags;

@EventBusSubscriber(modid = VapeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.VAPE_EXPOSITOR_MENU.get(), VapeExpositorScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.VAPE_SMOKE_PARTICLES.get(), VapeSmokeParticles.Provider::new);
    }

    @SubscribeEvent
    public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.VAPE_EXPOSITOR_BE.get(), VapeExpositorBERenderer::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {

        event.registerItem(
                new VapeAnimation(),
                ModItems.VAPE
        );

/*        for (Item item : BuiltInRegistries.ITEM) {
            if (item.builtInRegistryHolder().is(ModTags.Items.VAPES)) {
                event.registerItem(
                        new VapeAnimation(),
                        item
                );
            }
        }*/
    }


}
