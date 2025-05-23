package net.thedragonskull.vapemod.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.thedragonskull.vapemod.item.custom.Vape;
import net.thedragonskull.vapemod.particle.ModParticles;

import java.util.List;

public class VapeUtil {

    public static void smokeParticles(Player player) {
        ItemStack stack = ItemStack.EMPTY;

        if (player.getMainHandItem().getItem() instanceof Vape) {
            stack = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof Vape) {
            stack = player.getOffhandItem();
        }

        int red = 255, green = 255, blue = 255;

        if (stack.getItem() instanceof Vape) {
            if (!PotionUtils.getPotion(stack).equals(Potions.WATER)) {
                int potionColor = PotionUtils.getColor(stack);
                red = (potionColor >> 16) & 0xFF;
                green = (potionColor >> 8) & 0xFF;
                blue = potionColor & 0xFF;
            }
        }

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

            player.level().addParticle(ModParticles.VAPE_SMOKE_PARTICLES.get(),
                    x, y, z,
                    red / 255.0D, green / 255.0D, blue / 255.0D
            );
        }
    }

    private static final String[] ROMAN_NUMERALS = {
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    public static String toRoman(int number) {
        if (number >= 0 && number < ROMAN_NUMERALS.length) {
            return ROMAN_NUMERALS[number];
        }
        return Integer.toString(number + 1);
    }

}
