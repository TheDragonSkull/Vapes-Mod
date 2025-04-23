package net.thedragonskull.vapemod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.block.ModBlocks;
import net.thedragonskull.vapemod.capability.VapeEnergy;

import java.util.List;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VapeMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VAPING_TAB = CREATIVE_MODE_TABS.register("vaping_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.VAPE.get()))
                    .title(Component.translatable("creativetab.vaping_tab"))
                    .displayItems((pParameters, pOutput) -> {

                        List<RegistryObject<Item>> allVapes = List.of(
                                ModItems.VAPE,
                                ModItems.VAPE_RED,
                                ModItems.VAPE_YELLOW,
                                ModItems.VAPE_BLUE,
                                ModItems.VAPE_RAINBOW,
                                ModItems.VAPE_METAL,
                                ModItems.VAPE_STEEL
                        );

                        for (RegistryObject<Item> vape : allVapes) {
                            ItemStack vapeStack = new ItemStack(vape.get());
/*                            vapeStack.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
                                if (storage instanceof VapeEnergy ve) {
                                    ve.setEnergy(0);
                                }
                            });*/
                            pOutput.accept(vapeStack);
                        }

                        pOutput.accept(ModBlocks.VAPE_EXPOSITOR.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
