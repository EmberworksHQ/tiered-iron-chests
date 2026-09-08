# Tiered Iron Chests — the classic *iron chests* progression, rebuilt for 1.21.1 & 1.20.1

Copper, iron, gold, diamond, netherite: the chest progression you already know, rebuilt from scratch for modern Minecraft. Five tiers, four upgrade kits, vanilla-perfect automation behavior, zero dependencies. The two legacy iron chests leaders (6.1M combined downloads) still have no 1.21.1 build — this is the fresh, complete, modern rebuild.

## Why Tiered Iron Chests

- **Full five-tier progression** — copper 27 slots up to netherite 108: 27 → 54 → 72 → 108 → 108.
- **Upgrade kits preserve contents** — a chest full of loot crafts straight into the next tier with every item still inside.
- **Vanilla-perfect behavior** — hoppers, hopper minecarts, comparators, and cats sitting on the lid all work exactly like a vanilla chest.

## The tiers

| Tier | Slots | Upgrade kit material |
|---|---|---|
| Copper | 27 | — starting tier |
| Iron | 54 | Iron ingot |
| Gold | 72 | Gold ingot |
| Diamond | 108 | Diamond |
| Netherite | 108 | Netherite ingot |

Kit materials follow the classic next-metal pattern; exact items [verify-at-release]. Slot counts are fixed by design — no config file needed.

## How upgrades work

Combine a tier-N chest with the next tier's material in a crafting table and you get the tier-N+1 chest — contents preserved, every stack survives. Crafting-only in v1 (exact recipe shapes [verify-at-release]); right-click-to-upgrade is planned for v1.1.

## Compatibility

| | 1.21.1 | 1.20.1 |
|---|---|---|
| Fabric | ✓ | ✓ |
| NeoForge | ✓ | ✓ |

All four combos ship day one — the legacy iron chests mods aren't on 1.21.1 at all. Zero dependencies: drop the jar in and play.

## FAQ

**Do hoppers and comparators work?**
Yes — vanilla parity. Hoppers insert and extract, hopper minecarts load and unload, comparators read fill level, and cats can still sit on the lid.

**Is it on 1.21.1?**
Yes. Fabric and NeoForge builds for both 1.21.1 and 1.20.1 release together on day one — the legacy iron chests mods don't offer that.

**Will my chests from other iron-chest mods convert?**
Not in v1 — there is no auto-conversion from other mods' chests. Conversion is on the roadmap.

## Scope notes

v1 keeps the block set tight by design: tiered chests don't pair into double chests, and there's no pickup-to-chest magnet mode. Documented v1 scope, not missing features.

## License & source

MIT — [github.com/AutoMods/tiered-iron-chests](https://github.com/AutoMods/tiered-iron-chests)
