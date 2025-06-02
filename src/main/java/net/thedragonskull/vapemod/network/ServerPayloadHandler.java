package net.thedragonskull.vapemod.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thedragonskull.vapemod.block.custom.VapeCatalog;
import net.thedragonskull.vapemod.catalog_offers.VapeCatalogOffers;
import net.thedragonskull.vapemod.catalog_offers.VapeOfferRegistry;
import net.thedragonskull.vapemod.screen.VapeCatalogScreen;
import net.thedragonskull.vapemod.sound.ModSounds;

import java.util.List;

public class ServerPayloadHandler {

    private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

    public static ServerPayloadHandler getInstance() {
        return INSTANCE;
    }

    public void handleBuyVape(final C2SBuyVapePacket data, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            List<VapeCatalogOffers> trades = switch (VapeCatalogScreen.TabType.values()[data.tabOrdinal()]) {
                case SPECIAL -> VapeOfferRegistry.getSpecialTrades();
                case NORMAL -> VapeOfferRegistry.getNormalTrades();
                case DISPOSABLES -> VapeOfferRegistry.getDisposableTrades();
            };

            if (data.tradeIndex() >= 0 && data.tradeIndex() < trades.size()) {
                VapeCatalogOffers offer = trades.get(data.tradeIndex());

                if (offer.playerHasEnough(player)) {
                    ItemStack result = offer.getTradeLogic().createResult(player, offer);
                    offer.getTradeLogic().removeCost(player, offer);

                    if (!result.isEmpty()) {
                        if (!player.getInventory().add(result)) {
                            player.drop(result, false);
                        }

                        player.playNotifySound(ModSounds.CATALOG_BUY.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
            }
        });
    }

    public void handleCloseCatalog(C2SCloseCatalogPacket data, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            BlockState state = player.level().getBlockState(data.blockPos());
            if (state.getBlock() instanceof VapeCatalog) {
                player.level().setBlock(data.blockPos(), state.setValue(VapeCatalog.OPEN, false), 3);
                player.level().playSound(null, data.blockPos(), ModSounds.CATALOG_CLOSE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

        });
    }
}
