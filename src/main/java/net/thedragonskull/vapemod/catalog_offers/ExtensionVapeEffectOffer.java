package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import javax.annotation.Nullable;

public class ExtensionVapeEffectOffer implements ISpecialOfferLogic {

    @Override
    public boolean canTrade(Player player, VapeCatalogOffers offer) {
        ItemStack valid = findMatchingVapeWithPartialEnergy(player, offer.getCostATag());
        if (valid == null) return false;

        IEnergyStorage energy = valid.getCapability(Capabilities.EnergyStorage.ITEM);
        int cost = calculateDiamondCost(energy);
        return VapeCatalogUtil.hasEnoughOf(player, new ItemStack(Items.DIAMOND, cost));
    }

    @Override
    public void removeCost(ServerPlayer player, VapeCatalogOffers offer) {
        ItemStack valid = findMatchingVapeWithPartialEnergy(player, offer.getCostATag());
        if (valid == null) return;

        IEnergyStorage cap = valid.getCapability(Capabilities.EnergyStorage.ITEM);
        if (cap != null) {
            int cost = calculateDiamondCost(cap);
            valid.shrink(1);
            VapeCatalogUtil.removeCurrency(player, ItemStack.EMPTY, new ItemStack(Items.DIAMOND, cost));
        }
    }

    @Override
    public ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer) {
        ItemStack valid = findMatchingVapeWithPartialEnergy(player, offer.getCostATag());
        if (valid == null) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(valid.getItem());

        PotionContents contents = valid.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            result.set(DataComponents.POTION_CONTENTS, contents);
        }

        IEnergyStorage energy = valid.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy instanceof VapeEnergy v) {
            VapeEnergy.setInt(result, v.getMaxEnergyStored());
        }

        return result;
    }

    public int getDiamondCostFor(Player player, VapeCatalogOffers offer) {
        ItemStack valid = findMatchingVapeWithPartialEnergy(player, offer.getCostATag());
        if (valid != null) {
            IEnergyStorage energy = valid.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energy != null) return calculateDiamondCost(energy);
        }
        return 1;
    }

    private int calculateDiamondCost(IEnergyStorage energy) {
        int missing = energy.getMaxEnergyStored() - energy.getEnergyStored();
        return 1 + (missing / 2);
    }

    @Nullable
    private ItemStack findMatchingVapeWithPartialEnergy(Player player, TagKey<Item> tag) {
        for (ItemStack stack : VapeCatalogUtil.getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(tag)) {
                PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                boolean hasPotion = contents != null &&
                        contents.potion().map(holder -> !holder.value().getEffects().isEmpty()).orElse(false);

                if (!hasPotion) continue;

                IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
                if (energy != null) {
                    int stored = energy.getEnergyStored();
                    int max = energy.getMaxEnergyStored();
                    if (stored > 0 && stored < max) {
                        return stack;
                    }
                }
            }
        }
        return null;
    }
}

