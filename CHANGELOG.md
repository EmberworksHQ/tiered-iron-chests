# Changelog

## 1.0.0

- Initial release for Fabric + NeoForge on Minecraft 1.21.1 and 1.20.1.
- Five chest tiers: Copper (27 slots, 9x3), Iron (54, 9x6), Gold (72, 9x8), Diamond (108, 12x9) and Netherite (108, 12x9). One chest model, one texture per tier; single blocks only (no double-chest pairing).
- Vanilla chest behaviour at every tier: hoppers, hopper minecarts, droppers and comparators see a plain vanilla container; lid animation, open/close sounds and viewer counting are the vanilla ones; a solid block or a sitting cat on top blocks the lid (and cats will sit on tiered chests like on vanilla ones); the contents drop when the block breaks; custom names survive breaking and placing; loot tables (`LootTable` / `minecraft:set_loot_table`) work like on a vanilla chest.
- Netherite chest has obsidian-class blast resistance (1200): explosions do not destroy it, so its contents stay inside.
- Upgrade kits (Wood->Copper, Copper->Iron, Iron->Gold, Gold->Diamond, Diamond->Netherite), each craftable and applied in a crafting grid: kit + previous-tier chest (shapeless) -> next tier. Every tier also has the classic "ring around the previous chest" shaped recipe. All 15 recipes are plain JSON (data-pack overridable, recipe-book / JEI / REI / EMI visible).
- Creative tab: Functional Blocks, right after the vanilla chest.
- Tags: `minecraft:mineable/pickaxe` (any tool still drops the block), `c:chests` block + item tags (plus `forge:chests` on 1.20.1).
- NeoForge: item-handler capability on every tier for pipe/storage mods. Fabric: vanilla `WorldlyContainer`, so the Fabric transfer API's inventory fallback applies.
- Dependencies: none on NeoForge; Fabric API on Fabric (resource loading + registry sync are Fabric API modules, not loader features). No config: slot counts are fixed by design, recipes are data-pack overridable.
