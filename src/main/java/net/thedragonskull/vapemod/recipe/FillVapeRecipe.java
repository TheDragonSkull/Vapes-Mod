package net.thedragonskull.vapemod.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.item.custom.Vape;


public class FillVapeRecipe extends CustomRecipe {

    public FillVapeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack potion = ItemStack.EMPTY;
        ItemStack vape = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.is(Items.LINGERING_POTION) || stack.is(Items.SPLASH_POTION)) return false;

            if (stack.getItem() instanceof PotionItem) {
                if (!potion.isEmpty()) return false;
                potion = stack;
            } else if (stack.getItem() instanceof Vape) {
                if (!vape.isEmpty()) return false;
                vape = stack;

                var cap = vape.getCapability(Capabilities.EnergyStorage.ITEM);
                boolean isEmpty = cap != null && cap.getEnergyStored() == 0;

                if (!isEmpty) return false;
            } else {
                return false;
            }
        }

        return !potion.isEmpty() && !vape.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider pRegistries) {
        ItemStack potionStack = ItemStack.EMPTY;
        ItemStack vapeInput = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack item = inv.getItem(i);
            if (item.getItem() instanceof PotionItem) {
                potionStack = item;
            } else if (item.getItem() instanceof Vape) {
                vapeInput = item;
            }
        }

        if (potionStack.isEmpty() || vapeInput.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(vapeInput.getItem());

        PotionContents contents = potionStack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            result.set(DataComponents.POTION_CONTENTS, contents);
        }

        var cap = result.getCapability(Capabilities.EnergyStorage.ITEM);
        if (cap instanceof VapeEnergy energy) {
            VapeEnergy.setInt(energy.stack, energy.getMaxEnergyStored()); // ← max energy
        }

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.size(), ItemStack.EMPTY);

        for (int i = 0; i < container.size(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.getItem() instanceof PotionItem) {
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
        return ModRecipes.FILL_VAPE.get();
    }


}

