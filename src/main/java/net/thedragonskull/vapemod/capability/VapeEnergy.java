package net.thedragonskull.vapemod.capability;

import net.minecraftforge.energy.IEnergyStorage;

public class VapeEnergy implements IEnergyStorage {
    private int energy;
    private final int capacity;

    public VapeEnergy(int capacity) {
        this(capacity, capacity);
    }

    public VapeEnergy(int capacity, int initialEnergy) {
        this.capacity = capacity;
        this.energy = Math.min(initialEnergy, capacity);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int accepted = Math.min(capacity - energy, maxReceive);
        if (!simulate) energy += accepted;
        return accepted;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = Math.min(energy, maxExtract);
        if (!simulate) energy -= extracted;
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public void setEnergy(int energy) {
        this.energy = Math.min(energy, capacity);
    }
}
