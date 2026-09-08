package com.automods.tieredironchests.client;

import com.automods.tieredironchests.ChestTier;
import com.automods.tieredironchests.TieredChestMenu;
import com.automods.tieredironchests.TieredIronChests;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Chest screen for any column x row layout, composed from the vanilla {@code generic_54.png} nine-slice at runtime:
 * header, one 18 px slot cell per grid position, and the 176 px wide player-inventory block centred under the grid
 * with the side gaps filled from plain texture samples. No GUI textures of our own, so resource packs that restyle
 * the vanilla chest restyle these too.
 */
public class TieredChestScreen extends AbstractContainerScreen<TieredChestMenu> {
    private static final ResourceLocation TEXTURE = TieredIronChests.vanillaId("textures/gui/container/generic_54.png");
    private static final int TEXTURE_SIZE = 256;
    private static final int SLOT = TieredChestMenu.SLOT_SIZE;
    private static final int EDGE = 7;
    private static final int HEADER_HEIGHT = 17;
    private static final int ROW_V = 17;
    private static final int RIGHT_EDGE_U = TieredChestMenu.VANILLA_WIDTH - EDGE;
    private static final int FOOTER_V = 126;
    private static final int FOOTER_HEIGHT = 96;
    private static final int FOOTER_BORDER = 7;
    /** A plain background texel (header area) and the first column of the footer's bottom border. */
    private static final float PLAIN_U = 8.0F;
    private static final float PLAIN_V = 130.0F;
    private static final float BORDER_V = FOOTER_V + FOOTER_HEIGHT - FOOTER_BORDER;

    private final int columns;
    private final int rows;

    public TieredChestScreen(TieredChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        ChestTier tier = menu.tier();
        this.columns = tier.columns();
        this.rows = tier.rows();
        this.imageWidth = TieredChestMenu.imageWidth(tier);
        this.imageHeight = TieredChestMenu.imageHeight(tier);
        this.inventoryLabelX = TieredChestMenu.playerInventoryLeft(tier);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        int width = this.imageWidth;

        // Header: corners + a 1 px column stretched across.
        guiGraphics.blit(TEXTURE, left, top, 0, 0, EDGE, HEADER_HEIGHT);
        guiGraphics.blit(TEXTURE, left + EDGE, top, width - 2 * EDGE, HEADER_HEIGHT, PLAIN_U, 0.0F, 1, HEADER_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(TEXTURE, left + width - EDGE, top, RIGHT_EDGE_U, 0, EDGE, HEADER_HEIGHT);

        // Slot rows: edge, one vanilla slot cell per column, edge.
        for (int row = 0; row < rows; row++) {
            int y = top + HEADER_HEIGHT + row * SLOT;
            guiGraphics.blit(TEXTURE, left, y, 0, ROW_V, EDGE, SLOT);
            for (int column = 0; column < columns; column++) {
                guiGraphics.blit(TEXTURE, left + EDGE + column * SLOT, y, EDGE, ROW_V, SLOT, SLOT);
            }
            guiGraphics.blit(TEXTURE, left + width - EDGE, y, RIGHT_EDGE_U, ROW_V, EDGE, SLOT);
        }

        // Footer: player inventory block centred, edges at the outer corners, gaps filled with plain background.
        int footerY = top + HEADER_HEIGHT + rows * SLOT;
        int pad = (width - TieredChestMenu.VANILLA_WIDTH) / 2;
        guiGraphics.blit(TEXTURE, left, footerY, 0, FOOTER_V, EDGE, FOOTER_HEIGHT);
        guiGraphics.blit(TEXTURE, left + pad + EDGE, footerY, EDGE, FOOTER_V, TieredChestMenu.VANILLA_WIDTH - 2 * EDGE, FOOTER_HEIGHT);
        guiGraphics.blit(TEXTURE, left + width - EDGE, footerY, RIGHT_EDGE_U, FOOTER_V, EDGE, FOOTER_HEIGHT);
        if (pad > 0) {
            fillFooterGap(guiGraphics, left + EDGE, footerY, pad);
            fillFooterGap(guiGraphics, left + pad + TieredChestMenu.VANILLA_WIDTH - EDGE, footerY, pad);
        }
    }

    private static void fillFooterGap(GuiGraphics guiGraphics, int x, int y, int width) {
        guiGraphics.blit(TEXTURE, x, y, width, FOOTER_HEIGHT - FOOTER_BORDER, PLAIN_U, PLAIN_V, 1, 1, TEXTURE_SIZE, TEXTURE_SIZE);
        guiGraphics.blit(TEXTURE, x, y + FOOTER_HEIGHT - FOOTER_BORDER, width, FOOTER_BORDER, PLAIN_U, BORDER_V, 1, FOOTER_BORDER, TEXTURE_SIZE, TEXTURE_SIZE);
    }
}
