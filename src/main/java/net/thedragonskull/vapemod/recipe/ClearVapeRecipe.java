package net.thedragonskull.vapemod.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.item.custom.Vape;

public class ClearVapeRecipe extends CustomRecipe {

    public ClearVapeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }


    @Override
    public boolean matches(CraftingContainer inv, Level level) {
        ItemStack waterBottle = ItemStack.EMPTY;
        ItemStack vape = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof Vape) {
                if (!vape.isEmpty()) return false;
                vape = stack;

                boolean hasEnergy = vape.getCapability(ForgeCapabilities.ENERGY)
                        .map(storage -> storage.getEnergyStored() > 0)
                        .orElse(false);

                if (!hasEnergy) return false;

            } else if (stack.getItem() instanceof PotionItem &&
                    PotionUtils.getPotion(stack) == Potions.WATER) {

                if (!waterBottle.isEmpty()) return false;
                waterBottle = stack;

            } else {
                return false;
            }
        }

        return !vape.isEmpty() && !waterBottle.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access) {
        ItemStack vapeInput = ItemStack.EMPTY;

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof Vape) {
                vapeInput = stack;
                break;
            }
        }

        if (vapeInput.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = vapeInput.copy();
        result.setCount(1);

        PotionUtils.setPotion(result, Potions.EMPTY);
        PotionUtils.setCustomEffects(result, java.util.List.of());

        result.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
            if (storage instanceof VapeEnergy e) {
                VapeEnergy.setInt(e.stack, "Energy", 0); // ← no energy
            }
        });

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof PotionItem &&
                    PotionUtils.getPotion(stack) == Potions.WATER) {
                remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }

        return remaining;
    }


    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CLEAR_VAPE.get();
    }
}
