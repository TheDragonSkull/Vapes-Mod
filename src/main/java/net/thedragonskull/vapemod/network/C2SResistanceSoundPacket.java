package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.vapemod.sound.ResistanceSoundInstance;

import java.util.UUID;
import java.util.function.Supplier;

public class C2SResistanceSoundPacket {
    private final Vec3 pos;
    private final UUID playerId;

    public C2SResistanceSoundPacket(Vec3 pos, UUID playerId) {
        this.pos = pos;
        this.playerId = playerId;
    }

    public C2SResistanceSoundPacket(FriendlyByteBuf buf) {
        this.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.playerId = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeUUID(playerId);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Player target = mc.level.getPlayerByUUID(this.playerId);
            if (target != null && mc.level != null) {
                mc.getSoundManager().play(new ResistanceSoundInstance(target));
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }


}
