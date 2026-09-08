package com.automods.tieredironchests;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.stream.IntStream;

/**
 * Block entity of a tiered chest: vanilla {@code ChestBlockEntity} semantics at the tier's slot count.
 *
 * <ul>
 *   <li>Storage: {@link RandomizableContainerBlockEntity} (loot tables, lock, custom name, "Items" NBT via
 *       {@link ContainerHelper}) - the same base class as the vanilla chest, so hoppers, hopper minecarts, droppers
 *       and comparators see a plain vanilla {@code Container}.</li>
 *   <li>{@link WorldlyContainer}: every slot is reachable from every face with vanilla insert/extract rules. This is
 *       behaviourally identical to the vanilla chest for hoppers and additionally exposes the sided API that
 *       loader item-transfer layers wrap.</li>
 *   <li>Lid: vanilla {@link ContainerOpenersCounter} + {@link ChestLidController}, chest open/close sounds, block
 *       event 1 carries the viewer count to clients.</li>
 * </ul>
 *
 * <p>Not extended from {@code ChestBlockEntity}: its item list is private and sized 27 in {@code load/save}, and its
 * openers counter only recognises {@code ChestMenu}, which cannot lay out 12 columns.
 */
public class TieredChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity, WorldlyContainer {
    /** Block-event id that carries the viewer count (vanilla ChestBlock.EVENT_SET_OPEN_COUNT). */
    private static final int EVENT_SET_OPEN_COUNT = 1;

    private final ChestTier tier;
    private final int[] slotsForAnyFace;
    private NonNullList<ItemStack> items;
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_OPEN);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            playSound(level, pos, SoundEvents.CHEST_CLOSE);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int previousCount, int newCount) {
            level.blockEvent(pos, state.getBlock(), EVENT_SET_OPEN_COUNT, newCount);
        }

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof TieredChestMenu menu && menu.getContainer() == TieredChestBlockEntity.this;
        }
    };
    private final ChestLidController lidController = new ChestLidController();

    public TieredChestBlockEntity(ChestTier tier, BlockPos pos, BlockState state) {
        super(TieredChestContent.blockEntityType(tier), pos, state);
        this.tier = tier;
        this.items = NonNullList.withSize(tier.slots(), ItemStack.EMPTY);
        this.slotsForAnyFace = IntStream.range(0, tier.slots()).toArray();
    }

    public ChestTier tier() {
        return tier;
    }

    // ---- container ----

    @Override
    public int getContainerSize() {
        return tier.slots();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(tier.containerTranslationKey());
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new TieredChestMenu(TieredChestContent.menuType(tier), tier, containerId, inventory, this);
    }

    // ---- NBT (same shape as the vanilla chest: "Items" list unless a loot table is pending) ----

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
    }

    // ---- sided access: every slot from every face, vanilla insert rules ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return slotsForAnyFace;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    // ---- lid / viewers (vanilla ChestBlockEntity) ----

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, TieredChestBlockEntity chest) {
        chest.lidController.tickLid();
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == EVENT_SET_OPEN_COUNT) {
            this.lidController.shouldBeOpen(type > 0);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    /** Number of players currently viewing this chest (what cats check before sitting on a chest). */
    public int getOpenCount() {
        return this.openersCounter.getOpenerCount();
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.lidController.getOpenness(partialTicks);
    }

    private static void playSound(Level level, BlockPos pos, SoundEvent sound) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }
}
