package net.thedragonskull.vapemod.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;

public class VapeEnergy implements IEnergyStorage {

    public ItemStack stack;
    public VapeEnergyContainer container;

    public VapeEnergy(ItemStack stack, VapeEnergyContainer container) {
        this.stack = stack;
        this.container = container;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return container.receiveEnergy(stack, maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return container.extractEnergy(stack, maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return container.getEnergy(stack);
    }

    @Override
    public int getMaxEnergyStored() {
        return container.getCapacity(stack);
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public static void setInt(ItemStack stack, String key, int value) {
        CompoundCheck(stack);
        stack.getTag().putInt(key, value);
    }

    public static void CompoundCheck(ItemStack stack) {
        if (!stack.hasTag()) {
            CompoundTag tag = new CompoundTag();
            stack.setTag(tag);
        }
    }
}
