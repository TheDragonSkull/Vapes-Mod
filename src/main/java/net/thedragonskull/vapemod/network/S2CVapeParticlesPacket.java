package net.thedragonskull.vapemod.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.vapemod.VapeMod;

import java.util.UUID;

public record S2CVapeParticlesPacket(UUID playerId, int color) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<S2CVapeParticlesPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "vape_particles_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CVapeParticlesPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    S2CVapeParticlesPacket::playerId,
                    ByteBufCodecs.INT,
                    S2CVapeParticlesPacket::color,
                    S2CVapeParticlesPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
