package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ISpecialOfferLogic {
    boolean canTrade(Player player, VapeCatalogOffers offer);
    void removeCost(ServerPlayer player, VapeCatalogOffers offer);
    ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer);
}
