package net.thedragonskull.vapemod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class DisposableVape extends Item {
    private final DyeColor dyeColor;

    public DisposableVape(DyeColor dyeColor, Properties pProperties) {
        super(pProperties);
        this.dyeColor = dyeColor;
    }

    public DyeColor getColor() {
        return dyeColor;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private static final HumanoidModel.ArmPose VAPING = HumanoidModel.ArmPose.create("vaping", false, (model, entity, arm) -> {
                if (!entity.isUnderWater()) {
                    if (arm == HumanoidArm.RIGHT) {
                        model.rightArm.xRot = (float) Math.toRadians(-90);
                        model.rightArm.yRot = (float) Math.toRadians(-30);
                        model.rightArm.zRot = (float) Math.toRadians(20);
                    } else {
                        model.leftArm.xRot = (float) Math.toRadians(-30);
                        model.leftArm.yRot = (float) Math.toRadians(-10);
                        model.leftArm.zRot = (float) Math.toRadians(-15);
                    }
                }
            });

            // Third Person
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (!itemStack.isEmpty()) {
                    if (entityLiving.getUsedItemHand() == hand && entityLiving.getUseItemRemainingTicks() > 0) {
                        return VAPING;
                    }
                }

                return HumanoidModel.ArmPose.EMPTY;
            }

            // First Person
            @Override
            public boolean applyForgeHandTransform(PoseStack pPoseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                int i = arm == HumanoidArm.RIGHT ? 1 : -1;
                pPoseStack.translate(i * 0.5F, -0.52F, -0.72F);

                if (player.getUseItem() == itemInHand && player.isUsingItem() && !player.isUnderWater()) {
                    float f = (float)player.getUseItemRemainingTicks() - partialTick + 1.0F;
                    float f1 = f / (float)itemInHand.getUseDuration();
                    float f3 = 1.0F - (float)Math.pow((double)f1, 27.0D);

                    pPoseStack.translate(i * -0.4F, 0.1F + equipProcess, 0.0F);

                    pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * f3 * 45.0F));
                    pPoseStack.mulPose(Axis.XP.rotationDegrees(f3 * 10.0F));
                    pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)i * f3 * 30.0F));
                } else {
                    pPoseStack.translate(i * -0.0F, 0.0F + equipProcess * -0.6F, -0.0F);

                    float f = Mth.sin(swingProcess * swingProcess * (float)Math.PI);
                    pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * (45.0F + f * -20.0F)));
                    float f1 = Mth.sin(Mth.sqrt(swingProcess) * (float)Math.PI);
                    pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)i * f1 * -20.0F));
                    pPoseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
                    pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * -5.0F));
                }
                return true;
            }
        });
    }

}
