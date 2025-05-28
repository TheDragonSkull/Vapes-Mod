package net.thedragonskull.vapemod.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class VapeCatalogOffers {
    private final ItemStack costA;
    private final TagKey<Item> costATag;

    private final ItemStack costB;

    private final ItemStack result;
    private final TagKey<Item> resultTag;

    public VapeCatalogOffers(ItemStack costA, ItemStack costB, ItemStack result) {
        this.costA = costA;
        this.costATag = null;
        this.costB = costB;
        this.result = result;
        this.resultTag = null;
    }

    public VapeCatalogOffers(TagKey<Item> costATag, ItemStack costB, TagKey<Item> resultTag) {
        this.costA = ItemStack.EMPTY;
        this.costATag = costATag;
        this.costB = costB;
        this.result = ItemStack.EMPTY;
        this.resultTag = resultTag;
    }

    public boolean isCostAByTag() {
        return costATag != null;
    }

    public boolean isResultByTag() {
        return resultTag != null;
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

    public TagKey<Item> getResultTag() {
        return resultTag;
    }

}
