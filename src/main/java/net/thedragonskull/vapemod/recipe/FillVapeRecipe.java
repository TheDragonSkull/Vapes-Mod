package net.thedragonskull.vapemod.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.item.custom.Vape;


public class FillVapeRecipe extends CustomRecipe {

    public FillVapeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack vape = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof PotionItem) {
                if (!potion.isEmpty()) return false; // solo 1 poción
                potion = stack;
            } else if (stack.getItem() instanceof Vape) {
                if (!vape.isEmpty()) return false; // solo 1 vape
                vape = stack;

                // ✔️ Asegurarse de que el vape esté vacío (energía = 0)
                boolean isEmpty = vape.getCapability(ForgeCapabilities.ENERGY)
                        .map(storage -> storage.getEnergyStored() == 0)
                        .orElse(false); // Si no tiene energía, no sirve

                if (!isEmpty) return false;
            } else {
                return false; // cualquier otro ítem invalida la receta
            }
        }

        return !potion.isEmpty() && !vape.isEmpty();
    }



    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        ItemStack potionStack = ItemStack.EMPTY;
        ItemStack vapeInput = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item.getItem() instanceof PotionItem) {
                potionStack = item;
            } else if (item.getItem() instanceof Vape) {
                vapeInput = item;
            }
        }

        if (potionStack.isEmpty() || vapeInput.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = vapeInput.copy();
        result.setCount(1);

        // 🔹 Copiar los efectos de la poción
        PotionUtils.setPotion(result, PotionUtils.getPotion(potionStack));
        PotionUtils.setCustomEffects(result, PotionUtils.getCustomEffects(potionStack));

        // 🔹 Rellenar energía completamente
        result.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
            if (storage instanceof VapeEnergy ve) {
                ve.setEnergy(ve.getMaxEnergyStored()); // forma segura si usas tu clase
            } else {
                storage.receiveEnergy(storage.getMaxEnergyStored(), false); // forma genérica
            }
        });

        return result;
    }


    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.FILL_VAPE.get(); // Definimos esto luego
    }
}

