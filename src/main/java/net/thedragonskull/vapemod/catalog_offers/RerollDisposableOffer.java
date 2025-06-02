package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.List;

public class RerollDisposableOffer implements ISpecialOfferLogic {

    @Override
    public boolean canTrade(Player player, VapeCatalogOffers offer) {
        boolean hasFull = VapeCatalogUtil.hasItemInTagWithFullDurability(player, offer.getCostATag());
        boolean hasCurrency = VapeCatalogUtil.hasEnoughOf(player, offer.getCostB());

        return hasFull && hasCurrency;
    }

    @Override
    public void removeCost(ServerPlayer player, VapeCatalogOffers offer) {
        int rerolled = 0;

        for (ItemStack stack : VapeCatalogUtil.getAllRelevantStacks(player)) {
            if (!stack.isEmpty() && stack.is(offer.getCostATag()) && isNewVape(stack)) {
                List<Potion> potions = BuiltInRegistries.POTION.stream()
                        .filter(p -> !p.getEffects().isEmpty())
                        .toList();

                if (!potions.isEmpty()) {
                    Potion randomPotion = potions.get(player.getRandom().nextInt(potions.size()));
                    Holder<Potion> randomHolder = BuiltInRegistries.POTION.wrapAsHolder(randomPotion);
                    PotionContents contents = new PotionContents(randomHolder);

                    stack.set(DataComponents.POTION_CONTENTS, contents);
                    rerolled++;
                }
            }
        }

        // Consume costB per reroll
        if (rerolled > 0 && !offer.getCostB().isEmpty()) {
            int costPerReroll = 2;
            int totalDiamonds = rerolled * costPerReroll;
            ItemStack totalCost = new ItemStack(Items.DIAMOND, totalDiamonds);
            VapeCatalogUtil.removeCurrency(player, ItemStack.EMPTY, totalCost);
        }
    }


    @Override
    public ItemStack createResult(ServerPlayer player, VapeCatalogOffers offer) {
        return ItemStack.EMPTY;
    }

    private boolean isNewVape(ItemStack stack) {
        return stack.getDamageValue() == 0;
    }

}
