package net.thedragonskull.vapemod.util;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;

import java.util.List;

public class VapeUtil {

    public static ItemStack fillVape(ItemStack emptyVape, ItemStack potion) {
        ItemStack filledVape = emptyVape.copy();

        Potion potionType = PotionUtils.getPotion(potion);
        PotionUtils.setPotion(filledVape, potionType);

        List<MobEffectInstance> customEffects = PotionUtils.getCustomEffects(potion);
        PotionUtils.setCustomEffects(filledVape, customEffects);

        return filledVape;
    }


}
