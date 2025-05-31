package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import javax.annotation.Nullable;
import java.util.Optional;

public class ExtensionVapeEffectOffer implements ISpecialOfferLogic{

    @Override
    public boolean canTrade(Player player, VapeCatalogOffers offer) {
        ItemStack valid = getValidStack(player, offer);
        if (valid == null) return false;

        int cost = calculateDiamondCost(valid.getCapability(ForgeCapabilities.ENERGY).orElse(null));
        return VapeCatalogUtil.hasEnoughOf(player, new ItemStack(Items.DIAMOND, cost));
    }

    @Override
    public void removeCost(ServerPlayer player, VapeCatalogOffers offer) {
        ItemStack valid = getValidStack(player, offer);
        if (valid == null) return;

        Optional<IEnergyStorage> cap = valid.getCapability(ForgeCapabilities.ENERGY).resolve();
        if (cap.isPresent()) {
            int cost = calculateDiamondCost(cap.get());
            valid.shrink(1);
            VapeCatalogUtil.removeCurrency(player, ItemStack.EMPTY, new ItemStack(Items.DIAMOND, cost));
        }
    }

    @Override
    public ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer) {
        ItemStack valid = getValidStack(player, offer);
        if (valid == null) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(valid.getItem());
        PotionUtils.setPotion(result, PotionUtils.getPotion(valid));

        result.getCapability(ForgeCapabilities.ENERGY).ifPresent(c -> {
            if (c instanceof VapeEnergy energy) {
                VapeEnergy.setInt(result, "Energy", energy.getMaxEnergyStored());
            }
        });

        return result;
    }

    private int calculateDiamondCost(IEnergyStorage energy) {
        int missing = energy.getMaxEnergyStored() - energy.getEnergyStored();
        return 1 + (missing / 2);
    }

    @Nullable
    private ItemStack getValidStack(Player player, VapeCatalogOffers offer) {
        for (ItemStack stack : VapeCatalogUtil.getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(offer.getCostATag())) {
                if (PotionUtils.getPotion(stack) == Potions.EMPTY) continue;

                Optional<IEnergyStorage> cap = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
                if (cap.isPresent()) {
                    IEnergyStorage energy = cap.get();
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

    public int getDiamondCostFor(Player player, VapeCatalogOffers offer) {
        for (ItemStack stack : VapeCatalogUtil.getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(offer.getCostATag())) {
                if (PotionUtils.getPotion(stack) == Potions.EMPTY) continue;

                Optional<IEnergyStorage> cap = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
                if (cap.isPresent()) {
                    IEnergyStorage energy = cap.get();
                    int stored = energy.getEnergyStored();
                    int max = energy.getMaxEnergyStored();
                    if (stored > 0 && stored < max) {
                        return calculateDiamondCost(energy);
                    }
                }
            }
        }

        return 1;
    }


}
