package net.thedragonskull.vapemod.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class ClientSoundHandler {
    private static final Map<UUID, ResistanceSoundInstance> PLAYING_SOUNDS = new HashMap<>();

    public static void start(Player player) {
        UUID uuid = player.getUUID();
        if (PLAYING_SOUNDS.containsKey(uuid)) return;

        ResistanceSoundInstance sound = new ResistanceSoundInstance(player);
        Minecraft.getInstance().getSoundManager().play(sound);
        PLAYING_SOUNDS.put(uuid, sound);
    }

    public static void stop(Player player) {
        UUID uuid = player.getUUID();
        ResistanceSoundInstance sound = PLAYING_SOUNDS.remove(uuid);
        if (sound != null) {
            sound.stopNow();
        }
    }

    public static void clear(UUID uuid) {
        PLAYING_SOUNDS.remove(uuid);
    }
}

