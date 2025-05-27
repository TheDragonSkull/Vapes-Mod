package net.thedragonskull.vapemod.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.config.VapeCommonConfigs;
import net.thedragonskull.vapemod.util.ModTags;
import net.thedragonskull.vapemod.villager.ModVillagers;

import java.util.List;

@Mod.EventBusSubscriber(modid = VapeMod.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if (event.getType() == ModVillagers.VAPE_SHOPKEEPER.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                if (item.builtInRegistryHolder().is(ModTags.Items.DISPOSABLE_VAPES)) {
                    for (int level = 1; level <= 5; level++) {
                        trades.get(level).add((pTrader, pRandom) -> {
                            ItemStack vape = new ItemStack(item);
                            return new MerchantOffer(
                                    new ItemStack(Items.EMERALD, 25),
                                    vape,
                                    10, 3, 0.0f
                            );
                        });
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void addCustomWanderingTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();

        for (Item item : ForgeRegistries.ITEMS.getValues()) {

             if (item.builtInRegistryHolder().is(ModTags.Items.DISPOSABLE_VAPES)) {

                genericTrades.add((pTrader, pRandom) -> {
                    ItemStack vape = new ItemStack(item);
                    int basePrice = VapeCommonConfigs.PRICE_DISPOSABLE.get();
                    int discountedPrice = Math.max(1, Math.round(basePrice * 0.2f));

                    return new MerchantOffer(
                            new ItemStack(Items.EMERALD, discountedPrice),
                            vape,
                            1, 4, 0.0f
                    );
                });
            }

            if (item.builtInRegistryHolder().is(ModTags.Items.VAPES)) {

                genericTrades.add((pTrader, pRandom) -> {
                    ItemStack vape = new ItemStack(item);
                    int basePrice = VapeCommonConfigs.PRICE_NORMAL.get();
                    int discountedPrice = Math.max(1, Math.round(basePrice * 0.2f));

                    return new MerchantOffer(
                            new ItemStack(Items.EMERALD, discountedPrice),
                            vape,
                            1, 5, 0.0f
                    );
                });
            }
        }
    }

}
