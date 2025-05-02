package net.thedragonskull.vapemod.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.client.IArmPoseTransformer;

public class VapePoseParams {
    public static final EnumProxy<HumanoidModel.ArmPose> VAPING_ARM_POSE = new EnumProxy<>(
            HumanoidModel.ArmPose.class,
            false,
            (IArmPoseTransformer) VapePoseParams::applyVapePose
    );

    private static void applyVapePose(HumanoidModel<?> model, LivingEntity entity, HumanoidArm arm) {
        ModelPart armPart = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;

        if (arm == HumanoidArm.RIGHT) {
            armPart.xRot = (float) Math.toRadians(-90);
            armPart.yRot = (float) Math.toRadians(-30);
            armPart.zRot = (float) Math.toRadians(20);
        } else {
            armPart.xRot = (float) Math.toRadians(-90);
            armPart.yRot = (float) Math.toRadians(30);
            armPart.zRot = (float) Math.toRadians(-20);
        }
    }
}
