package net.thedragonskull.vapemod.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.thedragonskull.vapemod.villager.ModVillagers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerOfferCapMixin extends AbstractVillager {

    public VillagerOfferCapMixin(EntityType<? extends AbstractVillager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow
    public abstract VillagerData getVillagerData();

    @Inject(method = "updateTrades", at = @At("HEAD"), cancellable = true)
    private void onUpdateTrades(CallbackInfo ci) {
        VillagerData villagerData = this.getVillagerData();

        if (villagerData.getProfession() == ModVillagers.VAPE_SHOPKEEPER.get()) {
            MerchantOffers merchantOffers = this.getOffers();
            VillagerTrades.ItemListing[] newTrades = VillagerTrades.TRADES.get(villagerData.getProfession()).get(villagerData.getLevel());

            this.addOffersFromItemListings(merchantOffers, newTrades, 25); // TODO: revisar después de nuevas offers

            ci.cancel();
        }
    }

}
