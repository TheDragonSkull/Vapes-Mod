package net.thedragonskull.vapemod.event;


import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.network.PacketHandler;

@EventBusSubscriber(modid = VapeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CommonModEvents {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
    }

}
