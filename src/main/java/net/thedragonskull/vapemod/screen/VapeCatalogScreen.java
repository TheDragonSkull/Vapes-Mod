package net.thedragonskull.vapemod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.thedragonskull.vapemod.VapeMod;
import net.thedragonskull.vapemod.util.ModTags;
import net.thedragonskull.vapemod.util.VapeCatalogUtil;

import java.util.ArrayList;
import java.util.List;

import static net.thedragonskull.vapemod.util.VapeCatalogUtil.hasEnoughCurrency;

public class VapeCatalogScreen extends Screen { // TODO: CLEAN COMMENTS AND CODE
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(VapeMod.MOD_ID, "textures/gui/vape_catalog_screen.png");
    private final VapeCatalogScreen.VapeTradeButton[] tradeOfferButtons = new VapeCatalogScreen.VapeTradeButton[7];
    private List<ItemStack> vapeList = new ArrayList<>();
    private List<ItemStack> fullVapeList = new ArrayList<>();
    private int pageIndex = 0;
    private final int BUTTON_WIDTH = 60;
    private final int BUTTON_HEIGHT = 20;
    private Button buyButton;

    ItemStack costA = VapeTradeButton.getCostA(); // o como lo tengas definido
    ItemStack costB = VapeTradeButton.getCostB();

    private static final int GUI_WIDTH = 276;
    private static final int GUI_HEIGHT = 166;

    private ItemStack selectedVape = ItemStack.EMPTY;
    int scrollOff;
    private boolean isDragging = false;

    public VapeCatalogScreen() {
        super(Component.literal("Vape Catalog"));
        this.fullVapeList = generateFullVapeList();
        this.vapeList = paginateVapeList();
    }

    @Override
    protected void init() {
        super.init();

        int width = (this.width - GUI_WIDTH) / 2;
        int height = (this.height - GUI_HEIGHT) / 2;
        int yPos = height + 16 + 2;

        for (int i = 0; i < 7; i++) {
            ItemStack vape = vapeList.get(i);
            ItemStack costA = new ItemStack(Items.DIAMOND, 45);
            ItemStack costB = ItemStack.EMPTY;

            this.tradeOfferButtons[i] = this.addRenderableWidget(new VapeTradeButton(width + 5 , yPos, i, costA, costB, vape, (btn) -> {
                if (btn instanceof VapeCatalogScreen.VapeTradeButton) {
                    this.selectedVape = vape;
                    attemptBuy(vape);
                }
            }));

            yPos += 20;
        }

        int centerX = (this.width) / 2;
        int centerY = height + 16 + 2;
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();

        //QVape Pen V2 tab
        this.addRenderableWidget(new VapeCatalogUtil.TabAndBuyButton(centerX - 31, centerY, 100, 20, Component.literal("QVape Pen V2"), btn -> {
            //TODO: change tab
        }, SoundEvents.BOOK_PAGE_TURN));

        centerY += 25;

        //QVape D Pod tab
        this.addRenderableWidget(new VapeCatalogUtil.TabAndBuyButton(centerX - 31, centerY, 100, 20, Component.literal("QVape D Pod"), btn -> {
            //TODO: change tab
        }, SoundEvents.BOOK_PAGE_TURN));

        // Buy button
        this.buyButton = new VapeCatalogUtil.TabAndBuyButton(centerX + 75, height + 83, 54, 20, Component.literal("$ Buy $"), btn -> {
            //todo: comprar
        }, SoundEvents.ANVIL_LAND); //TODO: register machine/cash sound

        buyButton.active = !this.selectedVape.isEmpty() && hasEnoughCurrency(this.minecraft.player, costA, costB);

        this.addRenderableWidget(this.buyButton);
    }

    private void updateVapeList() {
        // Genera todos los ítems disponibles
        TagKey<Item> tag = ModTags.Items.DISPOSABLE_VAPES;
        List<ItemStack> all = ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .map(ItemStack::new)
                .toList();

        // Calcula los ítems visibles según el scroll actual
        int from = Mth.clamp(this.scrollOff, 0, Math.max(0, all.size() - 7));
        int to = Math.min(from + 7, all.size());
        this.vapeList = new ArrayList<>(all); // guarda la lista entera por si se necesita más adelante

        // Actualiza los botones existentes sin recrearlos
        for (int i = 0; i < 7; i++) {
            if (i + from < all.size()) {
                ItemStack vape = all.get(i + from);
                ItemStack costA = new ItemStack(Items.DIAMOND, 45); // o ajusta por ítem si quieres

                VapeTradeButton button = this.tradeOfferButtons[i];
                button.visible = true;
                button.active = true;
                button.setItem(vape, costA, ItemStack.EMPTY);
            } else {
                // Oculta botones sobrantes
                VapeTradeButton button = this.tradeOfferButtons[i];
                button.visible = false;
                button.active = false;
            }
        }
    }

