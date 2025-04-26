package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;
import net.thedragonskull.vapemod.sound.ResistanceSoundInstance;

import java.util.UUID;
import java.util.function.Supplier;

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

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Player target = mc.level.getPlayerByUUID(this.playerId);
            if (target != null) {
                ClientSoundHandler.start(target);
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
