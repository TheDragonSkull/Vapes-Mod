package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;

import java.util.UUID;

public class S2CResistanceSoundPacket {
    private final UUID playerId;

    public S2CResistanceSoundPacket(UUID playerId) {
        this.playerId = playerId;
    }

    public S2CResistanceSoundPacket(FriendlyByteBuf buf) {
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
            ClientSoundHandler.start(target);
        }

        context.setPacketHandled(true);
    }
}
