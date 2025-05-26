package net.thedragonskull.vapemod.item.custom;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.network.PacketDistributor;
import net.thedragonskull.vapemod.network.PacketHandler;
import net.thedragonskull.vapemod.network.S2CResistanceSoundPacket;
import net.thedragonskull.vapemod.network.S2CStopResistanceSoundPacket;
import net.thedragonskull.vapemod.network.S2CVapeParticlesPacket;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;
import net.thedragonskull.vapemod.sound.ModSounds;
import net.thedragonskull.vapemod.util.VapeUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static net.thedragonskull.vapemod.util.VapeUtil.toRoman;

public class DisposableVape extends Item implements IVape {
    private static final String MESSAGE_CANT_SMOKE_UNDERWATER = "message.vapemod.cant_smoke_underwater";
    private final DyeColor dyeColor;

    public DisposableVape(DyeColor dyeColor, Properties pProperties) {
        super(pProperties);
        this.dyeColor = dyeColor;
    }

    public DyeColor getColor() {
        return dyeColor;
    }

    @Override
    public ItemStack getDefaultInstance() {
        return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (PotionUtils.getPotion(item) == Potions.EMPTY) {
            return InteractionResultHolder.fail(item);
        }

        for (InteractionHand h : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(h);
            if (held.getItem() instanceof DisposableVape) {
                if (player.getCooldowns().isOnCooldown(held.getItem())) {
                    return InteractionResultHolder.fail(item);
                }
            }
        }

        player.startUsingItem(hand);

        if (!level.isClientSide) {
            PacketHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> player),
                    new S2CResistanceSoundPacket(player.getUUID())
            );
        } else {
            ClientSoundHandler.start(player);
        }

        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack item, int count) {

        if (!(livingEntity instanceof Player player)) return;

        if (player.isUnderWater()) {
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable(MESSAGE_CANT_SMOKE_UNDERWATER).withStyle(ChatFormatting.DARK_RED), true);
            return;
        }

        if (player.getTicksUsingItem() >= getUseDuration(item) - 1) {
            int maxDurability = item.getMaxDamage();

            for (MobEffectInstance effect : PotionUtils.getMobEffects(item)) {
                if (effect.getEffect().isInstantenous()) {
                    effect.getEffect().applyInstantenousEffect(player, player, player, effect.getAmplifier(), 1.0);
                } else {
                    int duration = (int)(effect.getDuration() / maxDurability);
                    int amplifier = effect.getAmplifier();
                    player.addEffect(new MobEffectInstance(effect.getEffect(), duration * 2, amplifier, false, true));
                }
            }

            VapeUtil.applyCooldownToVapes(player, 100);

            int color = 0xFFFFFF;
            Potion contents = PotionUtils.getPotion(item);
            if (contents != null && contents.equals(Potions.WATER)) {
                color = PotionUtils.getColor(item);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
                        new S2CVapeParticlesPacket(player.getUUID(), color)
                );
            } else {
                VapeUtil.smokeParticles(player);
            }

            if (!level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.VAPE_RESISTANCE_END.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.SMOKING_BREATHE_OUT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }

        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        if (!level.isClientSide) {
            PacketHandler.INSTANCE.send(
                    PacketDistributor.TRACKING_ENTITY.with(() -> player),
                    new S2CStopResistanceSoundPacket(player.getUUID())
            );
        } else {
            ClientSoundHandler.stop(player);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                PacketHandler.INSTANCE.send(
                        PacketDistributor.TRACKING_ENTITY.with(() -> player),
                        new S2CStopResistanceSoundPacket(player.getUUID())
                );
            } else {
                ClientSoundHandler.stop(player);
            }

            stack.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(player.getUsedItemHand()));
        }

        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);


        int currentDurability = stack.getMaxDamage() - stack.getDamageValue();
        int maxDurability = stack.getMaxDamage();
        int percent = Math.round(((float) currentDurability / maxDurability) * 100);

        float ratio = (float) currentDurability / maxDurability;
        int red = (int) ((1.0f - ratio) * 255);
        int green = (int) (ratio * 255);
        int color = (red << 16) | (green << 8); // RGB

        MutableComponent label = Component.literal("Capacity: ").withStyle(ChatFormatting.DARK_AQUA);
        MutableComponent value = Component.literal(percent + "%").withStyle(style -> style.withColor(color));

        tooltip.add(label.append(value));

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
        if (!effects.isEmpty()) {

            for (MobEffectInstance effect : effects) {
                MutableComponent effectName = Component.translatable(effect.getDescriptionId());

                if (effect.getAmplifier() > 0) {
                    effectName.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
                }

                int adjustedDuration = (int)(effect.getDuration() / stack.getMaxDamage());
                if (adjustedDuration > 20) {
                    String time = formatDuration(adjustedDuration * 2);
                    effectName.append(" (").append(Component.literal(time)).append(")");
                }

                tooltip.add(effectName.withStyle(ChatFormatting.BLUE));
            }
        } else {
            tooltip.add(Component.literal("Effect: ???").withStyle(ChatFormatting.BLUE));
        }

    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return minutes + ":" + String.format("%02d", seconds);
    }

    @Override
    public String getDescriptionId(ItemStack pStack) {
        return PotionUtils.getPotion(pStack).getName(this.getDescriptionId() + ".effect.");
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = Component.translatable(this.getDescriptionId());

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(stack);
        if (!effects.isEmpty()) {
            MobEffectInstance effect = effects.get(0);
            Component effectName = Component.translatable(effect.getDescriptionId());
            int level = effect.getAmplifier();

            return VapeUtil.formatEffectName(baseName, effectName, level);
        }

        return baseName;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
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
                        model.leftArm.xRot = (float) Math.toRadians(-90);
                        model.leftArm.yRot = (float) Math.toRadians(30);
                        model.leftArm.zRot = (float) Math.toRadians(-20);
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
