package net.thedragonskull.vapemod.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.vapemod.VapeMod;

public record C2SBuyVapePacket(int tradeIndex, int tabOrdinal) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SBuyVapePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "buy_vape_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SBuyVapePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    C2SBuyVapePacket::tradeIndex,
                    ByteBufCodecs.INT,
                    C2SBuyVapePacket::tabOrdinal,
                    C2SBuyVapePacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
