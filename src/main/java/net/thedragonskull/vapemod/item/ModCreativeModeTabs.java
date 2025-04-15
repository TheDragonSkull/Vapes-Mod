package net.thedragonskull.vapemod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VapeMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VAPING_TAB = CREATIVE_MODE_TABS.register("vaping_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.VAPE.get()))
                    .title(Component.translatable("creativetab.vaping_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.VAPE_STEEL.get());
                        pOutput.accept(ModItems.VAPE.get());
                        pOutput.accept(ModItems.VAPE_RED.get());
                        pOutput.accept(ModItems.VAPE_YELLOW.get());
                        pOutput.accept(ModItems.VAPE_BLUE.get());
                        pOutput.accept(ModItems.VAPE_RAINBOW.get());
                        pOutput.accept(ModItems.VAPE_METAL.get());

                        //pOutput.accept(ModBlocks.VAPE_EXPOSITOR.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
