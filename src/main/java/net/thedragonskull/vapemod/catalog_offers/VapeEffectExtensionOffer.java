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

import java.util.Optional;

public class VapeEffectExtensionOffer implements ISpecialOfferLogic{

    @Override
    public boolean canTrade(Player player, VapeCatalogOffers offer) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(offer.getCostATag())) {
                if (PotionUtils.getPotion(stack) == Potions.EMPTY) continue;

                Optional<IEnergyStorage> cap = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
                if (cap.isPresent()) {
                    IEnergyStorage energy = cap.get();
                    if (energy.getEnergyStored() <= 0 && energy.getEnergyStored() < energy.getMaxEnergyStored()) continue;

                    int cost = calculateDiamondCost(energy);
                    if (VapeCatalogUtil.hasEnoughOf(player, new ItemStack(Items.DIAMOND, cost))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void removeCost(ServerPlayer player, VapeCatalogOffers offer) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (!stack.isEmpty() && stack.is(offer.getCostATag())) {
                if (PotionUtils.getPotion(stack) == Potions.EMPTY) continue;

                Optional<IEnergyStorage> cap = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
                if (cap.isPresent()) {
                    IEnergyStorage energy = cap.get();
                    if (energy.getEnergyStored() <= 0) continue;

                    int diamondCost = calculateDiamondCost(energy);
                    stack.shrink(1);
                    VapeCatalogUtil.removeCurrency(player, ItemStack.EMPTY, new ItemStack(Items.DIAMOND, diamondCost));
                    break;
                }
            }
        }
    }

    @Override
    public ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(offer.getCostATag())) {
                if (PotionUtils.getPotion(stack) == Potions.EMPTY) continue;

                Optional<IEnergyStorage> cap = stack.getCapability(ForgeCapabilities.ENERGY).resolve();
                if (cap.isPresent() && cap.get().getEnergyStored() > 0) {
                    ItemStack result = new ItemStack(stack.getItem());
                    PotionUtils.setPotion(result, PotionUtils.getPotion(stack));

                    result.getCapability(ForgeCapabilities.ENERGY).ifPresent(c -> {
                        if (c instanceof VapeEnergy energy) {
                            VapeEnergy.setInt(result, "Energy", energy.getMaxEnergyStored());
                        }
                    });

                    return result;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private int calculateDiamondCost(IEnergyStorage energy) {
        int missing = energy.getMaxEnergyStored() - energy.getEnergyStored();
        return 1 + (missing / 2);
    }
}
