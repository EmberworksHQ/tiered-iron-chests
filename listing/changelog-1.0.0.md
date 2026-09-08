# Changelog — Tiered Iron Chests 1.0.0

Initial release. Scope frozen per BRIEF v1.0.0 (approved 2026-09-08).

## Added

### Chest tiers (5)
- **Copper Chest** — 27 slots
- **Iron Chest** — 54 slots
- **Gold Chest** — 72 slots
- **Diamond Chest** — 108 slots
- **Netherite Chest** — 108 slots
- One chest model with five per-tier entity textures (tier = skin).

### Upgrade kits (4)
Crafting recipes; contents are preserved on every upgrade.
- Copper → Iron kit
- Iron → Gold kit
- Gold → Diamond kit
- Diamond → Netherite kit
- Crafting-only in v1; right-click transform is planned for v1.1.

### Vanilla parity
- Hoppers insert and extract correctly.
- Hopper minecarts load and unload correctly.
- Comparators read chest fill level.
- Cats sit on tiered chests.
- Netherite chest keeps its contents when blown up (vanilla blast resistance).

### Platform
- Fabric 1.21.1, Fabric 1.20.1, NeoForge 1.21.1, NeoForge 1.20.1 — all four combos day one.
- Zero hard dependencies.

## Documented v1 scope limits
- Tiered chests do not pair into double chests (tiered blocks only).
- No pickup-to-chest magnet mode.
- No auto-conversion of chests from other iron-chest mods (roadmap).
- No config file — tier slot counts are fixed by design.
