package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

public class VapeCatalogOffers {
    private final ItemStack costA;
    private final TagKey<Item> costATag;
    private final ItemStack costB;
    private final ItemStack result;
    private final TagKey<Item> resultTag;
    private final ISpecialOfferLogic tradeLogic;

    public VapeCatalogOffers(ItemStack costA, ItemStack costB, ItemStack result, ISpecialOfferLogic logic) {
        this.costA = costA;
        this.costATag = null;
        this.costB = costB;
        this.result = result;
        this.resultTag = null;
        this.tradeLogic = logic;
    }

    public VapeCatalogOffers(TagKey<Item> costATag, ItemStack costB, TagKey<Item> resultTag, ISpecialOfferLogic logic) {
        this.costA = ItemStack.EMPTY;
        this.costATag = costATag;
        this.costB = costB;
        this.result = ItemStack.EMPTY;
        this.resultTag = resultTag;
        this.tradeLogic = logic;
    }

    public VapeCatalogOffers(TagKey<Item> costATag, ItemStack costB, ItemStack visualResult, ISpecialOfferLogic logic) {
        this.costA = ItemStack.EMPTY;
        this.costATag = costATag;
        this.costB = costB;
        this.result = visualResult;
        this.resultTag = null;
        this.tradeLogic = logic;
    }

    public boolean isCostAByTag() {
        return costATag != null;
    }

    public boolean isResultByTag() {
        return resultTag != null;
    }

    public TagKey<Item> getResultTag() {
        return resultTag;
    }

    public ItemStack getCostA() {
        return costA;
    }

    public TagKey<Item> getCostATag() {
        return costATag;
    }

    public ItemStack getCostB() {
        return costB;
    }

    public ItemStack getResult() {
        return result;
    }

    public ISpecialOfferLogic getTradeLogic() {
        return tradeLogic;
    }

    public boolean playerHasEnough(ServerPlayer player) {
        return tradeLogic.canTrade(player, this);
    }

    public boolean clientPlayerHasEnough(Player player) {

        if (tradeLogic instanceof SimpleVapeOffer) {
            return VapeCatalogUtil.hasEnoughCurrency(player, getCostA(), getCostB());
        }

        if (tradeLogic instanceof RandomPotionRechargeOffer) {
            return VapeCatalogUtil.hasItemInTagWithZeroEnergy(player, getCostATag())
                    && VapeCatalogUtil.hasEnoughOf(player, getCostB());
        }

        if (tradeLogic instanceof RerollDisposableOffer offerLogic) {
            int cost = offerLogic.getRerollCostFor(player, getCostATag());
            if (cost == 0) return false;
            return VapeCatalogUtil.hasEnoughOf(player, new ItemStack(Items.DIAMOND, cost));
        }

        if (tradeLogic instanceof RecycleDisposableOffer) {
            return VapeCatalogUtil.hasItemInTagWithZeroDurability(player, getCostATag())
                    && VapeCatalogUtil.hasEnoughOf(player, getCostB());
        }

        if (tradeLogic instanceof ExtensionVapeEffectOffer) {
            return VapeCatalogUtil.hasItemInTagWithPartialEnergy(player, getCostATag())
                    && VapeCatalogUtil.hasEnoughOf(player, getCostB());
        }

        return false;
    }


}
