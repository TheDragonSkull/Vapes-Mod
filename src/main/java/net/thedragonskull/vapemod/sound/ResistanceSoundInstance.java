package net.thedragonskull.vapemod.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class ResistanceSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private boolean shouldStop = false;

    public ResistanceSoundInstance(Player player) {
        super(ModSounds.VAPE_RESISTANCE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }


    @Override
    public void tick() {
        if (player.isRemoved() || !player.isUsingItem() || shouldStop) {
            this.stop();
            return;
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    public void stopSound() {
        this.shouldStop = true;
    }
}
