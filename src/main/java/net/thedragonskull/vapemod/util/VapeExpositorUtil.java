package net.thedragonskull.vapemod.util;

import net.minecraft.world.item.ItemStack;
import net.thedragonskull.vapemod.block.entity.VapeExpositorBE;

public class VapeExpositorUtil {

    public static ItemStack getVapeInSlot(VapeExpositorBE be, int slot) {
        if (be == null || be.getInventory() == null || slot < 0 || slot >= be.getInventory().getSlots()) {
            return ItemStack.EMPTY;
        }
        return be.getInventory().getStackInSlot(slot);
    }

    public static boolean isSlotOccupied(VapeExpositorBE be, int slot) {
        return !getVapeInSlot(be, slot).isEmpty();
    }

}
