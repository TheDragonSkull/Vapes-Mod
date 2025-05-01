package net.thedragonskull.vapemod.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.item.custom.Vape;

public class ClearVapeRecipe extends CustomRecipe {

    public ClearVapeRecipe(CraftingBookCategory pCategory) {
        super(pCategory);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack waterBottle = ItemStack.EMPTY;
        ItemStack vape = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof Vape) {
                if (!vape.isEmpty()) return false;
                vape = stack;

                boolean hasEnergy = vape.getCapability(ForgeCapabilities.ENERGY)
                        .map(storage -> storage.getEnergyStored() > 0)
                        .orElse(false);

                if (!hasEnergy) return false;

            } else if (stack.getItem() instanceof PotionItem) {

                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                if (contents == null ||contents.potion().isEmpty() || !contents.potion().get().is(Potions.WATER)) {
                    return false;
                }

                if (!waterBottle.isEmpty()) return false;
                waterBottle = stack;

            } else {
                return false;
            }
        }

        return !vape.isEmpty() && !waterBottle.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider pRegistries) {
        ItemStack vapeInput = ItemStack.EMPTY;
        PotionContents potionContents = null;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.getItem() instanceof Vape) {
                vapeInput = stack;
            } else if (stack.getItem() instanceof PotionItem) {
                potionContents = stack.get(DataComponents.POTION_CONTENTS);
            }
        }

        if (vapeInput.isEmpty() || potionContents == null) return ItemStack.EMPTY;

        ItemStack result = vapeInput.copy();
        result.setCount(1);

        result.set(DataComponents.POTION_CONTENTS, potionContents);

        result.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
            if (storage instanceof VapeEnergy e) {
                VapeEnergy.setInt(e.stack, 0); // ← no energy
            }
        });

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.size(), ItemStack.EMPTY);

        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.getItem() instanceof PotionItem) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);

                if (contents != null && contents.potion().isPresent() && contents.potion().get() == Potions.WATER) {
                    remaining.set(i, new ItemStack(Items.GLASS_BOTTLE));
                }
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
