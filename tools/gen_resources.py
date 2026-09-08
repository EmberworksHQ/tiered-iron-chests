#!/usr/bin/env python3
"""Regenerates the data-driven resources of Tiered Iron Chests (no third-party modules needed).

    python tools/gen_resources.py --mc 1.21.1        # branch main
    python tools/gen_resources.py --mc 1.20.1        # branch 1.20.1
    python tools/gen_resources.py --mc 1.21.1 --force-textures   # overwrite placeholder PNGs (NOT once real art landed)

Writes into common/src/main/resources:
  assets/tieredironchests/lang/en_us.json, blockstates/, models/block/, models/item/
  data/tieredironchests/<loot tables>/blocks/, <recipes>/, <advancements>/recipes/misc/ (recipe-book unlocks),
  data/minecraft/tags (mineable/pickaxe), data/c (+ data/forge on 1.20.1)
  placeholder textures + mod icon, only when the file does not exist yet (see art/TEXTURE-SPEC.md for the real art)

Per-version differences handled here: data folder names (recipe/recipes, loot_table/loot_tables, tags/block vs tags/blocks,
advancement/advancements), recipe result key (id vs item), the loot-table name-copy function (copy_components vs
copy_name) and the advancement item-predicate shape ("items": "#tag" vs "tag": ...).
"""
import argparse
import json
import os
import struct
import zlib

MOD_ID = "tieredironchests"

# name, previous tier name, slot count, GUI columns, particle block, (base, light, dark) RGB
TIERS = [
    {"name": "copper", "prev": "wood", "slots": 27, "cols": 9, "particle": "minecraft:block/copper_block",
     "rgb": ((196, 106, 59), (232, 146, 96), (140, 70, 38))},
    {"name": "iron", "prev": "copper", "slots": 54, "cols": 9, "particle": "minecraft:block/iron_block",
     "rgb": ((216, 216, 216), (240, 240, 240), (150, 150, 150))},
    {"name": "gold", "prev": "iron", "slots": 72, "cols": 9, "particle": "minecraft:block/gold_block",
     "rgb": ((250, 215, 60), (255, 240, 140), (180, 140, 30))},
    {"name": "diamond", "prev": "gold", "slots": 108, "cols": 12, "particle": "minecraft:block/diamond_block",
     "rgb": ((95, 225, 215), (170, 245, 240), (55, 150, 145))},
    {"name": "netherite", "prev": "diamond", "slots": 108, "cols": 12, "particle": "minecraft:block/netherite_block",
     "rgb": ((74, 63, 69), (110, 96, 102), (40, 34, 38))},
]

# Crafting: 'X' in the pattern is the centre slot (kit: previous-tier material, direct: previous-tier chest).
# 'material' is the tier's signature ingredient; it and the centre ingredient unlock the recipe in the recipe book.
RECIPES = {
    "copper": {"material": {"item": "minecraft:copper_ingot"}, "pattern": ["CCC", "CXC", "CCC"], "keys": {"C": {"item": "minecraft:copper_ingot"}},
               "kit_center": {"tag": "minecraft:planks"}, "direct_center": {"item": "minecraft:chest"}},
    "iron": {"material": {"item": "minecraft:iron_ingot"}, "pattern": ["III", "IXI", "III"], "keys": {"I": {"item": "minecraft:iron_ingot"}},
             "kit_center": {"item": "minecraft:copper_ingot"}, "direct_center": {"item": f"{MOD_ID}:copper_chest"}},
    "gold": {"material": {"item": "minecraft:gold_ingot"}, "pattern": ["GGG", "GXG", "GGG"], "keys": {"G": {"item": "minecraft:gold_ingot"}},
             "kit_center": {"item": "minecraft:iron_ingot"}, "direct_center": {"item": f"{MOD_ID}:iron_chest"}},
    "diamond": {"material": {"item": "minecraft:diamond"}, "pattern": ["GGG", "DXD", "GGG"], "keys": {"G": {"item": "minecraft:glass"}, "D": {"item": "minecraft:diamond"}},
                "kit_center": {"item": "minecraft:gold_ingot"}, "direct_center": {"item": f"{MOD_ID}:gold_chest"}},
    "netherite": {"material": {"item": "minecraft:netherite_ingot"}, "pattern": ["OOO", "NXN", "OOO"], "keys": {"O": {"item": "minecraft:obsidian"}, "N": {"item": "minecraft:netherite_ingot"}},
                  "kit_center": {"item": "minecraft:diamond"}, "direct_center": {"item": f"{MOD_ID}:diamond_chest"}},
}

