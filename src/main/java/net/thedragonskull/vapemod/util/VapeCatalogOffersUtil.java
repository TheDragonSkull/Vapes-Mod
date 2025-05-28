package net.thedragonskull.vapemod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
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

    public static ItemStack getFirstStackInTagWithZeroEnergy(Player player, TagKey<Item> tag) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(tag)) {
                IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if (energy.getEnergyStored() == 0) {
                    return stack.copy();
                }
            }
        }
        return ItemStack.EMPTY;
    }


    public static ItemStack getCycledItemFromTag(TagKey<Item> tag) {
        List<Item> tagItems = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .toList();

        if (tagItems.isEmpty()) return ItemStack.EMPTY;

        long time = System.currentTimeMillis() / 1000L;
        int index = (int)(time % tagItems.size());

        return new ItemStack(tagItems.get(index));
    }


}
