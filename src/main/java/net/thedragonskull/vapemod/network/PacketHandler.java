package net.thedragonskull.vapemod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.thedragonskull.vapemod.VapeMod;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {

        INSTANCE.messageBuilder(S2CResistanceSoundPacket.class, NetworkDirection.PLAY_TO_CLIENT.ordinal())
                .encoder(S2CResistanceSoundPacket::encode)
                .decoder(S2CResistanceSoundPacket::new)
                .consumerMainThread(S2CResistanceSoundPacket::handle)
                .add();

        INSTANCE.messageBuilder(S2CStopResistanceSoundPacket.class, NetworkDirection.PLAY_TO_CLIENT.ordinal() + 1)
                .encoder(S2CStopResistanceSoundPacket::encode)
                .decoder(S2CStopResistanceSoundPacket::new)
                .consumerMainThread(S2CStopResistanceSoundPacket::handle)
                .add();
    }


    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

    public static void sendToAllPlayer(Object msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}
