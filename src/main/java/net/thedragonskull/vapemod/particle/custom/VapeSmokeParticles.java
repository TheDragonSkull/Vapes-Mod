package net.thedragonskull.vapemod.particle.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class VapeSmokeParticles extends TextureSheetParticle {
    private boolean hasColorGradient = false;
    private static final int[] GRADIENT_START = {220, 164, 224};
    private static final int[] GRADIENT_END = {130, 217, 192};

    protected VapeSmokeParticles(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet spriteSet,
                                 double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);

        this.scale(3.0F);
        this.setSize(0.25F, 0.25F);
        this.lifetime = this.random.nextInt(50) + 80;

        this.gravity = 3.0E-6F;
        this.xd = pXSpeed;
        this.yd = pYSpeed + (double)(this.random.nextFloat() / 500.0F);
        this.zd = pZSpeed;

        this.setAlpha(0.65F);
        this.pickSprite(spriteSet);
    }

    public void setRGBColor(int red, int green, int blue) {
        this.rCol = red / 255.0f;
        this.gCol = green / 255.0f;
        this.bCol = blue / 255.0f;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;

        if (age++ < lifetime && alpha > 0.0F) {
            xd += random.nextFloat() / 5000.0F * (random.nextBoolean() ? 1 : -1);
            zd += random.nextFloat() / 5000.0F * (random.nextBoolean() ? 1 : -1);
            yd -= gravity;
            move(xd, yd, zd);

            if (age >= lifetime - 60 && alpha > 0.01F) {
                alpha -= 0.015F;
            }
        } else {
            remove();
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
                                       double x, double y, double z, double dx, double dy, double dz) {
            VapeSmokeParticles particle = new VapeSmokeParticles(level, x, y, z, sprites, 0, 0, 0);

            int red = (int)(dx * 255);
            int green = (int)(dy * 255);
            int blue = (int)(dz * 255);

            particle.setRGBColor(red, green, blue);

            return particle;
        }

    }
}
