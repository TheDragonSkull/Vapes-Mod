package net.thedragonskull.vapemod.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.vapemod.VapeMod;

import java.util.UUID;

public record S2CStopResistanceSoundPacket(UUID playerId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<S2CStopResistanceSoundPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "stop_resistance_sound_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CStopResistanceSoundPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    S2CStopResistanceSoundPacket::playerId,
                    S2CStopResistanceSoundPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

