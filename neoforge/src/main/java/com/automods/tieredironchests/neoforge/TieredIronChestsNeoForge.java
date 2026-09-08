package com.automods.tieredironchests.neoforge;

import com.automods.tieredironchests.ChestTier;
import com.automods.tieredironchests.ChestUpgradeItem;
import com.automods.tieredironchests.TieredChestBlock;
import com.automods.tieredironchests.TieredChestBlockEntity;
import com.automods.tieredironchests.TieredChestContent;
import com.automods.tieredironchests.TieredChestMenu;
import com.automods.tieredironchests.TieredIronChests;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge adapter: deferred registration of every tier, creative-tab placement after the vanilla chest and the
 * item-handler capability (a plain {@link InvWrapper} around the vanilla container) for pipe/storage mods.
 */
@Mod(TieredIronChests.MOD_ID)
public final class TieredIronChestsNeoForge {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, TieredIronChests.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, TieredIronChests.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TieredIronChests.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, TieredIronChests.MOD_ID);

    public TieredIronChestsNeoForge(IEventBus modBus) {
        for (ChestTier tier : ChestTier.values()) {
            DeferredHolder<Block, TieredChestBlock> block =
                    BLOCKS.register(tier.blockName(), () -> TieredChestContent.createBlock(tier));
            DeferredHolder<Item, BlockItem> blockItem =
                    ITEMS.register(tier.blockName(), () -> TieredChestContent.createBlockItem(tier, block.get()));
            DeferredHolder<BlockEntityType<?>, BlockEntityType<TieredChestBlockEntity>> blockEntityType =
                    BLOCK_ENTITY_TYPES.register(tier.blockName(), () -> TieredChestContent.createBlockEntityType(tier, block.get()));
            DeferredHolder<MenuType<?>, MenuType<TieredChestMenu>> menuType =
                    MENU_TYPES.register(tier.blockName(), () -> TieredChestContent.createMenuType(tier));
            DeferredHolder<Item, ChestUpgradeItem> kit =
                    ITEMS.register(tier.kitName(), () -> TieredChestContent.createKit(tier));
            TieredChestContent.registerTier(tier, block, blockItem, blockEntityType, menuType, kit);
        }
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);

        modBus.addListener(TieredIronChestsNeoForge::onBuildCreativeTabContents);
        modBus.addListener(TieredIronChestsNeoForge::onRegisterCapabilities);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TieredIronChestsNeoForgeClient.init(modBus);
        }
        TieredIronChests.logRegistered("(NeoForge)");
    }

    /** Functional Blocks tab, directly after the vanilla chest (appended if another mod removed the chest). */
    private static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            return;
        }
        ItemStack anchor = new ItemStack(Items.CHEST);
        boolean anchored = event.getParentEntries().contains(anchor) && event.getSearchEntries().contains(anchor);
        for (ItemStack entry : TieredChestContent.creativeTabEntries()) {
            if (anchored) {
                event.insertAfter(anchor, entry, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            } else {
                event.accept(entry, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            anchor = entry;
        }
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        for (ChestTier tier : ChestTier.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, TieredChestContent.blockEntityType(tier),
                    (chest, side) -> new InvWrapper(chest));
        }
    }
}
