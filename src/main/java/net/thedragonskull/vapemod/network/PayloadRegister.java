package net.thedragonskull.vapemod.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.thedragonskull.vapemod.VapeMod;

@EventBusSubscriber(modid = VapeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PayloadRegister {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // Server
        registrar.playToClient(
                S2CResistanceSoundPacket.TYPE,
                S2CResistanceSoundPacket.STREAM_CODEC,
                ClientPayloadHandler.getInstance()::handleResistanceSound
                );

        registrar.playToClient(
                S2CStopResistanceSoundPacket.TYPE,
                S2CStopResistanceSoundPacket.STREAM_CODEC,
                ClientPayloadHandler.getInstance()::handleStopResistanceSound
        );

        registrar.playToClient(
                S2CVapeParticlesPacket.TYPE,
                S2CVapeParticlesPacket.STREAM_CODEC,
                ClientPayloadHandler.getInstance()::handleVapeParticles
        );

        // Client
        registrar.playToServer(
                C2SBuyVapePacket.TYPE,
                C2SBuyVapePacket.STREAM_CODEC,
                ServerPayloadHandler.getInstance()::handleBuyVape
        );

        registrar.playToServer(
                C2SCloseCatalogPacket.TYPE,
                C2SCloseCatalogPacket.STREAM_CODEC,
                ServerPayloadHandler.getInstance()::handleCloseCatalog
        );

    }
}
