# Tiered Iron Chests — texture hand-off spec (v1.0.0)

Drop-in replacement of the flat placeholder PNGs that ship today. Same paths, same sizes; nothing else changes.
All paths are relative to `common/src/main/resources/`. PNG, RGBA, no interpolation (nearest-neighbour pixel art).

## Deviation from the BRIEF

The BRIEF asked for 32x32 item sprites. We ship **16x16** item sprites (`item/generated` models) because that is the
vanilla-native item resolution: 32x32 sprites in the block atlas get mip-mapped inconsistently next to 16x16 vanilla
items and look blurry in inventories at the default GUI scale. The chest *blocks* are 3D block-entity models, so
their item form is a flat sprite (vanilla chest uses a special built-in renderer; we deliberately avoid that to stay
loader-API-free).

## 1. Chest entity textures (5 files, 64x64 each)

```
assets/tieredironchests/textures/entity/chest/copper.png
assets/tieredironchests/textures/entity/chest/iron.png
assets/tieredironchests/textures/entity/chest/gold.png
assets/tieredironchests/textures/entity/chest/diamond.png
assets/tieredironchests/textures/entity/chest/netherite.png
```

Exactly the vanilla single-chest UV layout (`assets/minecraft/textures/entity/chest/normal.png` in the vanilla jar is
the reference — start from it). The model is vanilla `ModelLayers.CHEST`:

| Part | Box (x, y, z size) | texOffs | UV regions (x0..x1, y0..y1, exclusive end) |
|---|---|---|---|
| lid | 14 x 5 x 14 | (0, 0) | top 14..28 x 0..14, bottom 28..42 x 0..14, sides row y 14..19: west 0..14, **front 14..28**, east 28..42, back 42..56 |
| lock (latch) | 2 x 4 x 1 | (0, 0) | top 1..3 x 0..1, bottom 3..5 x 0..1, sides row y 1..5: west 0..1, **front 1..3**, east 3..4, back 4..6 |
| base | 14 x 10 x 14 | (0, 19) | top 14..28 x 19..33, bottom 28..42 x 19..33, sides row y 33..43: west 0..14, **front 14..28**, east 28..42, back 42..56 |

Everything outside those regions (x >= 56, y >= 43, and the 6..14 x 0..14 corner) is unused and should stay
transparent. "Front" is the face with the latch. The lid rotates on its back edge, so keep the lid's back side row
(42..56 x 14..19) consistent with the base's back (42..56 x 33..43).

Style: vanilla-adjacent. Same silhouette, edge highlights and rivets as the vanilla chest, with the wood replaced by
the tier metal (copper: oxidised-free bright copper; iron: brushed iron block; gold: gold block; diamond: diamond
block cyan with white facets; netherite: dark `netherite_block` with subtle purple sheen). The latch may stay
vanilla-iron on every tier except gold/netherite where a matching latch reads better. No Christmas variants.

## 2. Chest item sprites (5 files, 16x16 each)

```
assets/tieredironchests/textures/item/copper_chest.png
assets/tieredironchests/textures/item/iron_chest.png
assets/tieredironchests/textures/item/gold_chest.png
assets/tieredironchests/textures/item/diamond_chest.png
assets/tieredironchests/textures/item/netherite_chest.png
```

A 3/4-view chest icon (like the vanilla chest as rendered in the inventory: lid slightly lighter, latch visible),
tinted per tier. Transparent background, 1 px dark outline, nothing touching the outer 1 px margin except the
outline where needed.

## 3. Upgrade-kit item sprites (5 files, 16x16 each)

```
assets/tieredironchests/textures/item/wood_to_copper_chest_upgrade.png
assets/tieredironchests/textures/item/copper_to_iron_chest_upgrade.png
assets/tieredironchests/textures/item/iron_to_gold_chest_upgrade.png
assets/tieredironchests/textures/item/gold_to_diamond_chest_upgrade.png
assets/tieredironchests/textures/item/diamond_to_netherite_chest_upgrade.png
```

Suggested read: a small plate/kit in the **target** tier's metal with a white or light "up" arrow — the tooltip
already says which chest it upgrades. Keep the five clearly distinguishable by colour at 16 px (they sit next to each
other in the creative tab).

## 4. Mod icon (in-jar) and store art

```
assets/tieredironchests/icon.png            64x64  (shown by mod menus; keep the AutoMods dark-slate tile + gold pill)
art/icon.png                               512x512 store icon: diamond chest + tier arrows up
art/banner.png                            1920x1080 store banner
art/icon-64.png                             64x64  same as the in-jar icon (kept in art/ for the listing pipeline)
```

## Regenerating placeholders

`python tools/gen_resources.py --mc 1.21.1` never overwrites an existing PNG; `--force-textures` does. Do not run it
with `--force-textures` once real art is in place.
