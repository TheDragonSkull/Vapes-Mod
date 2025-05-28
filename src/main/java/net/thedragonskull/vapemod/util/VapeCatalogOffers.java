package net.thedragonskull.vapemod.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class VapeCatalogOffers {
    private final ItemStack costA;
    private final TagKey<Item> costATag;
    private final ItemStack costB;
    private final ItemStack result;

    public VapeCatalogOffers(ItemStack costA, ItemStack costB, ItemStack result) {
        this.costA = costA;
        this.costATag = null;
        this.costB = costB;
        this.result = result;
    }

    public VapeCatalogOffers(TagKey<Item> costATag, ItemStack costB, ItemStack result) {
        this.costA = ItemStack.EMPTY;
        this.costATag = costATag;
        this.costB = costB;
        this.result = result;
    }

    public boolean isCostAByTag() {
        return costATag != null;
    }

    public ItemStack getCostA() {
        return costA;
    }

    public TagKey<Item> getCostATag() {
        return costATag;
    }

    public ItemStack getCostB() {
        return costB;
    }

    public ItemStack getResult() {
        return result;
    }

}
