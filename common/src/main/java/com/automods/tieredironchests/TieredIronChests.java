package com.automods.tieredironchests;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-independent constants. Registration itself is driven by the loader adapters (fabric/, neoforge/), which
 * call the factories in {@link TieredChestContent} and hand the registered objects back through
 * {@link TieredChestContent#registerTier}.
 */
public final class TieredIronChests {
    public static final String MOD_ID = "tieredironchests";
    public static final String MOD_NAME = "Tiered Iron Chests";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private TieredIronChests() {
    }

    /** {@code tieredironchests:<path>}. Single construction site: the ResourceLocation API differs per MC version. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    /** {@code minecraft:<path>}. */
    public static ResourceLocation vanillaId(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    /** Called once by each loader adapter after all tiers are registered. */
    public static void logRegistered(String loader) {
        LOGGER.info("{} {} registered {} chest tiers ({} blocks, block entities, menus and upgrade kits)",
                MOD_NAME, loader, ChestTier.values().length, ChestTier.values().length);
    }
}
