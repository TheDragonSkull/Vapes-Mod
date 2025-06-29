package net.thedragonskull.vapemod.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;

import java.util.function.UnaryOperator;

public class ModDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, VapeMod.MOD_ID);

    public static final RegistryObject<DataComponentType<Integer>> ENERGY = register("energy",
            builder -> builder.persistent(Codec.INT));

    public static final RegistryObject<DataComponentType<Boolean>> RANDOMIZED_POTION = register("randomized_potion",
            builder -> builder.persistent(Codec.BOOL));

    public static final RegistryObject<DataComponentType<String>> TAG_KEY = register("tag_key",
            builder -> builder.persistent(Codec.STRING));

    private static <T> RegistryObject<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
