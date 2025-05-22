package net.thedragonskull.vapemod.item.custom.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.thedragonskull.vapemod.item.custom.DisposableVape;

import java.util.List;

public class DisposableVapeRenderer extends BlockEntityWithoutLevelRenderer {

    public DisposableVapeRenderer(BlockEntityRenderDispatcher pBlockEntityRenderDispatcher, EntityModelSet pEntityModelSet) {
        super(pBlockEntityRenderDispatcher, pEntityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                             MultiBufferSource buffer, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getItemRenderer().getItemModelShaper().getItemModel(stack);

        float r = 1f, g = 1f, b = 1f;

        if (stack.getItem() instanceof DisposableVape vape) {
            int color = vape.getColor().getTextColor();
            r = (color >> 16 & 0xFF) / 255f;
            g = (color >> 8 & 0xFF) / 255f;
            b = (color & 0xFF) / 255f;
        }

        PoseStack.Pose pose = poseStack.last();

        boolean cull = true; // normalmente true para items
        var renderTypes = model.getRenderTypes(stack, cull);

        for (RenderType renderType : renderTypes) {
            VertexConsumer consumer = buffer.getBuffer(renderType);

            for (Direction direction : Direction.values()) {
                renderColoredQuads(model.getQuads(null, direction, mc.level.random), consumer, pose, light, overlay, r, g, b);
            }

            renderColoredQuads(model.getQuads(null, null, mc.level.random), consumer, pose, light, overlay, r, g, b);
        }
    }

    private void renderColoredQuads(List<BakedQuad> quads, VertexConsumer buffer, PoseStack.Pose pose,
                                    int light, int overlay, float r, float g, float b) {
        for (BakedQuad quad : quads) {
            buffer.putBulkData(pose, quad, r, g, b, 1.0f, light, overlay, true);
        }
    }


}
