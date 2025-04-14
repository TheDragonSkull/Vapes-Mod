package net.thedragonskull.vapemod.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.particle.custom.VapeSmokeParticles;

@Mod.EventBusSubscriber(modid = VapeMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.VAPE_SMOKE_PARTICLES.get(), VapeSmokeParticles.Provider::new);
    }

}
