package net.thedragonskull.vapemod.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.item.ModItems;
import net.thedragonskull.vapemod.util.ModTags;
import net.thedragonskull.vapemod.villager.ModVillagers;

import java.util.List;

@EventBusSubscriber(modid = VapeMod.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if (event.getType() == ModVillagers.VAPE_SHOPKEEPER.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            for (Item item : BuiltInRegistries.ITEM) {
                if (item.builtInRegistryHolder().is(ModTags.Items.VAPES)
                        && item != ModItems.VAPE_RAINBOW.get()) { //todo: basic

                    trades.get(1).add((pTrader, pRandom) -> {
                        ItemStack vape = new ItemStack(item);

                        return new MerchantOffer(
                                new ItemCost(Items.EMERALD, 45),
                                vape,
                                10, 20, 0.2f
                        );
                    });
                }
            }

            trades.get(2).add((pTrader, pRandom) -> {
                ItemStack vape = new ItemStack(ModItems.VAPE_RAINBOW.get());

                return new MerchantOffer(
                        new ItemCost(Items.EMERALD, 65),
                        vape,
                        10, 30, 0.3f
                );
            });


        }
    }

    @SubscribeEvent
    public static void addCustomWanderingTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item.builtInRegistryHolder().is(ModTags.Items.VAPES)
                    && item != ModItems.VAPE_RAINBOW.get()) {

                genericTrades.add((pTrader, pRandom) -> {
                    ItemStack vape = new ItemStack(item);

                    return new MerchantOffer(
                            new ItemCost(Items.EMERALD, 30),
                            vape,
                            10, 20, 0.2f
                    );
                });
            }
        }
    }

}