LOCK_RGB = ((120, 120, 120), (170, 170, 170), (70, 70, 70))


def block_name(tier):
    return f"{tier['name']}_chest"


def kit_name(tier):
    return f"{tier['prev']}_to_{tier['name']}_chest_upgrade"


def title(word):
    return word.capitalize()


# ---------------------------------------------------------------- files

class Layout:
    def __init__(self, mc):
        modern = mc != "1.20.1"
        self.mc = mc
        self.recipe_dir = "recipe" if modern else "recipes"
        self.loot_dir = "loot_table" if modern else "loot_tables"
        self.block_tag_dir = "tags/block" if modern else "tags/blocks"
        self.item_tag_dir = "tags/item" if modern else "tags/items"
        self.result_key = "id" if modern else "item"
        self.advancement_dir = "advancement" if modern else "advancements"
        self.modern = modern
        self.copy_name_function = (
            {"function": "minecraft:copy_components", "source": "block_entity", "include": ["minecraft:custom_name"]}
            if modern else
            {"function": "minecraft:copy_name", "source": "block_entity"}
        )
        self.extra_tag_namespaces = [] if modern else ["forge"]


def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def result(layout, item_id):
    return {layout.result_key: item_id, "count": 1}


def gen_lang(res):
    lang = {}
    for tier in TIERS:
        lang[f"block.{MOD_ID}.{block_name(tier)}"] = f"{title(tier['name'])} Chest"
    for tier in TIERS:
        lang[f"container.{MOD_ID}.{block_name(tier)}"] = f"{title(tier['name'])} Chest"
    for tier in TIERS:
        lang[f"item.{MOD_ID}.{kit_name(tier)}"] = f"{title(tier['prev'])} to {title(tier['name'])} Chest Upgrade"
    lang[f"item.{MOD_ID}.chest_upgrade.tooltip"] = "Craft with %s to make %s"
    write_json(os.path.join(res, "assets", MOD_ID, "lang", "en_us.json"), lang)


def gen_models(res):
    assets = os.path.join(res, "assets", MOD_ID)
    for tier in TIERS:
        name = block_name(tier)
        write_json(os.path.join(assets, "blockstates", f"{name}.json"),
                   {"variants": {"": {"model": f"{MOD_ID}:block/{name}"}}})
        write_json(os.path.join(assets, "models", "block", f"{name}.json"),
                   {"textures": {"particle": tier["particle"]}})
        write_json(os.path.join(assets, "models", "item", f"{name}.json"),
                   {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD_ID}:item/{name}"}})
        kit = kit_name(tier)
        write_json(os.path.join(assets, "models", "item", f"{kit}.json"),
                   {"parent": "minecraft:item/generated", "textures": {"layer0": f"{MOD_ID}:item/{kit}"}})


def gen_loot(res, layout):
    for tier in TIERS:
        name = block_name(tier)
        table = {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1.0,
                "bonus_rolls": 0.0,
                "conditions": [{"condition": "minecraft:survives_explosion"}],
                "entries": [{
                    "type": "minecraft:item",
                    "name": f"{MOD_ID}:{name}",
                    "functions": [layout.copy_name_function],
                }],
            }],
            "random_sequence": f"{MOD_ID}:blocks/{name}",
        }
        write_json(os.path.join(res, "data", MOD_ID, layout.loot_dir, "blocks", f"{name}.json"), table)


def gen_recipes(res, layout):
    out = os.path.join(res, "data", MOD_ID, layout.recipe_dir)
    for tier in TIERS:
        spec = RECIPES[tier["name"]]
        name = block_name(tier)
        kit = kit_name(tier)
        # 1) the kit itself: ring of target material around the previous tier's material
        write_json(os.path.join(out, f"{kit}.json"), {
            "type": "minecraft:crafting_shaped",
            "category": "misc",
            "key": {**spec["keys"], "X": spec["kit_center"]},
            "pattern": spec["pattern"],
            "result": result(layout, f"{MOD_ID}:{kit}"),
        })
        # 2) direct upgrade: same ring around the previous chest
        write_json(os.path.join(out, f"{name}.json"), {
            "type": "minecraft:crafting_shaped",
            "category": "misc",
            "key": {**spec["keys"], "X": spec["direct_center"]},
            "pattern": spec["pattern"],
            "result": result(layout, f"{MOD_ID}:{name}"),
        })
        # 3) kit + previous chest (shapeless)
        write_json(os.path.join(out, f"{name}_from_upgrade.json"), {
            "type": "minecraft:crafting_shapeless",
            "category": "misc",
            "ingredients": [{"item": f"{MOD_ID}:{kit}"}, spec["direct_center"]],
            "result": result(layout, f"{MOD_ID}:{name}"),
        })


