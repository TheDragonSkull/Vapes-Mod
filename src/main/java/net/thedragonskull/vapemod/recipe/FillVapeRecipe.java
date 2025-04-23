package net.thedragonskull.vapemod.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.item.custom.Vape;
import net.thedragonskull.vapemod.util.Constants;


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

            if (stack.is(Items.LINGERING_POTION) || stack.is(Items.SPLASH_POTION)) return false;

            if (stack.getItem() instanceof PotionItem) {
                if (!potion.isEmpty()) return false;
                potion = stack;
            } else if (stack.getItem() instanceof Vape) {
                if (!vape.isEmpty()) return false;
                vape = stack;

                boolean isEmpty = vape.getCapability(ForgeCapabilities.ENERGY)
                        .map(storage -> storage.getEnergyStored() == 0)
                        .orElse(false);

                if (!isEmpty) return false;
            } else {
                return false;
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

        ItemStack result = new ItemStack(vapeInput.getItem());

        PotionUtils.setPotion(result, PotionUtils.getPotion(potionStack));
        PotionUtils.setCustomEffects(result, PotionUtils.getCustomEffects(potionStack));

        result.getCapability(ForgeCapabilities.ENERGY).ifPresent(cap -> {
            if (cap instanceof VapeEnergy e) {
                e.setInt(e.stack, Constants.TAG_ENERGY, e.getMaxEnergyStored()); // ← max energy
            }
        });

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < container.getContainerSize(); i++) {
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

