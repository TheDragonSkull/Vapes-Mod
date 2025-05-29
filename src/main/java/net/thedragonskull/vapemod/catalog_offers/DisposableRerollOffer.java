package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

public class DisposableRerollOffer implements ISpecialOfferLogic{

    @Override
    public boolean canTrade(Player player, VapeCatalogOffers offer) {
        boolean hasFull = VapeCatalogUtil.hasItemInTagWithFullDurability(player, offer.getCostATag());
        boolean hasCurrency = VapeCatalogUtil.hasEnoughOf(player, offer.getCostB());

        return hasFull && hasCurrency;
    }

    @Override
    public void removeCost(ServerPlayer player, VapeCatalogOffers offer) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(offer.getCostATag()) && isNewVape(stack)) {
                stack.shrink(1);
                break;
            }
        }

        VapeCatalogUtil.removeCurrency(player, ItemStack.EMPTY, offer.getCostB());
    }

    @Override
    public ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer) {
        ItemStack original = player.getInventory().items.stream()
                .filter(stack -> !stack.isEmpty() && stack.is(offer.getCostATag()) && isNewVape(stack))
                .findFirst()
                .orElse(ItemStack.EMPTY);

        if (original.isEmpty()) return ItemStack.EMPTY;

        return new ItemStack(original.getItem(), 1);
    }

    private boolean isNewVape(ItemStack stack) {
        return stack.getDamageValue() == 0;
    }
}
