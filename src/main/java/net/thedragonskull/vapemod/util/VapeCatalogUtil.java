package net.thedragonskull.vapemod.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.thedragonskull.vapemod.component.ModDataComponentTypes;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VapeCatalogUtil {

    // ==[ COMPONENT HELPERS ]==

    public static boolean isTagCost(ItemStack stack) {
        return stack.get(ModDataComponentTypes.TAG_KEY.get()) != null;
    }

    public static TagKey<Item> getTagFromCostA(ItemStack stack) {
        String tagId = stack.get(ModDataComponentTypes.TAG_KEY.get());
        if (tagId == null) return null;

        return TagKey.create(Registries.ITEM, ResourceLocation.parse(tagId));
    }

    public static ItemStack getVisualCostAWithTagInfo(TagKey<Item> tag) {
        ItemStack visual = getFirstItemFromTag(tag);
        if (!visual.isEmpty()) {
            visual.set(ModDataComponentTypes.TAG_KEY.get(), tag.location().toString());

            visual.setDamageValue(0);
        }
        return visual;
    }

    public static ItemStack getFirstItemFromTag(TagKey<Item> tag) {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .findFirst()
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
    }

    public static ItemStack getCycledItemFromTag(TagKey<Item> tag) {
        List<Item> tagItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .toList();

        if (tagItems.isEmpty()) return ItemStack.EMPTY;

        long time = System.currentTimeMillis() / 1000L;
        int index = (int)(time % tagItems.size());

        return new ItemStack(tagItems.get(index));
    }

    // ==[ ENERGY STACK QUERIES ]==

    public static boolean hasItemInTagWithZeroEnergy(Player player, TagKey<Item> tag) {
        for (ItemStack stack : getAllRelevantStacks(player)) {
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
        for (ItemStack stack : getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(tag)) {
                if (stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasItemInTagWithPartialEnergy(Player player, TagKey<Item> tag) {
        for (ItemStack stack : getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(tag)) {

                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                boolean hasEffects = contents != null &&
                        contents.potion()
                                .map(holder -> !holder.value().getEffects().isEmpty())
                                .orElse(false);

                if (!hasEffects) continue;

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

    public static int countItemsInTagWithFullDurability(Player player, TagKey<Item> tag) {
        int count = 0;
        for (ItemStack stack : getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(tag) && stack.getDamageValue() == 0) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public static ItemStack getFirstStackInTagWithZeroEnergy(Player player, TagKey<Item> tag) {
        for (ItemStack stack : getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(tag)) {
                IEnergyStorage energy = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
                if (energy.getEnergyStored() == 0) {
                    return stack.copy();
                }
            }
        }
        return ItemStack.EMPTY;
    }


    // ==[ STACK CONSUMPTION ]==

    public static void removeCurrency(Player player, ItemStack costA, ItemStack costB) {
        takeFromInventory(player, costA);
        takeFromInventory(player, costB);
    }

    private static void takeFromInventory(Player player, ItemStack required) {
        if (required == null || required.isEmpty()) return;

        int remaining = required.getCount();

        List<ItemStack> stacks = getAllRelevantStacks(player);

        if (isTagCost(required)) {
            TagKey<Item> tag = getTagFromCostA(required);

            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);

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
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);

                if (stack.getItem() == required.getItem()) {
                    int removed = Math.min(stack.getCount(), remaining);
                    stack.shrink(removed);
                    remaining -= removed;
                    if (remaining <= 0) break;
                }
            }
        }
    }

    // ==[ UTILS ]==

    public static List<ItemStack> getAllRelevantStacks(Player player) {
        List<ItemStack> stacks = new ArrayList<>(player.getInventory().items);

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (!stacks.contains(mainHand)) {
            stacks.add(mainHand);
        }

        if (!stacks.contains(offHand)) {
            stacks.add(offHand);
        }

        return stacks;
    }

    public static Item getCatalogCostItem() {
        ResourceLocation id = ResourceLocation.parse(VapeCommonConfigs.CATALOG_COST_ITEM.get());
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == Items.AIR) {
            return Items.DIAMOND;
        }
        return item;
    }

    public static boolean hasEnoughCurrency(Player player, ItemStack costA, ItemStack costB) {
        boolean costAOk = isTagCost(costA)
                ? hasItemInTagWithZeroEnergy(player, getTagFromCostA(costA))
                : hasEnoughOf(player, costA);

        boolean costBOk = costB == null || costB.isEmpty() || hasEnoughOf(player, costB);

        return costAOk && costBOk;
    }

    public static boolean hasEnoughOf(Player player, ItemStack required) {
        if (required.isEmpty()) return true;

        int requiredCount = required.getCount();
        int found = 0;

        for (ItemStack stack : getAllRelevantStacks(player)) {
            if (stack.getItem() == required.getItem()) {
                found += stack.getCount();
                if (found >= requiredCount) {
                    return true;
                }
            }
        }

        return false;
    }

    // Buy Button
    @OnlyIn(Dist.CLIENT)
    public static class TabAndBuyButton extends Button {
        private final SoundEvent clickSound;

        public TabAndBuyButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Button.OnPress pOnPress, SoundEvent clickSound) {
            super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
            this.clickSound = clickSound;
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            soundManager.play(SimpleSoundInstance.forUI(clickSound, 1.0F, 1.0F));
        }
    }
}
