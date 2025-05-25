package net.thedragonskull.vapemod.util;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class VapeCatalogUtil {

    public static boolean hasEnoughCurrency(Player player, ItemStack costA, @Nullable ItemStack costB) {
        return hasEnoughOf(player, costA) && (costB == null || costB.isEmpty() || hasEnoughOf(player, costB));
    }

    public static boolean hasEnoughOf(Player player, ItemStack required) {
        if (required == null || required.isEmpty()) return true;

        int requiredCount = required.getCount();
        int found = 0;

        for (ItemStack stack : player.getInventory().items) {
            if (ItemStack.isSameItemSameTags(stack, required)) {
                found += stack.getCount();
                if (found >= requiredCount) {
                    return true;
                }
            }
        }

        return false;
    }

    // Buy Button
    @OnlyIn(Dist.CLIENT)
    public static class TabAndBuyButton extends Button {
        private final SoundEvent clickSound;

        public TabAndBuyButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress, SoundEvent clickSound) {
            super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
            this.clickSound = clickSound;
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            soundManager.play(SimpleSoundInstance.forUI(clickSound, 1.0F, 1.0F));
        }
    }

}
