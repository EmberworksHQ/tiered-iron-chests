# Tiered Iron Chests — the classic *iron chests* progression, rebuilt for 1.21.1 & 1.20.1

Copper, iron, gold, diamond, netherite: the chest progression you already know, rebuilt from scratch for modern Minecraft. Five tiers, five upgrade kits, vanilla-perfect automation behavior, all four loader/version combos on day one. The two legacy iron chests leaders (6.1M combined downloads) still have no 1.21.1 build — this is the fresh, complete, modern rebuild.

## Why Tiered Iron Chests

- **Full five-tier progression** — copper 27 slots up to netherite 108: 27 → 54 → 72 → 108 → 108.
- **Craft your way up** — every upgrade is a plain crafting recipe, and all 15 recipes are data-pack overridable and visible in the recipe book, JEI, REI and EMI.
- **Vanilla-perfect behavior** — hoppers, hopper minecarts, droppers, comparators, and cats sitting on the lid all work exactly like a vanilla chest.

## The tiers

| Tier | Slots | Layout | Chest recipe |
|---|---|---|---|
| Copper | 27 | 9×3 | 8 copper ingots around a vanilla chest |
| Iron | 54 | 9×6 | 8 iron ingots around a Copper Chest |
| Gold | 72 | 9×8 | 8 gold ingots around an Iron Chest |
| Diamond | 108 | 12×9 | 6 glass + 2 diamonds around a Gold Chest |
| Netherite | 108 | 12×9 | 6 obsidian + 2 netherite ingots around a Diamond Chest — blast-proof |

Slot counts are fixed by design — no config file needed. The netherite chest uses obsidian-class blast resistance: explosions never destroy it or its contents.

## Upgrade kits

Each tier has an upgrade kit: **Wood → Copper, Copper → Iron, Iron → Gold, Gold → Diamond, Diamond → Netherite**. Craft the kit (same ring recipe as the chest, with the previous tier's material in the middle), then combine **kit + previous chest** anywhere in a crafting grid to get the next tier. Kit tooltips tell you which chest they want.

v1 upgrades are crafting-only: empty the chest first (breaking it drops the contents exactly like a vanilla chest), then craft. Right-click in-place upgrades that keep the contents inside are planned for v1.1.

## Compatibility

| | 1.21.1 | 1.20.1 |
|---|---|---|
| Fabric | ✓ | ✓ |
| NeoForge | ✓ | ✓ |

All four combos ship day one — the legacy iron chests mods aren't on 1.21.1 at all. **NeoForge: no dependencies. Fabric: Fabric API** (it serves the jar's data and keeps registry ids in sync — the Fabric loader alone provides neither).

## FAQ

**Do hoppers and comparators work?**
Yes — vanilla parity. Hoppers insert and extract, hopper minecarts load and unload, droppers work, comparators read fill level, and cats can still sit on the lid.

**Do I lose my items when I upgrade?**
Break the chest first — contents spill out exactly like a vanilla chest — then craft the old chest with a kit to get the next tier. In-place upgrades that keep contents inside are coming in v1.1.

**Is it on 1.21.1?**
Yes. Fabric and NeoForge builds for both 1.21.1 and 1.20.1 release together on day one — the legacy iron chests mods don't offer that.

## Scope notes

v1 keeps the block set tight by design: tiered chests don't pair into double chests, and there's no pickup-to-chest magnet mode. Documented v1 scope, not missing features.

## License & source

MIT — [github.com/EmberworksHQ/tiered-iron-chests](https://github.com/EmberworksHQ/tiered-iron-chests)
