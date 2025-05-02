package net.thedragonskull.vapemod.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thedragonskull.vapemod.capability.VapeEnergyContainer;
import net.thedragonskull.vapemod.component.ModDataComponentTypes;
import net.thedragonskull.vapemod.network.S2CResistanceSoundPacket;
import net.thedragonskull.vapemod.network.S2CStopResistanceSoundPacket;
import net.thedragonskull.vapemod.particle.ModParticles;
import net.thedragonskull.vapemod.sound.ClientSoundHandler;
import net.thedragonskull.vapemod.sound.ModSounds;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.joml.Math.clamp;

public class Vape extends Item implements VapeEnergyContainer {
    private static final String MESSAGE_CANT_SMOKE_UNDERWATER = "message.vapemod.cant_smoke_underwater";

    public Vape(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        stack.set(DataComponents.POTION_CONTENTS, PotionContents.EMPTY.withPotion(Potions.WATER));

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        var cap = stack.getCapability(Capabilities.EnergyStorage.ITEM, null);  //todo Optional?
        if (cap != null) {
            int energy = cap.getEnergyStored();
            int max = cap.getMaxEnergyStored();
            int percent = Math.round(((float) energy / max) * 100);

            float ratio = (float) energy / max;
            int red = (int) ((1.0f - ratio) * 255);
            int green = (int) (ratio * 255);
            int color = (red << 16) | (green << 8);

            MutableComponent label = Component.literal("Capacity: ").withStyle(ChatFormatting.DARK_AQUA);
            MutableComponent value = Component.literal(percent + "%").withStyle(style -> style.withColor(color));

            tooltip.add(label.append(value));

            PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
            if (contents != null && !stack.get(DataComponents.POTION_CONTENTS).potion().get().value().getEffects().isEmpty()) {
                for (MobEffectInstance effect : stack.get(DataComponents.POTION_CONTENTS).potion().get().value().getEffects()) {
                    MutableComponent effectName = Component.translatable(effect.getDescriptionId());

                    if (effect.getAmplifier() > 0) {
                        effectName.append(" ").append(Component.translatable("potion.potency." + effect.getAmplifier()));
                    }

                    int adjustedDuration = (int)(effect.getDuration() / (float) max);
                    if (adjustedDuration > 20) {
                        String time = formatDuration(adjustedDuration);
                        effectName.append(" (").append(Component.literal(time)).append(")");
                    }

                    tooltip.add(effectName.withStyle(ChatFormatting.BLUE));
                }
            } else {
                tooltip.add(Component.literal("No Effects").withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return minutes + ":" + String.format("%02d", seconds);
    }

    public String getDescriptionId(ItemStack pStack) {
        return Potion.getName(pStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion(), this.getDescriptionId() + ".effect.");
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = Component.translatable(this.getDescriptionId());

        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents != null) {
            List<MobEffectInstance> effects = stack.get(DataComponents.POTION_CONTENTS).potion().get().value().getEffects();
            if (!effects.isEmpty()) {
                MobEffectInstance effect = effects.get(0);
                Component effectName = Component.translatable(effect.getDescriptionId());
                return Component.literal("").append(baseName).append(" (").append(effectName).append(")");
            }
        }

        return baseName;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        Minecraft minecraft = Minecraft.getInstance();
        var cap = item.getCapability(Capabilities.EnergyStorage.ITEM, null);  //todo Optional?
        boolean hasEnergy = cap != null && cap.getEnergyStored() > 0;

        if (!hasEnergy) {
            if (level.isClientSide) {
                player.displayClientMessage(Component.literal("¡Empty tank, refill!").withStyle(ChatFormatting.DARK_RED), true);
            }
            return InteractionResultHolder.fail(item);
        }

        if (hand == InteractionHand.MAIN_HAND || hasEnergy) {
            player.startUsingItem(hand);
        } else {
            player.stopUsingItem();
            return InteractionResultHolder.fail(item);
        }

        if (!level.isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(player, new S2CResistanceSoundPacket(player.getUUID())); //todo: funciona?
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

        if (player.getTicksUsingItem() >= getUseDuration(item, livingEntity) - 1) {
            var cap = item.getCapability(Capabilities.EnergyStorage.ITEM, null);  //todo Optional?

            if (cap != null) {
                int energy = cap.getEnergyStored();

                if (energy > 0) {
                    for (MobEffectInstance effect : item.get(DataComponents.POTION_CONTENTS).potion().get().value().getEffects()) {
                        if (effect.getEffect().value().isInstantenous()) {
                            effect.getEffect().value().applyInstantenousEffect(player, player, player, effect.getAmplifier(), 1.0);
                        } else {
                            int duration = (int)(effect.getDuration() / cap.getMaxEnergyStored());
                            int amplifier = effect.getAmplifier();
                            player.addEffect(new MobEffectInstance(effect.getEffect(), duration, amplifier, false, true));
                        }
                    }

                    player.getCooldowns().addCooldown(this, 100);

                    if (!player.getAbilities().instabuild) {
                        cap.extractEnergy(1, false);
                    }

                    if (level.isClientSide) {
                        smokeParticles(player);
                    } else {
                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.VAPE_RESISTANCE_END.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                ModSounds.SMOKING_BREATHE_OUT.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }

    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;

        if (!level.isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntity(player, new S2CStopResistanceSoundPacket(player.getUUID())); //todo: funciona?

        } else {
            ClientSoundHandler.stop(player);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (!level.isClientSide) {
                PacketDistributor.sendToPlayersTrackingEntity(player, new S2CStopResistanceSoundPacket(player.getUUID())); //todo: funciona?
            } else {
                ClientSoundHandler.stop(player);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    public void smokeParticles(Player player) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Minecraft.getInstance().execute(() -> {

                    ItemStack stack = player.getMainHandItem();
                    int red = 255, green = 255, blue = 255;

                    if (stack.getItem() instanceof Vape) {
                        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
                        if (!(contents.potion().get() == Potions.WATER)) {
                            int potionColor = stack.get(DataComponents.POTION_CONTENTS).getColor();
                            red = (potionColor >> 16) & 0xFF;
                            green = (potionColor >> 8) & 0xFF;
                            blue = potionColor & 0xFF;
                        }
                    }

                    for (int i = 0; i < 10; i++) {
                        double distance = -0.5D;
                        double horizontalAngle = Math.toRadians(player.getYRot());
                        double verticalAngle = Math.toRadians(player.getXRot());
                        double xOffset = distance * Math.sin(horizontalAngle) * Math.cos(verticalAngle);
                        double yOffset = distance * Math.sin(verticalAngle);
                        double zOffset = -distance * Math.cos(horizontalAngle) * Math.cos(verticalAngle);
                        double x = player.getX() + xOffset;
                        double y = player.getEyeY() + yOffset;
                        double z = player.getZ() + zOffset;

                        player.level().addParticle(ModParticles.VAPE_SMOKE_PARTICLES.get(),
                                x, y, z,
                                red / 255.0D, green / 255.0D, blue / 255.0D
                        );
                    }
                });
            }
        }, 300);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 32;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);

        if (!level.isClientSide) {
            level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    private void setEnergyStored(ItemStack container, int value) {
        container.set(ModDataComponentTypes.ENERGY.get(), clamp(value, 0, getCapacity(container)));
    }

    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        if (1 == 0) return 0;
        int energyStored = getEnergy(container);
        int energyExtracted = Math.min(energyStored, Math.min(1, maxExtract));
        if (!simulate) setEnergyStored(container, energyStored - energyExtracted);
        return energyExtracted;
    }

    @Override
    public int getEnergy(ItemStack container) {
        Integer energy = container.get(ModDataComponentTypes.ENERGY.get());

        if (energy == null) {
            energy = 0;
            container.set(ModDataComponentTypes.ENERGY.get(), energy);
        }

        return energy;
    }

    @Override
    public int getCapacity(ItemStack container) {
        return 15;
    }
}
