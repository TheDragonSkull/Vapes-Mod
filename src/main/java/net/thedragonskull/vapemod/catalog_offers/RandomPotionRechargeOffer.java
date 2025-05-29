package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.List;

public class RandomPotionRechargeOffer implements ISpecialOfferLogic{

    @Override
    public boolean canTrade(Player player, VapeCatalogOffers offer) {
        return VapeCatalogUtil.hasItemInTagWithZeroEnergy(player, offer.getCostATag()) &&
                VapeCatalogUtil.hasEnoughOf(player, offer.getCostB());
    }

    @Override
    public void removeCost(ServerPlayer player, VapeCatalogOffers offer) {
        VapeCatalogUtil.removeCurrency(player, VapeCatalogUtil.getVisualCostAWithTagInfo(offer.getCostATag()), offer.getCostB());
    }

    @Override
    public ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer) {
        ItemStack base = VapeCatalogUtil.getFirstStackInTagWithZeroEnergy(player, offer.getCostATag());
        if (base.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(base.getItem());
        result.getCapability(ForgeCapabilities.ENERGY).ifPresent(cap -> {
            if (cap instanceof VapeEnergy e) {
                VapeEnergy.setInt(e.stack, "Energy", e.getMaxEnergyStored());
            }
        });

        List<Potion> potions = BuiltInRegistries.POTION.stream()
                .filter(p -> !p.getEffects().isEmpty() && p != Potions.EMPTY)
                .toList();

        if (!potions.isEmpty()) {
            Potion random = potions.get(player.getRandom().nextInt(potions.size()));
            PotionUtils.setPotion(result, random);
        }

        return result;
    }


}
