package net.thedragonskull.vapemod.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
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

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void vapemod$removeDurabilityText(Player pPlayer, TooltipFlag pIsAdvanced, CallbackInfoReturnable<java.util.List<Component>> cir) {
        ItemStack self = (ItemStack)(Object)this;
        List<Component> original = cir.getReturnValue();
        List<Component> filtered = new ArrayList<>();

        if (self.getItem() instanceof DisposableVape) {
            boolean isFirst = true;

            for (Component line : original) {
                if (isFirst) {
                    filtered.add(line);
                    isFirst = false;
                    continue;
                }

                String plain = line.getString();
                if (!plain.startsWith("Durability:") && !plain.matches("\\d+ / \\d+")) {
                    filtered.add(line);
                }
            }

        }

        if (!filtered.isEmpty()) {
            cir.setReturnValue(filtered);
        }
    }
}
