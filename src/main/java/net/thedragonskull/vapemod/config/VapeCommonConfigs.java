package net.thedragonskull.vapemod.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;

public class VapeCommonConfigs {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue DISPOSABLE_VAPE_DURABILITY;
    public static final ForgeConfigSpec.IntValue NORMAL_VAPE_CAPACITY;

    public static final ForgeConfigSpec.ConfigValue<String> CATALOG_COST_ITEM;
    public static final ForgeConfigSpec.IntValue PRICE_DISPOSABLE;
    public static final ForgeConfigSpec.IntValue PRICE_NORMAL;

    public static final ForgeConfigSpec.IntValue VAPE_SHOP_GENERATION;

    static {
        BUILDER.push("Configs for Vape Mod");

        DISPOSABLE_VAPE_DURABILITY = BUILDER.comment("Durability (puffs) of disposable vapes")
                .defineInRange("Disposable Vapes durability", 25, 1, 1000);

        NORMAL_VAPE_CAPACITY = BUILDER.comment("Max energy (puffs) for QVape Pen V2 vapes")
                .defineInRange("QVape Pen V2 Max Energy", 15, 1, 1000);


        CATALOG_COST_ITEM = BUILDER.comment("The cost item for the items in the catalog (like emeralds in trades)")
                .comment("Must be a valid item ID like 'minecraft:emerald'")
                .define("Catalog cost item", "minecraft:diamond");

        PRICE_DISPOSABLE = BUILDER.comment("The price for the QVape D Pod vapes")
                .defineInRange("QVape D Pod price", 10, 1, 64);

        PRICE_NORMAL = BUILDER.comment("The price for the QVape Pen V2 vapes")
                .defineInRange("QVape Pen V2 price", 25, 1, 64);


        VAPE_SHOP_GENERATION = BUILDER.comment("How many vape shops will generate")
                .defineInRange("Vape Shop generation", 10, 1, 1000);




        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    public static Item getCatalogCostItem() {
        ResourceLocation id = ResourceLocation.parse(CATALOG_COST_ITEM.get());
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) {
            return Items.DIAMOND;
        }
        return item;
    }

}
