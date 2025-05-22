package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.vapemod.particle.ModParticles;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CVapeParticlesPacket {
    private final UUID playerId;
    private final int color;

    public S2CVapeParticlesPacket(UUID playerId, int color) {
        this.playerId = playerId;
        this.color = color;
    }

    public S2CVapeParticlesPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.color = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeInt(color);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;

            Player player = level.getPlayerByUUID(this.playerId);
            if (player == null) return;

            int color = this.color;
            double red = ((color >> 16) & 0xFF) / 255.0;
            double green = ((color >> 8) & 0xFF) / 255.0;
            double blue = (color & 0xFF) / 255.0;

            for (int i = 0; i < 10; i++) {
                double distance = -0.5D;
                double horizontalAngle = Math.toRadians(player.getYRot());
                double verticalAngle = Math.toRadians(player.getXRot());
                double xOffset = distance * Math.sin(horizontalAngle) * Math.cos(verticalAngle);
                double yOffset = distance * Math.sin(verticalAngle);
                double zOffset = -distance * Math.cos(horizontalAngle) * Math.cos(verticalAngle);
                double x = player.getX() + xOffset;
                double y = player.getEyeY() + yOffset;
                double z = player.getZ() + zOffset;

                player.level().addParticle(
                        ModParticles.VAPE_SMOKE_PARTICLES.get(),
                        x, y, z,
                        red, green, blue
                );
            }

        });

        contextSupplier.get().setPacketHandled(true);
    }
}

