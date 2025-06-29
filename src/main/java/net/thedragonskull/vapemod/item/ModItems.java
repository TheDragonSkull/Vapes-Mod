package net.thedragonskull.vapemod.item;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.item.custom.DisposableVape;
import net.thedragonskull.vapemod.item.custom.Vape;

import java.util.EnumMap;
import java.util.Map;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VapeMod.MOD_ID);

    // BASIC VAPES
    public static final RegistryObject<Item> VAPE_STEEL = ITEMS.register("vape_steel",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> VAPE = ITEMS.register("vape",
            () -> new Vape(new Item.Properties().stacksTo(1)));
    
    public static final RegistryObject<Item> VAPE_RED = ITEMS.register("vape_red",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> VAPE_YELLOW = ITEMS.register("vape_yellow",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> VAPE_BLUE = ITEMS.register("vape_blue",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> VAPE_RAINBOW = ITEMS.register("vape_rainbow",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> VAPE_METAL = ITEMS.register("vape_metal",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    // DISPOSABLE VAPES
    public static final Map<DyeColor, RegistryObject<Item>> D_VAPES = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor dyeColor : DyeColor.values()) {
            String name = "d_vape_" + dyeColor.getName();
            RegistryObject<Item> item = ITEMS.register(name,
                    () -> new DisposableVape(dyeColor, new Item.Properties().stacksTo(1).durability(25)));
            D_VAPES.put(dyeColor, item);
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
