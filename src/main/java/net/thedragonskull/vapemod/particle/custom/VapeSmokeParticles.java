package net.thedragonskull.vapemod.particle.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VapeSmokeParticles extends TextureSheetParticle {
    private boolean hasColorGradient = false;
    private static final int[] GRADIENT_START = {185, 109, 191};
    private static final int[] GRADIENT_END = {72, 189, 155};

    protected VapeSmokeParticles(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet spriteSet,
                                 double pXSpeed, double pYSpeed, double pZSpeed, Item sourceItem) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);

        this.scale(3.0F);
        this.setSize(0.25F, 0.25F);
        this.lifetime = this.random.nextInt(50) + 80;

        this.gravity = 3.0E-6F;
        this.xd = pXSpeed;
        this.yd = pYSpeed + (double)(this.random.nextFloat() / 500.0F);
        this.zd = pZSpeed;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {

            if (sourceItem == Items.DIAMOND) {
                this.setHasColorGradient(true);
                this.setRGBColor(185 / 255f, 109 / 255f, 191 / 255f);
            } else {
                int[] rgb = SmokeParticleColorManager.getColorForItem(sourceItem);
                this.setRGBColor(rgb[0] / 255.0f, rgb[1] / 255.0f, rgb[2] / 255.0f);
            }

        }
        this.setAlpha(0.65F);

        this.pickSprite(spriteSet);
    }

    public void setRGBColor(float red, float green, float blue) {
        this.rCol = red;
        this.gCol = green;
        this.bCol = blue;
    }

    public void setHasColorGradient(boolean hasGradient) {
        this.hasColorGradient = hasGradient;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.xd += this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1);
            this.zd += this.random.nextFloat() / 5000.0F * (this.random.nextBoolean() ? 1 : -1);
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);

            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }

            if (this.hasColorGradient) {
                float ageFraction = (float) this.age / (float) this.lifetime;

                float red = GRADIENT_START[0] + ageFraction * (GRADIENT_END[0] - GRADIENT_START[0]);
                float green = GRADIENT_START[1] + ageFraction * (GRADIENT_END[1] - GRADIENT_START[1]);
                float blue = GRADIENT_START[2] + ageFraction * (GRADIENT_END[2] - GRADIENT_START[2]);

                setRGBColor((int) red, (int) green, (int) blue);
            }

        } else {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            LocalPlayer player = Minecraft.getInstance().player;
            Item item = Items.AIR;

            if (player != null) {
                item = player.getMainHandItem().getItem();
            }

            return new VapeSmokeParticles(level, x, y, z, sprites, dx, dy, dz, item);
        }

    }
}