    private void attemptBuy(ItemStack vape) {
        // Aquí puedes hacer la comprobación de esmeraldas y enviar un paquete al servidor
        // para ejecutar la compra real.
    }

    @Override
    public void tick() {
        super.tick();
        boolean hasSelection = !this.selectedVape.isEmpty();
        boolean hasCurrency = hasSelection && hasEnoughCurrency(this.minecraft.player, costA, costB);
        this.buyButton.active = hasSelection && hasCurrency;
    }

    private void renderScroller(GuiGraphics pGuiGraphics, int pPosX, int pPosY, List<ItemStack> vapeList) {
        int total = this.fullVapeList.size();
        int visible = 7;
        int maxScroll = Math.max(0, total - visible);

        if (maxScroll > 0) {
            int thumbHeight = 27; // Alto del pulgar
            int trackHeight = 140;

            // Relación de scroll
            float scrollRatio = (float) this.scrollOff / maxScroll;
            int thumbY = (int) (scrollRatio * (trackHeight - thumbHeight));

            // Usa u=199, v=0 para el scroller activo
            pGuiGraphics.blit(BACKGROUND, pPosX + 94, pPosY + 18 + thumbY, 0, 0.0F, 199.0F, 6, 27, 512, 256);
        } else {
            // Usa u=199, v=6 para el scroller inactivo
            pGuiGraphics.blit(BACKGROUND, pPosX + 94, pPosY + 18, 0, 6.0F, 199.0F, 6, 27, 512, 256);
        }
    }

    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int fontWidth = this.font.width("Vape List");
        int labelX = (this.width - GUI_WIDTH) / 2 + 48 - fontWidth / 2;
        int labelY = (this.height - GUI_HEIGHT) / 2 + 6;

        pGuiGraphics.drawString(this.font, "Vape List", labelX, labelY, 4210752, false);

        int vapeModelsWidth = this.font.width("Vape Models");
        int centerX = (this.width / 2) + 50;
        pGuiGraphics.drawString(this.font, "Vape Models", centerX - vapeModelsWidth, labelY, 4210752, false);

        int vape3DWidth = this.font.width("3D View");
        int displayCenter = (this.width / 2) + 75 + (54 - vape3DWidth) / 2;
        pGuiGraphics.drawString(this.font, "3D View", displayCenter, labelY, 4210752, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);

        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        // Background
        graphics.blit(BACKGROUND, x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT, 512, 256);

        this.renderScroller(graphics, x, y, this.vapeList);
        this.renderLabels(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTicks);

        // Tooltips
        for (Renderable widget : this.renderables) {
            if (widget instanceof VapeTradeButton button) {
                button.renderToolTip(graphics, mouseX, mouseY, this.font);
            }
        }

