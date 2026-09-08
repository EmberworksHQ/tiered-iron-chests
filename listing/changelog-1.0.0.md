# Changelog — Tiered Iron Chests 1.0.0

Initial release. Scope frozen per BRIEF v1.0.0 (approved 2026-09-08).

## Added

### Chest tiers (5)
- **Copper Chest** — 27 slots (9×3)
- **Iron Chest** — 54 slots (9×6)
- **Gold Chest** — 72 slots (9×8)
- **Diamond Chest** — 108 slots (12×9)
- **Netherite Chest** — 108 slots (12×9), obsidian-class blast resistance
- One chest model with five per-tier entity textures (tier = skin).

### Upgrade kits (5)
Each kit is crafted with the same ring recipe as the chest, using the previous tier's material in the middle; combine **kit + previous chest** in any crafting grid to get the next tier. v1 upgrades are crafting-only (empty the chest first — contents drop like a vanilla chest); right-click in-place upgrades are planned for v1.1.
- Wood → Copper kit
- Copper → Iron kit
- Iron → Gold kit
- Gold → Diamond kit
- Diamond → Netherite kit
- All 15 recipes are plain JSON (`data/tieredironchests/recipe/`, `recipes/` on 1.20.1), data-pack overridable, visible in the recipe book / JEI / REI / EMI.

### Vanilla parity
- Hoppers insert and extract correctly.
- Hopper minecarts and droppers work.
- Comparators read chest fill level (0 when obstructed, like vanilla).
- Cats sit on tiered chests; solid blocks keep the lid shut.
- Custom names, `Lock`, and `LootTable` unpacking all behave like vanilla.
- Netherite chest is blast-proof — explosions never destroy it or its contents.
- Piglin anger and the "Chests Opened" statistic work; waterloggable; same 14/16 hitbox.

### Platform
- Fabric 1.21.1, Fabric 1.20.1, NeoForge 1.21.1, NeoForge 1.20.1 — all four combos day one.
- NeoForge: no dependencies. Fabric: Fabric API.

## Documented v1 scope limits
- Tiered chests do not pair into double chests (tiered blocks only).
- No pickup-to-chest magnet mode; no in-place upgrades (v1.1).
- No auto-conversion of chests from other iron-chest mods (roadmap).
- No config file — tier slot counts are fixed by design.
- The 12×9 GUI is 276 px tall: at GUI scale 4 on a 1080p display 3 px clip top and bottom (GUI scale 3 fits) — same as the legacy mods.
