package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

public class SimpleVapeOffer implements ISpecialOfferLogic{

    @Override
    public boolean canTrade(Player player, VapeCatalogOffers offer) {
        return VapeCatalogUtil.hasEnoughCurrency(player, offer.getCostA(), offer.getCostB());
    }

    @Override
    public void removeCost(ServerPlayer player, VapeCatalogOffers offer) {
        VapeCatalogUtil.removeCurrency(player, offer.getCostA(), offer.getCostB());
    }

    @Override
    public ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer) {
        return offer.getResult().copy();
    }

}
