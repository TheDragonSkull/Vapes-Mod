package net.thedragonskull.vapemod.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.thedragonskull.vapemod.item.custom.DisposableVape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    //Removes durability text from tooltip
    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void vapemod$removeDurabilityText(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack self = (ItemStack)(Object)this;
        List<Component> original = cir.getReturnValue();

        if (!(self.getItem() instanceof DisposableVape)) return;

        List<Component> filtered = new ArrayList<>();
        boolean isFirst = true;

        for (Component line : original) {
            if (isFirst) {
                filtered.add(line);
                isFirst = false;
                continue;
            }

            // Check if line is the durability line (translatable key: "item.durability")
            if (line.getContents() instanceof TranslatableContents contents) {
                if (contents.getKey().equals("item.durability")) {
                    continue; // skip it
                }
            }

            // Also filter generic durability formats like "5 / 100"
            String plain = line.getString();
            if (!plain.matches("\\d+ / \\d+")) {
                filtered.add(line);
            }
        }

        cir.setReturnValue(filtered);
    }
}
