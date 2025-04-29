package net.thedragonskull.vapemod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;
import net.thedragonskull.vapemod.VapeMod;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = ChannelBuilder.named(
            new ResourceLocation(VapeMod.MOD_ID, "main"))
            .serverAcceptedVersions((status, version) -> true)
            .clientAcceptedVersions((status, version) -> true)
            .networkProtocolVersion(1)
            .simpleChannel();

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
        INSTANCE.send(msg, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        INSTANCE.send(msg, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToAllPlayer(Object msg) {
        INSTANCE.send(msg, PacketDistributor.ALL.noArg());
    }
}
