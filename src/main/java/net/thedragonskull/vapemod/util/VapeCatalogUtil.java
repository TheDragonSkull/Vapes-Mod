package net.thedragonskull.vapemod.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;


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
                if (stack.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
                    IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                    if (energy.getEnergyStored() == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean hasItemInTagWithZeroDurability(Player player, TagKey<Item> tag) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(tag)) {
                if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
                    return true;
                }
            }
        }
        return false;
    }


    public static boolean hasItemInTagWithFullDurability(Player player, TagKey<Item> tag) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(tag)) {
                if (stack.getDamageValue() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasItemInTagWithPartialEnergy(Player player, TagKey<Item> tag) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(tag)) {

                if (PotionUtils.getPotion(stack) == Potions.EMPTY) continue;

                Optional<IEnergyStorage> cap = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
                if (cap.isPresent()) {
                    IEnergyStorage energy = cap.get();
                    int stored = energy.getEnergyStored();
                    int max = energy.getMaxEnergyStored();
                    if (stored > 0 && stored < max) {
                        return true;
                    }
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

                if (!stack.isEmpty() && stack.is(tag)) {
                    // for ENERGY
                    if (stack.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
                        IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                        if (energy.getEnergyStored() == 0) {
                            int removed = Math.min(stack.getCount(), remaining);
                            stack.shrink(removed);
                            remaining -= removed;
                            if (remaining <= 0) break;
                        }
                    }
                    // for durability
                    else if (stack.getDamageValue() >= stack.getMaxDamage()) {
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
            visual.setTag(nbt);

            visual.setDamageValue(0);
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

    public static ItemStack getPlaceholderVapeFromTag(TagKey<Item> tag) {
        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .findFirst()
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
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
