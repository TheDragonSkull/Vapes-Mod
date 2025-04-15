package net.thedragonskull.vapemod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.item.custom.Vape;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, VapeMod.MOD_ID);

    public static final RegistryObject<Item> VAPE_STEEL = ITEMS.register("vape_steel",
            () -> new Vape(new Item.Properties().stacksTo(1).durability(25),
                    new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, false)));

    public static final RegistryObject<Item> VAPE = ITEMS.register("vape",
            () -> new Vape(new Item.Properties().stacksTo(1).durability(25),
                    new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, false, false)));
    
    public static final RegistryObject<Item> VAPE_RED = ITEMS.register("vape_red",
            () -> new Vape(new Item.Properties().stacksTo(1).durability(25),
                    new MobEffectInstance(MobEffects.REGENERATION, 200, 0, false, false)));

    public static final RegistryObject<Item> VAPE_YELLOW = ITEMS.register("vape_yellow",
            () -> new Vape(new Item.Properties().stacksTo(1).durability(25),
                    new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false)));

    public static final RegistryObject<Item> VAPE_BLUE = ITEMS.register("vape_blue",
            () -> new Vape(new Item.Properties().stacksTo(1).durability(25),
                    new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, false, false)));

    public static final RegistryObject<Item> VAPE_RAINBOW = ITEMS.register("vape_rainbow",
            () -> new Vape(new Item.Properties().stacksTo(1).durability(25),
                    new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0, false, false)));

    public static final RegistryObject<Item> VAPE_METAL = ITEMS.register("vape_metal",
            () -> new Vape(new Item.Properties().stacksTo(1).durability(25),
                    new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0, false, false)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