def item_criterion(layout, ingredient):
    """inventory_changed criterion for one recipe ingredient ({"item": id} / {"tag": id}), in the version's vanilla shape."""
    if "tag" in ingredient:
        tag = ingredient["tag"]
        crit = "has_" + tag.split(":", 1)[1].replace("/", "_")
        predicate = {"items": "#" + tag} if layout.modern else {"tag": tag}
    else:
        item = ingredient["item"]
        crit = "has_" + item.split(":", 1)[1].replace("/", "_")
        predicate = {"items": item} if layout.modern else {"items": [item]}
    return crit, {"conditions": {"items": [predicate]}, "trigger": "minecraft:inventory_changed"}


def recipe_advancement(layout, recipe_id, ingredients):
    """Same shape vanilla uses for its own recipe unlocks: child of recipes/root, any has_<ingredient> OR has_the_recipe
    grants the recipe. Not a visible advancement (no display)."""
    criteria = {}
    for ingredient in ingredients:
        crit, body = item_criterion(layout, ingredient)
        criteria[crit] = body
    criteria["has_the_recipe"] = {"conditions": {"recipe": recipe_id}, "trigger": "minecraft:recipe_unlocked"}
    adv = {
        "parent": "minecraft:recipes/root",
        "criteria": criteria,
        "requirements": [list(criteria)],
        "rewards": {"recipes": [recipe_id]},
    }
    if not layout.modern:
        adv["sends_telemetry_event"] = False  # 1.20.1 vanilla emits it explicitly
    return adv


def gen_advancements(res, layout):
    out = os.path.join(res, "data", MOD_ID, layout.advancement_dir, "recipes", "misc")
    for tier in TIERS:
        spec = RECIPES[tier["name"]]
        name = block_name(tier)
        kit = kit_name(tier)
        # kit: tier material or the material it is wrapped around
        write_json(os.path.join(out, f"{kit}.json"),
                   recipe_advancement(layout, f"{MOD_ID}:{kit}", [spec["material"], spec["kit_center"]]))
        # direct upgrade: tier material or the previous-tier chest
        write_json(os.path.join(out, f"{name}.json"),
                   recipe_advancement(layout, f"{MOD_ID}:{name}", [spec["material"], spec["direct_center"]]))
        # kit + previous chest: the kit or the previous-tier chest
        write_json(os.path.join(out, f"{name}_from_upgrade.json"),
                   recipe_advancement(layout, f"{MOD_ID}:{name}_from_upgrade",
                                      [{"item": f"{MOD_ID}:{kit}"}, spec["direct_center"]]))


def gen_tags(res, layout):
    blocks = [f"{MOD_ID}:{block_name(t)}" for t in TIERS]
    write_json(os.path.join(res, "data", "minecraft", layout.block_tag_dir, "mineable", "pickaxe.json"),
               {"replace": False, "values": blocks})
    for ns in ["c"] + layout.extra_tag_namespaces:
        write_json(os.path.join(res, "data", ns, layout.block_tag_dir, "chests.json"), {"replace": False, "values": blocks})
        write_json(os.path.join(res, "data", ns, layout.item_tag_dir, "chests.json"), {"replace": False, "values": blocks})


# ---------------------------------------------------------------- placeholder textures

def write_png(path, width, height, pixels):
    """pixels: list of rows, each a list of (r, g, b, a)."""
    raw = b"".join(b"\x00" + bytes(c for px in row for c in px) for row in pixels)

    def chunk(tag, data):
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def canvas(width, height):
    return [[(0, 0, 0, 0) for _ in range(width)] for _ in range(height)]


def rect(img, x0, y0, x1, y1, rgb, outline=None):
    """Fills [x0, x1) x [y0, y1); optional 1 px outline colour."""
    for y in range(y0, y1):
        for x in range(x0, x1):
            edge = outline is not None and (x in (x0, x1 - 1) or y in (y0, y1 - 1))
            img[y][x] = (*(outline if edge else rgb), 255)


