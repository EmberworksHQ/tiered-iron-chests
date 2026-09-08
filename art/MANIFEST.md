# Tiered Iron Chests — Art Manifest (v1.0.0)

Artist deliverables for modid `tieredironchests`. All game textures are native-resolution pixel art
(no supersampling, no partial alpha, 9 colours each). Brand art (icon/banner) rendered 2x → LANCZOS.

No `TEXTURE-SPEC.md` was present when art was produced; paths below follow **vanilla chest conventions**
and the engineer's `ChestTier.java` (`entityTexture()`, `blockName()`, `kitName()`). If the spec lands with
different names, only the copy step changes — the pixels are final.

## Drop-in tree

`art/jar/` mirrors the exact in-jar layout. Copy `art/jar/assets` over `common/src/main/resources/assets`
(or the loader resources dir) as-is.

| Source file (art/)          | In-jar path                                                                  | Size  | Notes |
|-----------------------------|------------------------------------------------------------------------------|-------|-------|
| `chest_copper.png`          | `assets/tieredironchests/textures/entity/chest/copper.png`                   | 64×64 | Chest BER texture, vanilla single-chest UV |
| `chest_iron.png`            | `assets/tieredironchests/textures/entity/chest/iron.png`                     | 64×64 | " |
| `chest_gold.png`            | `assets/tieredironchests/textures/entity/chest/gold.png`                     | 64×64 | " |
| `chest_diamond.png`         | `assets/tieredironchests/textures/entity/chest/diamond.png`                  | 64×64 | " |
| `chest_netherite.png`       | `assets/tieredironchests/textures/entity/chest/netherite.png`                | 64×64 | " |
| `item_chest_copper.png`     | `assets/tieredironchests/textures/item/copper_chest.png`                     | 16×16 | Flat item sprite for `copper_chest` block item (`item/generated`) |
| `item_chest_iron.png`       | `assets/tieredironchests/textures/item/iron_chest.png`                       | 16×16 | " |
| `item_chest_gold.png`       | `assets/tieredironchests/textures/item/gold_chest.png`                       | 16×16 | " |
| `item_chest_diamond.png`    | `assets/tieredironchests/textures/item/diamond_chest.png`                    | 16×16 | " |
| `item_chest_netherite.png`  | `assets/tieredironchests/textures/item/netherite_chest.png`                  | 16×16 | " |
| `kit_wood.png`              | `assets/tieredironchests/textures/item/wood_to_copper_chest_upgrade.png`     | 16×16 | Oak plate, copper arrow |
| `kit_copper.png`            | `assets/tieredironchests/textures/item/copper_to_iron_chest_upgrade.png`     | 16×16 | Copper plate, iron arrow |
| `kit_iron.png`              | `assets/tieredironchests/textures/item/iron_to_gold_chest_upgrade.png`       | 16×16 | Iron plate, gold arrow |
| `kit_gold.png`              | `assets/tieredironchests/textures/item/gold_to_diamond_chest_upgrade.png`    | 16×16 | Gold plate, diamond arrow |
| `kit_diamond.png`           | `assets/tieredironchests/textures/item/diamond_to_netherite_chest_upgrade.png` | 16×16 | Diamond plate, netherite arrow |
| `icon-64.png`               | jar root `icon.png` (referenced by `fabric.mod.json` / `neoforge.mods.toml` `logoFile`) | 64×64 | LANCZOS of icon.png |
| `icon.png`                  | Modrinth / CurseForge project icon (not in jar)                              | 512×512 | Brand tile |
| `banner.png`                | Modrinth gallery / CurseForge banner (not in jar)                            | 1920×1080 | Brand banner |

Not shipped: `_src/` (Pillow generators — rerun to regenerate everything), `_preview/` (x8/x12 upscales,
software-rendered iso previews, contact sheet used for self-review).

## Chest entity texture layout (64×64)

Matches `ChestRenderer.createSingleBodyLayer()` exactly, so the vanilla `ChestModel`/`ChestRenderer` renders
it with no model changes:

```
bottom: texOffs(0,19)  box(1,0,1, 14,10,14)
lid   : texOffs(0,0)   box(1,0,0, 14,5,14)   offset(0,9,1)
lock  : texOffs(0,0)   box(7,-2,14, 2,4,1)   offset(0,9,1)
```

Face orientation follows the vanilla convention (ChestRenderer applies no y-flip, so side faces are stored
rotated 180° and UP/DOWN faces carry the latch edge on their top row). Verified with a UV-accurate software
render (`_preview/chest_<tier>_iso.png`): latch is front-centre on the lid seam, rim/bands upright.

Unused atlas area is fully transparent; the double-chest regions are intentionally absent (no pairing in v1).
Since the vanilla chest atlas lists `textures/entity/chest/` as a directory source, these textures are picked
up in the atlas automatically — use `Sheets`-style `Material(Sheets.CHEST_SHEET, ChestTier.entityTexture())`.

## Tier palettes (outline / dark / mid / light / highlight — latch)

| Tier      | O       | D       | M       | L       | H       | Latch |
|-----------|---------|---------|---------|---------|---------|-------|
| copper    | #4A2313 | #934A2B | #C06A42 | #DA8B5E | #F0B088 | iron-grey |
| iron      | #3E3E44 | #8C8C92 | #BDBDC2 | #DADADE | #F4F4F6 | gold |
| gold      | #6A4508 | #C18C14 | #EABB2C | #F6D44E | #FFF2A0 | dark iron |
| diamond   | #125A60 | #2FA9A6 | #55D8D2 | #8CEEE8 | #D2FFFB | gold |
| netherite | #140F0E | #2A2222 | #3E3535 | #4B4141 | #5C5050 | gold (recessed darker panels) |

Latch colour alternates for contrast so each tier's silhouette differs even in a 16px slot.

## Brand system (identical to mods #1/#2)

- Tile: 512², rim `#333B4E` 12px, radius 88, checker 64px `#1A1E29`/`#1F2431`; gold pill bottom-right
  (`#3A2508` ring 9px, `#FFC531`, text `#2B1A06` Arial Black), outer (330,372)–(482,458).
- Banner: 120px checker, left panel `#2A1E18` polygon (0,0)(835,0)(695,1080)(0,1080), 10px copper stripe
  `#F0965A`, tile 576px @ (131,251) + hard shadow (+16,+16), title Arial Black 144 (white / `#F0965A`,
  `#0A0D14` 10px round stroke) at x=840, subtitle Segoe UI Bold `#D7DEE9`. Right margin ≥ 110px (min 122px).
- Mod accent: copper `#F0965A` (mod #1 green `#5CF596`, mod #2 cyan `#3CCDEC`).
