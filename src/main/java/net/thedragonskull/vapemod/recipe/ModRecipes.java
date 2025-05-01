package net.thedragonskull.vapemod.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, "vapemod");

    public static final Supplier<RecipeSerializer<FillVapeRecipe>> FILL_VAPE =
            RECIPE_SERIALIZERS.register("fill_vape",
                    () -> new SimpleCraftingRecipeSerializer<>(FillVapeRecipe::new));

    public static final Supplier<RecipeSerializer<ClearVapeRecipe>> CLEAR_VAPE =
            RECIPE_SERIALIZERS.register("clear_vape",
                    () -> new SimpleCraftingRecipeSerializer<>(ClearVapeRecipe::new));


    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
    }
}
