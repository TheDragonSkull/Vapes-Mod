package net.thedragonskull.vapemod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.thedragonskull.vapemod.item.custom.IVape;
import net.thedragonskull.vapemod.item.custom.Vape;
import net.thedragonskull.vapemod.particle.ModParticles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VapeUtil {

    public static Set<Item> collectVapeItems(Player player) {
        Set<Item> cooldownItems = new HashSet<>();

        // Inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof IVape) {
                cooldownItems.add(stack.getItem());
            }
        }

        // Hand
        for (ItemStack handStack : List.of(player.getMainHandItem(), player.getOffhandItem())) {
            if (handStack.getItem() instanceof IVape) {
                cooldownItems.add(handStack.getItem());
            }
        }

        return cooldownItems;
    }

    public static void applyCooldownToVapes(Player player, int ticks) {
        Set<Item> cooldownItems = collectVapeItems(player);
        for (Item itemToCooldown : cooldownItems) {
            player.getCooldowns().addCooldown(itemToCooldown, ticks);
        }
    }

    public static void smokeParticles(Player player) {
        ItemStack stack = ItemStack.EMPTY;

        if (player.getMainHandItem().getItem() instanceof IVape) {
            stack = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof IVape) {
            stack = player.getOffhandItem();
        }

        int red = 255, green = 255, blue = 255;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null && contents.potion().isPresent() && !contents.potion().get().is(Potions.WATER)) {
            int potionColor = contents.getColor();
            red = (potionColor >> 16) & 0xFF;
            green = (potionColor >> 8) & 0xFF;
            blue = potionColor & 0xFF;
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

    public static String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return minutes + ":" + String.format("%02d", seconds);
    }

    private static final String[] ROMAN_NUMERALS = {
            "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"
    };

    public static String toRoman(int number) {
        if (number >= 1 && number < ROMAN_NUMERALS.length) {
            return ROMAN_NUMERALS[number];
        }

        return "";
    }

    public static Component formatEffectName(Component baseName, Component effectName, int level) {
        if (level < 1) {
            return Component.literal("")
                    .append(baseName)
                    .append(" (")
                    .append(effectName)
                    .append(")");
        } else {
            String romanLevel = toRoman(level);
            return Component.literal("")
                    .append(baseName)
                    .append(" (")
                    .append(effectName)
                    .append(" ")
                    .append(romanLevel)
                    .append(")");
        }
    }

}
