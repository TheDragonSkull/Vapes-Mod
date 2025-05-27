package net.thedragonskull.vapemod.util;

import net.minecraft.world.item.ItemStack;

public class VapeCatalogOffers {
    private final ItemStack costA;
    private final ItemStack costB;
    private final ItemStack result;

    public VapeCatalogOffers(ItemStack costA, ItemStack costB, ItemStack result) {
        this.costA = costA;
        this.costB = costB;
        this.result = result;
    }

    public ItemStack getCostA() {
        return costA;
    }

    public ItemStack getCostB() {
        return costB;
    }

    public ItemStack getResult() {
        return result;
    }

}
