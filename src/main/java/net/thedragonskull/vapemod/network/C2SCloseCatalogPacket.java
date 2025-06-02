package net.thedragonskull.vapemod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.vapemod.VapeMod;

public record C2SCloseCatalogPacket(BlockPos blockPos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SCloseCatalogPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "close_catalog_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCloseCatalogPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    C2SCloseCatalogPacket::blockPos,
                    C2SCloseCatalogPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