        if (this.buyButton != null && this.buyButton.isHoveredOrFocused()) {
            Component tooltip = null;

            if (this.selectedVape.isEmpty()) {
                if (!this.buyButton.active) {
                    tooltip = Component.literal("No vape selected").withStyle(ChatFormatting.RED);
                }
            } else {
                ItemStack costA = this.costA;
                ItemStack costB = this.costB;

                if (!hasEnoughCurrency(this.minecraft.player, costA, costB)) {
                    tooltip = Component.literal("Not enough currency").withStyle(ChatFormatting.RED);
                }
            }

            if (tooltip != null) {
                graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }

        // 3D item TODO: centrar en cuadrado?
        if (!this.selectedVape.isEmpty()) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            int scale = 100;

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();

            poseStack.translate((centerX + 100) + 1.5F, centerY - 30, 100);
            poseStack.scale(scale, scale, scale);

            poseStack.mulPose(Axis.XP.rotationDegrees(180f));


            float rotation = (System.currentTimeMillis() % 3600L) / 10f;
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
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

    private List<ItemStack> generateFullVapeList() {
        TagKey<Item> tag = ModTags.Items.DISPOSABLE_VAPES;

        return ForgeRegistries.ITEMS.getValues().stream()
                .filter(item -> item.builtInRegistryHolder().is(tag))
                .map(ItemStack::new)
                .toList();
    }

    private List<ItemStack> paginateVapeList() {
        int from = Mth.clamp(this.scrollOff, 0, Math.max(0, fullVapeList.size() - 7));
        int to = Math.min(from + 7, fullVapeList.size());
        return new ArrayList<>(fullVapeList.subList(from, to));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean canScroll(int pNumOffers) {
        return pNumOffers > 7;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int total = fullVapeList.size();

        if (this.canScroll(total)) {
            int maxScroll = total - 7;
            this.scrollOff = Mth.clamp((int)(this.scrollOff - delta), 0, maxScroll);
            this.vapeList = paginateVapeList();
            this.clearWidgets();
            this.init();
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        int x = (this.width - GUI_WIDTH) / 2;
        int y = (this.height - GUI_HEIGHT) / 2;

        if (this.canScroll(this.fullVapeList.size()) &&
                mouseX > x + 94 && mouseX < x + 94 + 6 &&
                mouseY > y + 18 && mouseY <= y + 18 + 139 + 1) {
            this.isDragging = true;
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isDragging) {
            int y = (this.height - GUI_HEIGHT) / 2;
            int top = y + 18;
            int bottom = top + 139;

            int maxScroll = Math.max(0, fullVapeList.size() - 7);
            float scrollProgress = ((float)mouseY - top - 13.5F) / ((bottom - top) - 27.0F);
            scrollProgress = Mth.clamp(scrollProgress, 0.0F, 1.0F);

            this.scrollOff = Mth.clamp((int)(scrollProgress * maxScroll + 0.5F), 0, maxScroll);
            this.vapeList = paginateVapeList();
            this.clearWidgets();
            this.init();
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @OnlyIn(Dist.CLIENT)
    public class VapeTradeButton extends Button {
        private ItemStack result;
        private static ItemStack costA;
        private static ItemStack costB;
        private final int index;

        public VapeTradeButton(int x, int y, int index, ItemStack costA, ItemStack costB, ItemStack result, OnPress onPress) {
            super(x, y, 88, 20, Component.empty(), onPress, DEFAULT_NARRATION);
            this.index = index;
            this.costA = costA;
            this.costB = costB;
            this.result = result;
        }

        public ItemStack getResult() {
            return result;
        }

        public static ItemStack getCostA() {
            return costA;
        }

        public static ItemStack getCostB() {
            return costB;
        }

        public int getIndex() {
            return this.index;
        }

        public void setItem(ItemStack result, ItemStack costA, ItemStack costB) {
            this.result = result;
            this.costA = costA;
            this.costB = costB;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);

            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();

            // Posiciones relativas al botón
            int x = this.getX();
            int y = this.getY() + 1;

            // Render costA
            graphics.renderItem(this.costA, x + 2, y);
            graphics.renderItemDecorations(Minecraft.getInstance().font, this.costA, x + 2, y);

            // Render costB (solo si no está vacío)
            if (!this.costB.isEmpty()) {
                graphics.renderItem(this.costB, x + 34, y);
                graphics.renderItemDecorations(Minecraft.getInstance().font, this.costB, x + 34, y);
            }

            // Render result
            graphics.renderItem(this.result, x + 68, y + 1);
            graphics.renderItemDecorations(Minecraft.getInstance().font, this.result, x + 68, y);

            poseStack.popPose();

            this.renderButtonArrows(graphics, x, y);

            int arrowX = x + 5 + 35;
            int arrowY = y + 4;
            int arrowWidth = 10;
            int arrowHeight = 9;
            boolean enough = hasEnoughCurrency(Minecraft.getInstance().player, costA, costB);

            if (!enough) {
                if (mouseX >= arrowX && mouseX < arrowX + arrowWidth &&
                        mouseY >= arrowY && mouseY < arrowY + arrowHeight) {
                    graphics.renderTooltip(font, Component.literal("Not enough currency").withStyle(ChatFormatting.RED), mouseX, mouseY);
                }
            }
        }

        private void renderButtonArrows(GuiGraphics pGuiGraphics, int pPosX, int pPosY) {
            boolean enough = hasEnoughCurrency(Minecraft.getInstance().player, costA, costB);
            RenderSystem.enableBlend();

            if (enough) {
                pGuiGraphics.blit(BACKGROUND, pPosX + 5 + 35, pPosY + 4, 0, 15.0F, 171.0F, 10, 9, 512, 256);
            } else {
                pGuiGraphics.blit(BACKGROUND, pPosX + 5 + 35, pPosY + 4, 0, 25.0F, 171.0F, 10, 9, 512, 256);
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