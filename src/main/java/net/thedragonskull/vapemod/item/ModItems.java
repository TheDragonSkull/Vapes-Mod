package net.thedragonskull.vapemod.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.item.custom.Vape;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(VapeMod.MOD_ID);

    public static final DeferredItem<Item> VAPE_STEEL = ITEMS.register("vape_steel",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VAPE = ITEMS.register("vape",
            () -> new Vape(new Item.Properties().stacksTo(1)));
    
    public static final DeferredItem<Item> VAPE_RED = ITEMS.register("vape_red",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VAPE_YELLOW = ITEMS.register("vape_yellow",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VAPE_BLUE = ITEMS.register("vape_blue",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VAPE_RAINBOW = ITEMS.register("vape_rainbow",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VAPE_METAL = ITEMS.register("vape_metal",
            () -> new Vape(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
