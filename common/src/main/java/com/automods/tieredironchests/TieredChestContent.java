package com.automods.tieredironchests;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Registry-agnostic content model: factories for every registered object plus lookups by tier.
 *
 * <p>Each loader adapter creates the objects with the {@code create*} factories through its own registration API
 * (vanilla {@code Registry.register} on Fabric, {@code DeferredRegister} on NeoForge) and hands suppliers back via
 * {@link #registerTier}. Common code only ever resolves objects lazily through the lookups, so it never depends on
 * registration order.
 */
public final class TieredChestContent {
    private record Entry(
            Supplier<? extends Block> block,
            Supplier<? extends Item> blockItem,
            Supplier<BlockEntityType<TieredChestBlockEntity>> blockEntityType,
            Supplier<MenuType<TieredChestMenu>> menuType,
            Supplier<? extends Item> kit) {
    }

    private static final Map<ChestTier, Entry> ENTRIES = new EnumMap<>(ChestTier.class);

    private TieredChestContent() {
    }

    // ---- factories (called by the loader adapters exactly once per tier) ----

    public static TieredChestBlock createBlock(ChestTier tier) {
        return new TieredChestBlock(tier, tier.blockProperties());
    }

    public static BlockItem createBlockItem(ChestTier tier, Block block) {
        return new BlockItem(block, new Item.Properties());
    }

    public static BlockEntityType<TieredChestBlockEntity> createBlockEntityType(ChestTier tier, Block block) {
        return BlockEntityType.Builder.of((pos, state) -> new TieredChestBlockEntity(tier, pos, state), block).build(null);
    }

    /** One menu type per tier; the tier (and therefore the slot layout) is implied by the type, no extra data needed. */
    public static MenuType<TieredChestMenu> createMenuType(ChestTier tier) {
        return new MenuType<>((containerId, inventory) -> TieredChestMenu.clientSide(tier, containerId, inventory),
                FeatureFlags.VANILLA_SET);
    }

    public static ChestUpgradeItem createKit(ChestTier tier) {
        return new ChestUpgradeItem(tier, new Item.Properties());
    }

    // ---- registration hand-off ----

    public static void registerTier(ChestTier tier,
                                    Supplier<? extends Block> block,
                                    Supplier<? extends Item> blockItem,
                                    Supplier<BlockEntityType<TieredChestBlockEntity>> blockEntityType,
                                    Supplier<MenuType<TieredChestMenu>> menuType,
                                    Supplier<? extends Item> kit) {
        ENTRIES.put(tier, new Entry(block, blockItem, blockEntityType, menuType, kit));
    }

    // ---- lookups ----

    public static Block block(ChestTier tier) {
        return entry(tier).block().get();
    }

    public static Item blockItem(ChestTier tier) {
        return entry(tier).blockItem().get();
    }

    public static BlockEntityType<TieredChestBlockEntity> blockEntityType(ChestTier tier) {
        return entry(tier).blockEntityType().get();
    }

    public static MenuType<TieredChestMenu> menuType(ChestTier tier) {
        return entry(tier).menuType().get();
    }

    public static Item kit(ChestTier tier) {
        return entry(tier).kit().get();
    }

    /** The chest a kit of the given tier is applied to: the previous tier, or the vanilla chest for copper. */
    public static Block previousChestBlock(ChestTier tier) {
        return tier.previous().map(TieredChestContent::block).orElse(Blocks.CHEST);
    }

    /** Creative-tab entries in display order: the five chests, then the five kits. */
    public static List<ItemStack> creativeTabEntries() {
        List<ItemStack> entries = new ArrayList<>(ChestTier.values().length * 2);
        for (ChestTier tier : ChestTier.values()) {
            entries.add(new ItemStack(blockItem(tier)));
        }
        for (ChestTier tier : ChestTier.values()) {
            entries.add(new ItemStack(kit(tier)));
        }
        return entries;
    }

    private static Entry entry(ChestTier tier) {
        Entry entry = ENTRIES.get(tier);
        if (entry == null) {
            throw new IllegalStateException("Tiered Iron Chests: tier " + tier + " was never registered by the loader adapter");
        }
        return entry;
    }
}
