package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;

import java.util.UUID;

public class S2CStopResistanceSoundPacket {
    private final UUID playerId;

    public S2CStopResistanceSoundPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public S2CStopResistanceSoundPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
    }

    public void handle(CustomPayloadEvent.Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player target = mc.level.getPlayerByUUID(this.playerId);
        if (target != null) {
            ClientSoundHandler.stop(target);
        }

        context.setPacketHandled(true);
    }
}

