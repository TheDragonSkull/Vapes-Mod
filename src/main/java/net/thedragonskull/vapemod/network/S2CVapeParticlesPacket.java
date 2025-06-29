package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.thedragonskull.vapemod.particle.ModParticles;

import java.util.UUID;

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

    public void handle(CustomPayloadEvent.Context context) {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            Player localPlayer = mc.player;

            if (level == null || localPlayer == null) return;

            if (localPlayer.getUUID().equals(this.playerId)) return;

            Player target = level.getPlayerByUUID(this.playerId);
            if (target == null) return;

            int color = this.color;
            double red = ((color >> 16) & 0xFF) / 255.0;
            double green = ((color >> 8) & 0xFF) / 255.0;
            double blue = (color & 0xFF) / 255.0;

            for (int i = 0; i < 10; i++) {
                double distance = -0.5D;
                double horizontalAngle = Math.toRadians(target.getYRot());
                double verticalAngle = Math.toRadians(target.getXRot());
                double xOffset = distance * Math.sin(horizontalAngle) * Math.cos(verticalAngle);
                double yOffset = distance * Math.sin(verticalAngle);
                double zOffset = -distance * Math.cos(horizontalAngle) * Math.cos(verticalAngle);
                double x = target.getX() + xOffset;
                double y = target.getEyeY() + yOffset;
                double z = target.getZ() + zOffset;

                target.level().addParticle(
                        ModParticles.VAPE_SMOKE_PARTICLES.get(),
                        x, y, z,
                        red, green, blue
                );
            }

        context.setPacketHandled(true);
    }
}
