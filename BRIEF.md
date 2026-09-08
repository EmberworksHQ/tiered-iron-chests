# BRIEF — Tiered Iron Chests (mod #3)

Status: APPROVED by CEO 2026-09-08. Scope frozen at v1.0.0. Evidence: docs/market-research/round-1.md §#3.

## 1. Identity
- **Title:** Tiered Iron Chests
- **Slug:** `tiered-iron-chests` / **modid:** `tieredironchests`
- **Pitch:** "The classic chest progression — copper, iron, gold, diamond, netherite — rebuilt for modern Minecraft. Upgrade kits keep contents, hoppers just work, zero dependencies."
- **Search intent:** must rank for "iron chests" (11 hits; both 6.1M-dl leaders lack 1.21.1).
- **License:** MIT. **Categories:** storage (primary), utility, adventure. Loaders: fabric, neoforge.

## 2. Evidence
- Iron Chests: Restocked 3.6M dl — no 1.21.1, stale 2023. Legacy Iron Chests 2.5M — no 1.21.1.
- Iron Furnaces 9.3M active on 1.21.1 proves tiered-block appetite; chests are the unserved sibling.
- Micro-entrant (10.6K dl, 1.21.1-only) appeared 2026-08 — gold rush starting, nobody at scale.

## 3. v1 scope (FROZEN)
MUST:
- 5 tiers: copper 27, iron 54, gold 72, diamond 108, netherite 108 slots.
- Upgrade kits: tier-N chest + material → tier-N+1 chest, contents preserved (crafting recipe per tier).
- Copy vanilla ChestBlockEntity behavior exactly: hoppers, hopper minecarts, comparators, cats sitting. NO double-chest pairing for tiered blocks in v1 (documented).
- One chest model, five entity textures (tier = skin).
- All four combos day-1 (Fabric/NeoForge × 1.21.1/1.20.1). Zero hard dependencies.
SHOULD:
- Netherite chest keeps contents when blown up (blast resistance) — cheap vanilla attribute, include.
WON'T (v1):
- Crystal/shulker tiers, chest Transporter item, pickup-to-chest magnet mode, double-chest tiered pairing, IRON chest → vanilla chest downgrades.

## 4. Technical plan
- Repo: `mods/tiered-iron-chests/`, branch main = 1.21.1, branch 1.20.1; multiproject common/fabric/neoforge per D006.
- Common: tier registry (slot counts, kit recipes, textures), BlockEntity logic extending vanilla chest BE semantics, menu/screen wiring; loaders thin.
- Registration surface (bigger than mods #1/#2): 5 blocks + 5 BEs + 5 menus + 5 kits. Use vanilla registration paths via loader adapters; keep common loader-free with the build-guard task.
- Upgrade kit: right-click tier-N chest with kit OR crafting recipe — pick ONE: crafting-only for v1 (right-click transform is v1.1).
- Versions: strictly docs/toolchain-matrix.md. Learn from mods #1/#2 QA: config per-key tolerance, id normalization, cooldown patterns where relevant.
- Config (JSON): none required in v1 (tier slots are fixed by design) — only a `perTierRows` doc note. Skip config entirely if nothing is user-tunable.

## 5. Art work order (→ fable-5.1 agent) — THE BULK
- 5 chest entity textures (copper/iron/gold/diamond/netherite), vanilla chest style with metal-tier material coloring, 64x64 entity layout matching vanilla chest UV.
- 5 block-ish item sprites (32x32, vanilla chest item style tinted per tier) + 5 upgrade-kit item sprites.
- Icon 512×512 (diamond chest + tier arrows up), banner 1920×1080, in-jar icon-64.
- Style: matches the AutoMods brand system (dark slate tile, gold pill) for icon/banner; chest textures themselves must be pure vanilla-adjacent.

## 6. Release plan
- Pipeline as mods #1/#2. Changelog: CHANGELOG.md.

## 7. Success criteria (review at +6 weeks)
- ≥ 15K downloads first 6 weeks (leaders' combined velocity at maturity was ~3K/day each).
- Top-3 for "iron chests" on Modrinth.
- Zero item-loss reports; hopper/comparator parity verified.
