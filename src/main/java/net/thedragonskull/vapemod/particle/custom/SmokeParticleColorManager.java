package net.thedragonskull.vapemod.particle.custom;

import net.minecraft.world.item.Item;
import net.thedragonskull.vapemod.item.ModItems;

import java.util.HashMap;
import java.util.Map;

public class SmokeParticleColorManager {

    public static final Map<Item, int[]> ITEM_COLORS = new HashMap<>();

    static {
        //ITEM_COLORS.put(ModItems.VAPER.get(), new int[]{50, 50, 50});  // Black
        //ITEM_COLORS.put(ModItems.VAPER_YELLOW.get(), new int[]{255, 230, 100}); // Gold
        //ITEM_COLORS.put(ModItems.VAPER_RED.get(), new int[]{244, 56, 65}); // Red
        ITEM_COLORS.put(ModItems.VAPE_STEEL.get(), new int[]{246,246,246}); // Steel
        //ITEM_COLORS.put(ModItems.VAPER_BLUE.get(), new int[]{53,85,127}); // Blue
        //ITEM_COLORS.put(ModItems.VAPER_METAL.get(), new int[]{125,125,125}); // Blue
    }

    public static int[] getColorForItem(Item item) {
        return ITEM_COLORS.getOrDefault(item, new int[]{255, 255, 255});
    }

}
