package net.thedragonskull.vapemod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class VapeCatalogOffersUtil {

    public static ItemStack getVisualResultFromTag(TagKey<Item> tag) {
        List<Item> tagItems = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .toList();

        if (tagItems.isEmpty()) return ItemStack.EMPTY;

        long time = System.currentTimeMillis() / 1000L;
        int index = (int)(time % tagItems.size());
        return new ItemStack(tagItems.get(index));
    }

    public static ItemStack getVisualCostAWithTagInfo(TagKey<Item> tag) {
        ItemStack visual = getFirstItemFromTag(tag);
        if (!visual.isEmpty()) {
            CompoundTag nbt = visual.getOrCreateTag();
            nbt.putString("TagKey", tag.location().toString());
        }
        return visual;
    }

    public static ItemStack getFirstItemFromTag(TagKey<Item> tag) {
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .findFirst()
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

}
