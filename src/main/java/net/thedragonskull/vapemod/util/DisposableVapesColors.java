package net.thedragonskull.vapemod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.DyeColor;
import net.thedragonskull.vapemod.item.ModItems;

public class DisposableVapesColors {

    public static void register() {
        Minecraft mc = Minecraft.getInstance();
        ItemColors itemColors = mc.getItemColors();

        itemColors.register((stack, tintIndex) -> { // todo: pasar esto a RegisterColorHandlersEvent

                    float[] orangeRGB = DyeColor.CYAN.getTextureDiffuseColors();
                    int r = (int)(orangeRGB[0] * 255);
                    int g = (int)(orangeRGB[1] * 255);
                    int b = (int)(orangeRGB[2] * 255);
                    return 0xFF000000 | (r << 16) | (g << 8) | b;
                },
                ModItems.D_VAPE.get());
    }

}