def draw_box_uv(img, u, v, dx, dy, dz, rgb):
    """Standard Minecraft box UV: top/bottom in the first dz rows, then the four sides."""
    base, light, dark = rgb
    rect(img, u + dz, v, u + dz + dx, v + dz, light, dark)                    # top
    rect(img, u + dz + dx, v, u + dz + 2 * dx, v + dz, dark, dark)            # bottom
    y0, y1 = v + dz, v + dz + dy
    rect(img, u, y0, u + dz, y1, base, dark)                                   # west
    rect(img, u + dz, y0, u + dz + dx, y1, base, dark)                        # north (front)
    rect(img, u + dz + dx, y0, u + 2 * dz + dx, y1, base, dark)               # east
    rect(img, u + 2 * dz + dx, y0, u + 2 * dz + 2 * dx, y1, base, dark)       # south (back)


def chest_entity_texture(rgb):
    img = canvas(64, 64)
    draw_box_uv(img, 0, 0, 14, 5, 14, rgb)      # lid   (texOffs 0,0  box 14x5x14)
    draw_box_uv(img, 0, 19, 14, 10, 14, rgb)    # base  (texOffs 0,19 box 14x10x14)
    draw_box_uv(img, 0, 0, 2, 4, 1, LOCK_RGB)   # lock  (texOffs 0,0  box 2x4x1)
    return img


def chest_item_sprite(rgb):
    base, light, dark = rgb
    img = canvas(16, 16)
    rect(img, 1, 6, 15, 15, base, dark)     # body
    rect(img, 1, 2, 15, 7, light, dark)     # lid
    rect(img, 7, 5, 9, 8, LOCK_RGB[0], LOCK_RGB[2])
    return img


def kit_item_sprite(rgb):
    base, light, dark = rgb
    img = canvas(16, 16)
    rect(img, 2, 2, 14, 14, base, dark)
    white = (250, 250, 250)
    rect(img, 7, 6, 9, 12, white)                       # arrow shaft
    for i, half in enumerate((3, 2, 1)):                # arrow head, widest at the bottom
        rect(img, 8 - half, 3 + i, 8 + half, 4 + i, white)
    _ = light
    return img


def mod_icon():
    img = canvas(64, 64)
    rect(img, 0, 0, 64, 64, (31, 42, 51))                       # slate tile
    rect(img, 10, 26, 54, 52, (95, 225, 215), (55, 150, 145))   # diamond chest body
    rect(img, 10, 16, 54, 30, (170, 245, 240), (55, 150, 145))  # lid
    rect(img, 30, 24, 34, 32, (120, 120, 120), (70, 70, 70))    # lock
    rect(img, 16, 54, 48, 60, (227, 179, 65))                   # gold pill
    return img


def gen_textures(res, force):
    assets = os.path.join(res, "assets", MOD_ID)
    jobs = []
    for tier in TIERS:
        jobs.append((os.path.join(assets, "textures", "entity", "chest", f"{tier['name']}.png"), chest_entity_texture, tier["rgb"]))
        jobs.append((os.path.join(assets, "textures", "item", f"{block_name(tier)}.png"), chest_item_sprite, tier["rgb"]))
        jobs.append((os.path.join(assets, "textures", "item", f"{kit_name(tier)}.png"), kit_item_sprite, tier["rgb"]))
    jobs.append((os.path.join(assets, "icon.png"), lambda _rgb: mod_icon(), None))
    written = 0
    for path, painter, rgb in jobs:
        if os.path.exists(path) and not force:
            continue
        img = painter(rgb)
        write_png(path, len(img[0]), len(img), img)
        written += 1
    return written, len(jobs)


# ---------------------------------------------------------------- main

def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--mc", required=True, choices=["1.21.1", "1.20.1"])
    parser.add_argument("--root", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
    parser.add_argument("--force-textures", action="store_true", help="overwrite existing PNGs with placeholders")
    args = parser.parse_args()

    res = os.path.normpath(os.path.join(args.root, "common", "src", "main", "resources"))
    layout = Layout(args.mc)
    gen_lang(res)
    gen_models(res)
    gen_loot(res, layout)
    gen_recipes(res, layout)
    gen_advancements(res, layout)
    gen_tags(res, layout)
    written, total = gen_textures(res, args.force_textures)
    print(f"resources generated for MC {args.mc} in {res} (textures written: {written}/{total})")


if __name__ == "__main__":
    main()
