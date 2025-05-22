package net.thedragonskull.vapemod.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VapeMod.MOD_ID);

    private static RegistryObject<SoundEvent> registerSoundEvents(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, name)));
    }

    public static final RegistryObject<SoundEvent> VAPE_RESISTANCE =
            registerSoundEvents("vape_resistance");

    public static final RegistryObject<SoundEvent> VAPE_RESISTANCE_END =
            registerSoundEvents("vape_resistance_end");

    public static final RegistryObject<SoundEvent> SMOKING_BREATHE_SOUND =
            registerSoundEvents("smoking_breathe_sound");

    public static final RegistryObject<SoundEvent> SMOKING_BREATHE_OUT =
            registerSoundEvents("smoking_breathe_out");

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
