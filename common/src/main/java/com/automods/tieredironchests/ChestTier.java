package com.automods.tieredironchests;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.Optional;

/**
 * The five chest tiers. Everything tier-specific (slot count, GUI layout, block properties, texture, registry names)
 * is derived from here so the loaders only loop over {@link #values()}.
 *
 * <p>Slot counts are fixed by design (BRIEF section 3) and intentionally not configurable.
 */
public enum ChestTier implements StringRepresentable {
    COPPER("copper", "wood", 27, 9),
    IRON("iron", "copper", 54, 9),
    GOLD("gold", "iron", 72, 9),
    DIAMOND("diamond", "gold", 108, 12),
    NETHERITE("netherite", "diamond", 108, 12);

    public static final Codec<ChestTier> CODEC = StringRepresentable.fromEnum(ChestTier::values);

    /** Explosion resistance of every tier except netherite (iron block). */
    private static final float DEFAULT_EXPLOSION_RESISTANCE = 6.0F;
    /** Netherite: obsidian-class blast resistance, so TNT / creepers / ghast fireballs never destroy it (BRIEF SHOULD). */
    private static final float NETHERITE_EXPLOSION_RESISTANCE = 1200.0F;
    private static final float DESTROY_TIME = 3.0F;

    private final String name;
    private final String previousName;
    private final int slots;
    private final int columns;

    ChestTier(String name, String previousName, int slots, int columns) {
        this.name = name;
        this.previousName = previousName;
        this.slots = slots;
        this.columns = columns;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** Total inventory slots. */
    public int slots() {
        return slots;
    }

    /** Slots per GUI row. */
    public int columns() {
        return columns;
    }

    /** GUI rows. */
    public int rows() {
        return slots / columns;
    }

    /** Registry path of the block, block item, block entity type and menu type: {@code <tier>_chest}. */
    public String blockName() {
        return name + "_chest";
    }

    /** Registry path of the upgrade kit that produces this tier: {@code <previous>_to_<tier>_chest_upgrade}. */
    public String kitName() {
        return previousName + "_to_" + name + "_chest_upgrade";
    }

    /** Translation key used as the menu title ({@code container.tieredironchests.<tier>_chest}). */
    public String containerTranslationKey() {
        return "container." + TieredIronChests.MOD_ID + "." + blockName();
    }

    /**
     * Sprite id inside the vanilla chest atlas. The atlas lists {@code textures/entity/chest/} as a directory source,
     * which spans every namespace, so {@code assets/tieredironchests/textures/entity/chest/<tier>.png} is picked up
     * automatically.
     */
    public ResourceLocation entityTexture() {
        return TieredIronChests.id("entity/chest/" + name);
    }

    /** The tier one step below, empty for copper (whose predecessor is the vanilla wooden chest). */
    public Optional<ChestTier> previous() {
        return ordinal() == 0 ? Optional.empty() : Optional.of(values()[ordinal() - 1]);
    }

    /** Vanilla-style block properties; only netherite differs (blast resistance). Any tool drops the block. */
    public BlockBehaviour.Properties blockProperties() {
        float resistance = this == NETHERITE ? NETHERITE_EXPLOSION_RESISTANCE : DEFAULT_EXPLOSION_RESISTANCE;
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor())
                .strength(DESTROY_TIME, resistance)
                .sound(soundType());
    }

    private MapColor mapColor() {
        return switch (this) {
            case COPPER -> MapColor.COLOR_ORANGE;
            case IRON -> MapColor.METAL;
            case GOLD -> MapColor.GOLD;
            case DIAMOND -> MapColor.DIAMOND;
            case NETHERITE -> MapColor.COLOR_BLACK;
        };
    }

    private SoundType soundType() {
        return switch (this) {
            case COPPER -> SoundType.COPPER;
            case NETHERITE -> SoundType.NETHERITE_BLOCK;
            default -> SoundType.METAL;
        };
    }
}
