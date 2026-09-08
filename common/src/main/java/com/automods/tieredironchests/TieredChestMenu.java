package com.automods.tieredironchests;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Vanilla {@code ChestMenu} semantics (shift-click routing, open/close notifications, {@code stillValid}) at the
 * tier's column x row layout. 9-column tiers use the exact vanilla slot coordinates; 12-column tiers keep the player
 * inventory centred under the wider chest grid. The screen reads the same layout numbers from {@link #imageWidth},
 * {@link #imageHeight} and {@link #playerInventoryLeft}.
 */
public class TieredChestMenu extends AbstractContainerMenu {
    public static final int SLOT_SIZE = 18;
    /** Width of the vanilla chest GUI (9 columns); the player inventory block is always this wide. */
    public static final int VANILLA_WIDTH = 176;
    private static final int GRID_LEFT = 8;
    private static final int GRID_TOP = 18;
    /** Height above the player inventory block: header (17) + rows, plus the 14 px "Inventory" label band. */
    private static final int HEADER_HEIGHT = 17;
    private static final int LABEL_BAND = 14;
    private static final int PLAYER_ROWS = 3;
    private static final int HOTBAR_GAP = 4;
    private static final int FOOTER_HEIGHT = 96;

    private final ChestTier tier;
    private final Container container;

    /** Client-side constructor (menu type factory): the container is a throwaway of the right size. */
    public static TieredChestMenu clientSide(ChestTier tier, int containerId, Inventory playerInventory) {
        return new TieredChestMenu(TieredChestContent.menuType(tier), tier, containerId, playerInventory,
                new SimpleContainer(tier.slots()));
    }

    public TieredChestMenu(MenuType<?> type, ChestTier tier, int containerId, Inventory playerInventory, Container container) {
        super(type, containerId);
        checkContainerSize(container, tier.slots());
        this.tier = tier;
        this.container = container;
        container.startOpen(playerInventory.player);

        int columns = tier.columns();
        for (int row = 0; row < tier.rows(); row++) {
            for (int column = 0; column < columns; column++) {
                this.addSlot(new Slot(container, column + row * columns, GRID_LEFT + column * SLOT_SIZE, GRID_TOP + row * SLOT_SIZE));
            }
        }

        int playerLeft = playerInventoryLeft(tier);
        int playerTop = playerInventoryTop(tier);
        for (int row = 0; row < PLAYER_ROWS; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, playerLeft + column * SLOT_SIZE, playerTop + row * SLOT_SIZE));
            }
        }
        int hotbarTop = playerTop + PLAYER_ROWS * SLOT_SIZE + HOTBAR_GAP;
        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, playerLeft + column * SLOT_SIZE, hotbarTop));
        }
    }

    public ChestTier tier() {
        return tier;
    }

    public Container getContainer() {
        return container;
    }

    // ---- layout shared with the screen ----

    public static int imageWidth(ChestTier tier) {
        return tier.columns() * SLOT_SIZE + 2 * (GRID_LEFT - 1);
    }

    public static int imageHeight(ChestTier tier) {
        return HEADER_HEIGHT + tier.rows() * SLOT_SIZE + FOOTER_HEIGHT + 1;
    }

    /** X of the first player-inventory slot: 8 for 9-column tiers, centred for wider grids. */
    public static int playerInventoryLeft(ChestTier tier) {
        return (imageWidth(tier) - VANILLA_WIDTH) / 2 + GRID_LEFT;
    }

    /** Y of the first player-inventory slot (vanilla: 103 + (rows - 4) * 18). */
    public static int playerInventoryTop(ChestTier tier) {
        return HEADER_HEIGHT + tier.rows() * SLOT_SIZE + LABEL_BAND;
    }

    // ---- vanilla ChestMenu behaviour ----

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int chestSlots = tier.slots();
            if (index < chestSlots) {
                if (!this.moveItemStackTo(stack, chestSlots, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, chestSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}
