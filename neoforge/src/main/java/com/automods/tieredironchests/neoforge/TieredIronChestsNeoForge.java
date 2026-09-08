package com.automods.tieredironchests.neoforge;

import com.automods.tieredironchests.ChestTier;
import com.automods.tieredironchests.ChestUpgradeItem;
import com.automods.tieredironchests.TieredChestBlock;
import com.automods.tieredironchests.TieredChestBlockEntity;
import com.automods.tieredironchests.TieredChestContent;
import com.automods.tieredironchests.TieredChestMenu;
import com.automods.tieredironchests.TieredIronChests;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * NeoForge 1.20.1 adapter (Forge 47 packages): deferred registration of every tier, creative-tab placement after
 * the vanilla chest and the item-handler capability (a plain {@link InvWrapper} around the vanilla container)
 * attached to every tiered chest block entity for pipe/storage mods.
 */
@Mod(TieredIronChests.MOD_ID)
public final class TieredIronChestsNeoForge {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TieredIronChests.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TieredIronChests.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TieredIronChests.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TieredIronChests.MOD_ID);
    private static final ResourceLocation ITEM_HANDLER_ID = TieredIronChests.id("item_handler");

    public TieredIronChestsNeoForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        for (ChestTier tier : ChestTier.values()) {
            RegistryObject<TieredChestBlock> block =
                    BLOCKS.register(tier.blockName(), () -> TieredChestContent.createBlock(tier));
            RegistryObject<BlockItem> blockItem =
                    ITEMS.register(tier.blockName(), () -> TieredChestContent.createBlockItem(tier, block.get()));
            RegistryObject<BlockEntityType<TieredChestBlockEntity>> blockEntityType =
                    BLOCK_ENTITY_TYPES.register(tier.blockName(), () -> TieredChestContent.createBlockEntityType(tier, block.get()));
            RegistryObject<MenuType<TieredChestMenu>> menuType =
                    MENU_TYPES.register(tier.blockName(), () -> TieredChestContent.createMenuType(tier));
            RegistryObject<ChestUpgradeItem> kit =
                    ITEMS.register(tier.kitName(), () -> TieredChestContent.createKit(tier));
            TieredChestContent.registerTier(tier, block, blockItem, blockEntityType, menuType, kit);
        }
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);

        modBus.addListener(TieredIronChestsNeoForge::onBuildCreativeTabContents);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, TieredIronChestsNeoForge::onAttachBlockEntityCapabilities);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TieredIronChestsNeoForgeClient.init(modBus);
        }
        TieredIronChests.logRegistered("(NeoForge 1.20.1)");
    }

    /** Functional Blocks tab, directly after the vanilla chest ({@code putAfter} appends when the anchor is absent). */
    private static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            return;
        }
        ItemStack anchor = new ItemStack(Items.CHEST);
        for (ItemStack entry : TieredChestContent.creativeTabEntries()) {
            event.getEntries().putAfter(anchor, entry, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            anchor = entry;
        }
    }

    private static void onAttachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof TieredChestBlockEntity chest) {
            ItemHandlerProvider provider = new ItemHandlerProvider(chest);
            event.addCapability(ITEM_HANDLER_ID, provider);
            event.addListener(provider::invalidate);
        }
    }

    /** Forge 1.20.1 capability provider: one lazy {@link InvWrapper} per chest, invalidated with the block entity. */
    private static final class ItemHandlerProvider implements ICapabilityProvider {
        private final LazyOptional<IItemHandler> handler;

        ItemHandlerProvider(TieredChestBlockEntity chest) {
            this.handler = LazyOptional.of(() -> new InvWrapper(chest));
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return capability == ForgeCapabilities.ITEM_HANDLER ? handler.cast() : LazyOptional.empty();
        }

        void invalidate() {
            handler.invalidate();
        }
    }
}
