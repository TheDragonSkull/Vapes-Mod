package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;

public class ClientPayloadHandler {

    private static final ClientPayloadHandler INSTANCE = new ClientPayloadHandler();

    public static ClientPayloadHandler getInstance() {
        return INSTANCE;
    }

    public void handleResistanceSound(final S2CResistanceSoundPacket data, final IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player target = mc.level.getPlayerByUUID(data.playerId());
        if (target != null) {
            ClientSoundHandler.start(target);
        }
    }

    public void handleStopResistanceSound(final S2CStopResistanceSoundPacket data, final IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player target = mc.level.getPlayerByUUID(data.playerId());
        if (target != null) {
            ClientSoundHandler.stop(target);
        }
    }

    public void handleVapeParticles(final S2CVapeParticlesPacket data, IPayloadContext ctx) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Player player = level.getPlayerByUUID(data.playerId());
        if (player == null) return;

        int color = data.color();
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
    }

}
