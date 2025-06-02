package net.thedragonskull.vapemod.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class VapeCommonConfigs {
    public static final VapeCommonConfigs CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.IntValue DISPOSABLE_VAPE_DURABILITY;
    public final ModConfigSpec.IntValue NORMAL_VAPE_CAPACITY;

    public final ModConfigSpec.ConfigValue<String> CATALOG_COST_ITEM;
    public final ModConfigSpec.IntValue PRICE_DISPOSABLE;
    public final ModConfigSpec.IntValue PRICE_NORMAL;

    public final ModConfigSpec.IntValue VAPE_SHOP_GENERATION;

    private VapeCommonConfigs(ModConfigSpec.Builder builder) {

        DISPOSABLE_VAPE_DURABILITY = builder.comment("Durability (puffs) of disposable vapes")
                .defineInRange("Disposable Vapes durability", 25, 1, 1000);

        NORMAL_VAPE_CAPACITY = builder.comment("Max energy (puffs) for QVape Pen V2 vapes")
                .defineInRange("QVape Pen V2 Max Energy", 15, 1, 1000);


        CATALOG_COST_ITEM = builder.comment("The cost item for the vapes in the catalog (like emeralds in trades)")
                .comment("Must be a valid item ID like 'minecraft:emerald'")
                .define("Catalog cost item", "minecraft:diamond");

        PRICE_DISPOSABLE = builder.comment("The price for the QVape D Pod vapes in the catalog")
                .defineInRange("QVape D Pod price", 10, 1, 64);

        PRICE_NORMAL = builder.comment("The price for the QVape Pen V2 vapes the catalog")
                .defineInRange("QVape Pen V2 price", 25, 1, 64);


        VAPE_SHOP_GENERATION = builder.comment("How many vape shops will generate")
                .defineInRange("Vape Shop generation", 10, 1, 1000);

    }

    static {
        Pair<VapeCommonConfigs, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(VapeCommonConfigs::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }

}
