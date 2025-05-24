package net.thedragonskull.vapemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.util.ModTags;

import java.util.List;

public class VapeCatalogScreenBackup extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "textures/gui/vape_catalog_screen.png");
    private final List<ItemStack> vapeList;
    private int pageIndex = 0;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;

    private static final int GUI_WIDTH = 276;
    private static final int GUI_HEIGHT = 166;

    private ItemStack selectedVape = ItemStack.EMPTY;

    public VapeCatalogScreenBackup() {
        super(Component.literal("Vape Catalog"));
        this.vapeList = generateVapeListForPage(pageIndex);
    }

    @Override
    protected void init() {
        super.init();

        int y = GUI_HEIGHT + 16 + 2;
        for (int i = 0; i < 7; i++) {
            ItemStack vape = vapeList.get(i);
            ItemStack costA = new ItemStack(Items.DIAMOND, 45);
            ItemStack costB = ItemStack.EMPTY;

            this.addRenderableWidget(new VapeTradeButton(GUI_WIDTH + 5 , y, i, costA, costB, vape, (btn) -> {
                this.selectedVape = vape;
                attemptBuy(vape);
            }));

            y += 20;
        }
    }

    private void updateVapeList() {
        this.vapeList.clear();
        this.vapeList.addAll(generateVapeListForPage(pageIndex));
        this.clearWidgets();
        this.init();
    }

    private void attemptBuy(ItemStack vape) {
        // Aquí puedes hacer la comprobación de esmeraldas y enviar un paquete al servidor
        // para ejecutar la compra real.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);

        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;
        graphics.blit(BACKGROUND, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT, 512, 256);

        // Tooltips
        for (Renderable widget : this.renderables) {
            if (widget instanceof VapeTradeButton button) {
                button.renderToolTip(graphics, mouseX, mouseY, this.font);
            }
        }

        if (!this.selectedVape.isEmpty()) {
            int centerX = this.width - 100; // Posición derecha
            int centerY = this.height / 2;
            int scale = 200;

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();

            poseStack.translate(centerX, centerY, 100);
            poseStack.scale(scale, scale, scale);

            poseStack.mulPose(Axis.XP.rotationDegrees(180f));


            float rotation = (System.currentTimeMillis() % 3600L) / 10f;
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(25));

            RenderSystem.disableCull();

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    this.selectedVape,
                    ItemDisplayContext.GROUND,
                    15728880,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    graphics.bufferSource(),
                    null,
                    0
            );

            poseStack.popPose();
        }

    }

    private List<ItemStack> generateVapeListForPage(int pageIndex) {
        TagKey<Item> tag = switch (pageIndex) {
            case 0 -> ModTags.Items.VAPES;
            case 1 -> ModTags.Items.DISPOSABLE_VAPES;
            default -> ModTags.Items.VAPES;
        };

        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .map(ItemStack::new)
                .toList();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public class VapeTradeButton extends Button {
        private final ItemStack result;
        private final ItemStack costA;
        private final ItemStack costB;
        private final int index;

        public VapeTradeButton(int x, int y, int index, ItemStack costA, ItemStack costB, ItemStack result, OnPress onPress) {
            super(x, y, 88, 20, Component.empty(), onPress, DEFAULT_NARRATION);
            this.index = index;
            this.costA = costA;
            this.costB = costB;
            this.result = result;
        }

        public int getIndex() {
            return this.index;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

            // Button BG
            //graphics.blit(VapeCatalogScreen.BACKGROUND, this.getX(), this.getY(), 0, 166, this.width, this.height);

            // Render cost A
            graphics.renderItem(this.costA, this.getX() + 5, this.getY() + 2);
            graphics.renderItemDecorations(Minecraft.getInstance().font, this.costA, this.getX() + 5, this.getY() + 2);

            // Render cost B (if avaliable)
            if (!this.costB.isEmpty()) {
                graphics.renderItem(this.costB, this.getX() + 25, this.getY() + 2);
                graphics.renderItemDecorations(Minecraft.getInstance().font, this.costB, this.getX() + 25, this.getY() + 2);
            }

            // Render result
            graphics.renderItem(this.result, this.getX() + 65, this.getY() + 2);
            graphics.renderItemDecorations(Minecraft.getInstance().font, this.result, this.getX() + 65, this.getY() + 2);

            // Hover
            if (this.isHovered) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0x40FFFFFF);
            }
        }

        public void renderToolTip(GuiGraphics graphics, int mouseX, int mouseY, Font font) {
            if (this.isHovered) {
                if (mouseX < this.getX() + 20) {
                    graphics.renderTooltip(font, this.costA, mouseX, mouseY);
                } else if (mouseX < this.getX() + 50 && mouseX > this.getX() + 30 && !this.costB.isEmpty()) {
                    graphics.renderTooltip(font, this.costB, mouseX, mouseY);
                } else if (mouseX > this.getX() + 65) {
                    graphics.renderTooltip(font, this.result, mouseX, mouseY);
                }
            }
        }
    }


}