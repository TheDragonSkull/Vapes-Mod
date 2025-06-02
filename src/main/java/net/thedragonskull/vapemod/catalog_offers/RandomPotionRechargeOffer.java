package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.List;

public class RandomPotionRechargeOffer implements ISpecialOfferLogic {

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
        IEnergyStorage energy = result.getCapability(Capabilities.EnergyStorage.ITEM);

        if (energy instanceof VapeEnergy e) {
            VapeEnergy.setInt(e.stack, e.getMaxEnergyStored());
        }


        List<Potion> potions = BuiltInRegistries.POTION.stream()
                .filter(p -> !p.getEffects().isEmpty())
                .toList();

        if (!potions.isEmpty()) {
            Potion randomPotion = potions.get(player.getRandom().nextInt(potions.size()));
            Holder<Potion> randomHolder = BuiltInRegistries.POTION.wrapAsHolder(randomPotion);
            PotionContents contents = new PotionContents(randomHolder);

            result.set(DataComponents.POTION_CONTENTS, contents);
        }

        return result;
    }

}
