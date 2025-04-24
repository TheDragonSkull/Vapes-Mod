package net.thedragonskull.vapemod.sound;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ResistanceSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public ResistanceSoundInstance(Player player) {
        super(ModSounds.VAPE_RESISTANCE.get(), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = false;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }


    @Override
    public void tick() {
        if (player.isRemoved() || !player.isUsingItem() || player.isUnderWater()) {
            this.stop();
            return;
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    public void forceStop() {
        this.stop();
    }

}
