package net.thedragonskull.vapemod.event;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.entity.ModBlockEntities;
import net.thedragonskull.vapemod.block.entity.renderer.VapeExpositorBERenderer;
import net.thedragonskull.vapemod.item.ModItems;
import net.thedragonskull.vapemod.item.custom.DisposableVape;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.particle.custom.VapeSmokeParticles;
import net.thedragonskull.vapemod.screen.ModMenuTypes;
import net.thedragonskull.vapemod.screen.VapeExpositorScreen;

@Mod.EventBusSubscriber(modid = VapeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.VAPE_EXPOSITOR_MENU.get(), VapeExpositorScreen::new);
        });
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
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColor colorHandler = (stack, tintIndex) -> {
            if (stack.getItem() instanceof DisposableVape vape) {
                float[] rgb = vape.getColor().getTextureDiffuseColors();
                int r = (int)(rgb[0] * 255);
                int g = (int)(rgb[1] * 255);
                int b = (int)(rgb[2] * 255);
                return 0xFF000000 | (r << 16) | (g << 8) | b;
            }
            return 0xFFFFFFFF;
        };

        event.register(colorHandler,
                ModItems.D_VAPES.values().stream().map(RegistryObject::get).toArray(Item[]::new));
    }

}
