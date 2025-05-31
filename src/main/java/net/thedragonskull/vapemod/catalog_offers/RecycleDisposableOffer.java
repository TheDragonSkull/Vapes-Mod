package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

public class RecycleDisposableOffer implements ISpecialOfferLogic {

    @Override
    public boolean canTrade(Player player, VapeCatalogOffers offer) {
        return VapeCatalogUtil.getAllRelevantStacks(player).stream().anyMatch(stack ->
                !stack.isEmpty() &&
                        stack.is(offer.getCostATag()) &&
                        stack.getDamageValue() >= stack.getMaxDamage()
        );
    }

    @Override
    public void removeCost(ServerPlayer player, VapeCatalogOffers offer) {
        for (ItemStack stack : VapeCatalogUtil.getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(offer.getCostATag()) &&
                    stack.getDamageValue() >= stack.getMaxDamage()) {
                stack.shrink(1);
                break;
            }
        }
    }

    @Override
    public ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer) {
        int price = VapeCommonConfigs.PRICE_DISPOSABLE.get();
        int refund = Math.max(1, (int)(price * 0.25));
        return new ItemStack(VapeCommonConfigs.getCatalogCostItem(), refund);
    }

}
