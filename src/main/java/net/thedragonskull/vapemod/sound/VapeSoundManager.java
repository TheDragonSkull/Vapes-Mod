package net.thedragonskull.vapemod.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VapeSoundManager {
    private static final Map<UUID, ResistanceSoundInstance> ACTIVE_SOUNDS = new HashMap<>();

    public static void playSound(Player player) {
        ResistanceSoundInstance sound = new ResistanceSoundInstance(player);
        Minecraft.getInstance().getSoundManager().play(sound);
        ACTIVE_SOUNDS.put(player.getUUID(), sound);
    }

    public static void stopSound(Player player) {
        ResistanceSoundInstance sound = ACTIVE_SOUNDS.remove(player.getUUID());
        if (sound != null) {
            sound.forceStop();
        }
    }
}
