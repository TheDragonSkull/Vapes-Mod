package net.thedragonskull.vapemod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.thedragonskull.vapemod.block.custom.VapeCatalog;
import net.thedragonskull.vapemod.sound.ModSounds;

import java.util.function.Supplier;

public class C2SCloseCatalogPacket {
    private final BlockPos blockPos;

    public C2SCloseCatalogPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public C2SCloseCatalogPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
    }

    public static void handle(C2SCloseCatalogPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();

            if (player == null)
                return;

            BlockState state = player.level().getBlockState(msg.blockPos);
            if (state.getBlock() instanceof VapeCatalog) {
                player.level().setBlock(msg.blockPos, state.setValue(VapeCatalog.OPEN, false), 3);
                player.level().playSound(null, msg.blockPos, ModSounds.CATALOG_CLOSE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

        });

        ctx.get().setPacketHandled(true);
    }

}
