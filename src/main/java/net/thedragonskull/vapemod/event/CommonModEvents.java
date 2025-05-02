package net.thedragonskull.vapemod.event;


import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.capability.VapeEnergy;
import net.thedragonskull.vapemod.capability.VapeEnergyContainer;
import net.thedragonskull.vapemod.network.PayloadRegister;

@EventBusSubscriber(modid = VapeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonModEvents {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PayloadRegister::register);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof VapeEnergyContainer container) {
                event.registerItem(
                        Capabilities.EnergyStorage.ITEM,
                        (stack, ctx) -> new VapeEnergy(stack, container),
                        item
                );
            }
        }

    }


}
