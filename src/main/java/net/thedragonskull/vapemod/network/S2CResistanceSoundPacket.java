package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;

import java.util.UUID;

public record S2CResistanceSoundPacket(UUID playerId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<S2CResistanceSoundPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "resistance_sound_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CResistanceSoundPacket> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    S2CResistanceSoundPacket::playerId,
                    S2CResistanceSoundPacket::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
