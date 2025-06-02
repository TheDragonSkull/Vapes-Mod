package net.thedragonskull.vapemod.catalog_offers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.util.ModTags;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.ArrayList;
import java.util.List;

public class VapeOfferRegistry {

    private static final List<VapeCatalogOffers> SPECIAL_TRADES = new ArrayList<>();
    private static final List<VapeCatalogOffers> NORMAL_TRADES = new ArrayList<>();
    private static final List<VapeCatalogOffers> DISPOSABLE_TRADES = new ArrayList<>();

    public static void registerNormalTrades() {
        NORMAL_TRADES.clear();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item.builtInRegistryHolder().is(ModTags.Items.VAPES)) {
                NORMAL_TRADES.add(new VapeCatalogOffers(
                        new ItemStack(VapeCatalogUtil.getCatalogCostItem(), VapeCommonConfigs.CONFIG.PRICE_NORMAL.get()),
                        ItemStack.EMPTY,
                        new ItemStack(item),
                        new SimpleVapeOffer()
                ));
            }
        }
    }

    public static void registerDisposableTrades() {
        DISPOSABLE_TRADES.clear();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item.builtInRegistryHolder().is(ModTags.Items.DISPOSABLE_VAPES)) {
                DISPOSABLE_TRADES.add(new VapeCatalogOffers(
                        new ItemStack(VapeCatalogUtil.getCatalogCostItem(), VapeCommonConfigs.CONFIG.PRICE_DISPOSABLE.get()),
                        ItemStack.EMPTY,
                        new ItemStack(item),
                        new SimpleVapeOffer()
                ));
            }
        }
    }

    public static void registerSpecialTrades() {
        SPECIAL_TRADES.clear();


    }

    public static List<VapeCatalogOffers> getSpecialTrades() {
        return SPECIAL_TRADES;
    }

    public static List<VapeCatalogOffers> getNormalTrades() {
        return NORMAL_TRADES;
    }

    public static List<VapeCatalogOffers> getDisposableTrades() {
        return DISPOSABLE_TRADES;
    }

    public static void registerAll() {
        registerSpecialTrades();
        registerNormalTrades();
        registerDisposableTrades();
    }
}
