package net.thedragonskull.vapemod.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.vapemod.VapeMod;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, VapeMod.MOD_ID);

    private static Supplier<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, name)));
    }

    public static final Supplier<SoundEvent> VAPE_RESISTANCE =
            registerSoundEvents("vape_resistance");

    public static final Supplier<SoundEvent> VAPE_RESISTANCE_END =
            registerSoundEvents("vape_resistance_end");

    public static final Supplier<SoundEvent> SMOKING_BREATHE_SOUND =
            registerSoundEvents("smoking_breathe_sound");

    public static final Supplier<SoundEvent> SMOKING_BREATHE_OUT =
            registerSoundEvents("smoking_breathe_out");

    public static final Supplier<SoundEvent> CATALOG_BUY =
            registerSoundEvents("catalog_buy");

    public static final Supplier<SoundEvent> CATALOG_OPEN =
            registerSoundEvents("catalog_open");

    public static final Supplier<SoundEvent> CATALOG_CLOSE =
            registerSoundEvents("catalog_close");

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
