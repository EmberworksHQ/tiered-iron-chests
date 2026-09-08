Tiered Iron Chests rebuilds the classic iron chests progression — copper, iron, gold, diamond, netherite — for modern Minecraft. Five tiers from 27 to 108 slots, five upgrade kits, and vanilla-perfect automation: hoppers, hopper minecarts, droppers, comparators, and cats all behave exactly like a vanilla chest.

Tier slots: Copper 27 · Iron 54 · Gold 72 · Diamond 108 · Netherite 108. Every upgrade is a plain crafting recipe — all 15 recipes are data-pack overridable and visible in the recipe book, JEI, REI and EMI. Ships day one on all four combos: Fabric and NeoForge, each on Minecraft 1.21.1 and 1.20.1. The two legacy iron chests leaders (6.1M combined downloads) still have no 1.21.1 build — this is the fresh, complete, modern rebuild.

## Compatibility

- Fabric 1.21.1 — supported day one
- Fabric 1.20.1 — supported day one
- NeoForge 1.21.1 — supported day one
- NeoForge 1.20.1 — supported day one
- NeoForge: no dependencies. Fabric: Fabric API (standard for Fabric mods)
- No config file needed — slot counts are fixed by design

## The tiers

| Tier | Slots | Layout | Chest recipe |
|---|---|---|---|
| Copper | 27 | 9×3 | 8 copper ingots around a vanilla chest |
| Iron | 54 | 9×6 | 8 iron ingots around a Copper Chest |
| Gold | 72 | 9×8 | 8 gold ingots around an Iron Chest |
| Diamond | 108 | 12×9 | 6 glass + 2 diamonds around a Gold Chest |
| Netherite | 108 | 12×9 | 6 obsidian + 2 netherite ingots around a Diamond Chest — blast-proof |

## Upgrade kits

Five kits: **Wood → Copper, Copper → Iron, Iron → Gold, Gold → Diamond, Diamond → Netherite**. Craft the kit (same ring recipe as the chest, with the previous tier's material in the middle), then combine **kit + previous chest** anywhere in a crafting grid to get the next tier. Kit tooltips tell you which chest they want.

v1 upgrades are crafting-only: empty the chest first (breaking it drops the contents like a vanilla chest), then craft. Right-click in-place upgrades that keep the contents inside are planned for v1.1. The netherite chest is blast-proof — explosions never destroy it or its contents.

## FAQ

**Do hoppers and comparators work?**
Yes — vanilla parity. Hoppers insert and extract, hopper minecarts load and unload, droppers work, comparators read fill level, and cats can still sit on the lid.

**Is it on 1.21.1?**
Yes. Fabric and NeoForge builds for both 1.21.1 and 1.20.1 release together on day one — the legacy iron chests mods don't offer that.

**Will my chests from other iron-chest mods convert?**
Not in v1 — there is no auto-conversion from other mods' chests. Conversion is on the roadmap.

## Scope notes

v1 keeps the block set tight by design: tiered chests don't pair into double chests, and there's no pickup-to-chest magnet mode. Documented v1 scope, not missing features.

## License & source

MIT — github.com/AutoModsStudio/tiered-iron-chests
