package net.thedragonskull.vapemod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.ModBlocks;

import java.util.List;
import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VapeMod.MOD_ID);

    public static final Supplier<CreativeModeTab> VAPING_TAB = CREATIVE_MODE_TABS.register("vaping_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.VAPE.get()))
                    .title(Component.translatable("creativetab.vaping_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        pOutput.accept(ModBlocks.VAPE_EXPOSITOR.get());

                        List<Item> allVapes = List.of(
                                ModItems.VAPE.get(),
                                ModItems.VAPE_RED.get(),
                                ModItems.VAPE_YELLOW.get(),
                                ModItems.VAPE_BLUE.get(),
                                ModItems.VAPE_RAINBOW.get(),
                                ModItems.VAPE_METAL.get(),
                                ModItems.VAPE_STEEL.get()
                        );

                        for (Item vape : allVapes) {
                            ItemStack vapeStack = new ItemStack(vape);
                            pOutput.accept(vapeStack);
                        }

                        for (DeferredItem<Item> vape : ModItems.D_VAPES.values()) {
                            pOutput.accept(new ItemStack(vape.get()));
                        }

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
