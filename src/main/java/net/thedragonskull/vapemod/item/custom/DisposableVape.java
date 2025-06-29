package net.thedragonskull.vapemod.item.custom;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public class DisposableVape extends Item implements IVape {
    private static final String MESSAGE_CANT_SMOKE_UNDERWATER = "message.vapemod.cant_smoke_underwater";
    private static final String MESSAGE_DEPLETED = "message.vapemod.depleted";
    private final DyeColor dyeColor;

    public DisposableVape(DyeColor dyeColor, Properties pProperties) {
        super(pProperties);
        this.dyeColor = dyeColor;
    }

    public DyeColor getColor() {
        return dyeColor;
    }

}
