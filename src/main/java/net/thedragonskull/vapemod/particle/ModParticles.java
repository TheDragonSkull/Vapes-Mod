package net.thedragonskull.vapemod.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.vapemod.VapeMod;

import java.util.function.Supplier;

public class ModParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, VapeMod.MOD_ID);

    public static final Supplier<SimpleParticleType> VAPE_SMOKE_PARTICLES =
            PARTICLE_TYPES.register("vape_smoke_particles", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

}
