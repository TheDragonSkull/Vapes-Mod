package net.thedragonskull.vapemod.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;


public class VapeCatalogUtil {

    public static boolean hasEnoughCurrency(Player player, ItemStack costA, ItemStack costB) {
        boolean costAOk = isTagCost(costA)
                ? hasItemInTagWithZeroEnergy(player, getTagFromCostA(costA))
                : hasEnoughOf(player, costA);

        boolean costBOk = costB == null || costB.isEmpty() || hasEnoughOf(player, costB);

        return costAOk && costBOk;
    }

    public static boolean isTagCost(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("TagKey");
    }

    public static TagKey<Item> getTagFromCostA(ItemStack stack) {
        String tagId = stack.getTag().getString("TagKey");
        return TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId));
    }

    public static boolean hasItemInTag(Player player, TagKey<Item> tag) {
        return player.getInventory().items.stream().anyMatch(stack -> stack.is(tag));
    }

    public static boolean hasEnoughOf(Player player, ItemStack required) {
        if (required == null || required.isEmpty()) return true;

        int requiredCount = required.getCount();
        int found = 0;

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == required.getItem()) {
                found += stack.getCount();
                if (found >= requiredCount) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean hasItemInTagWithZeroEnergy(Player player, TagKey<Item> tag) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(tag)) {
                IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if (energy.getEnergyStored() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void removeCurrency(Player player, ItemStack costA, ItemStack costB) {
        takeFromInventory(player, costA);
        takeFromInventory(player, costB);
    }

    private static void takeFromInventory(Player player, ItemStack required) {
        if (required == null || required.isEmpty()) return;

        int remaining = required.getCount();

        if (isTagCost(required)) {
            TagKey<Item> tag = getTagFromCostA(required);

            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack stack = player.getInventory().items.get(i);

                if (stack.is(tag)) {
                    IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                    if (energy.getEnergyStored() == 0) {
                        int removed = Math.min(stack.getCount(), remaining);
                        stack.shrink(removed);
                        remaining -= removed;
                        if (remaining <= 0) break;
                    }
                }
            }
        } else {
            for (int i = 0; i < player.getInventory().items.size(); i++) {
                ItemStack stack = player.getInventory().items.get(i);

                if (stack.getItem() == required.getItem()) {
                    int removed = Math.min(stack.getCount(), remaining);
                    stack.shrink(removed);
                    remaining -= removed;
                    if (remaining <= 0) break;
                }
            }
        }
    }


    // Buy Button
    @OnlyIn(Dist.CLIENT)
    public static class TabAndBuyButton extends Button {
        private final SoundEvent clickSound;

        public TabAndBuyButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress, SoundEvent clickSound) {
            super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
            this.clickSound = clickSound;
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            soundManager.play(SimpleSoundInstance.forUI(clickSound, 1.0F, 1.0F));
        }
    }

}
