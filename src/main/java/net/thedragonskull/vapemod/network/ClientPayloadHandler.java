package net.thedragonskull.vapemod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
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

}
