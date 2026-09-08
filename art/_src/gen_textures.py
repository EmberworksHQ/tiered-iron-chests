"""
Tiered Iron Chests - pixel-art texture generator (native resolution, NO supersampling).

Outputs (art/):
  chest_<tier>.png       64x64 chest entity texture, vanilla single-chest UV layout
  item_chest_<tier>.png  16x16 item sprite (iso block-item look)
  kit_<tier>.png         16x16 upgrade kit sprite
  _preview/*.png         nearest-neighbour upscales + software-rendered iso previews (self-review only)

Vanilla single chest layer (ChestRenderer.createSingleBodyLayer, 64x64):
  bottom: texOffs(0,19) box(1,0,1, 14,10,14)
  lid   : texOffs(0,0)  box(1,0,0, 14,5,14)  offset(0,9,1)
  lock  : texOffs(0,0)  box(7,-2,14, 2,4,1)  offset(0,9,1)
ChestRenderer applies NO y-flip, so per ModelPart.Cube UV assignment:
  DOWN=(u+d,v) UP=(u+d+w,v) WEST=(u,v+d) NORTH=(u+d,v+d) EAST=(u+d+w,v+d) SOUTH=(u+2d+w,v+d)
  side faces land rotated 180 deg vs. an outside viewer (min-y -> top row, viewer-right -> left col);
  UP/DOWN land with the +z (front/latch) edge at the top row.
"""
from PIL import Image
import os, math

ART = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
PRE = os.path.join(ART, "_preview")
os.makedirs(PRE, exist_ok=True)

def hx(s, a=255):
    s = s.lstrip('#'); return (int(s[0:2],16), int(s[2:4],16), int(s[4:6],16), a)

# ---------------------------------------------------------------- palettes
# O outline, D dark, M mid, L light, H highlight ; latch: lO/lB/lH
TIERS = {
    "copper":    dict(O="#4A2313", D="#934A2B", M="#C06A42", L="#DA8B5E", H="#F0B088",
                      lO="#2C2C30", lB="#8E8E94", lH="#D6D6DA", panel_dark=False),
    "iron":      dict(O="#3E3E44", D="#8C8C92", M="#BDBDC2", L="#DADADE", H="#F4F4F6",
                      lO="#5A3A08", lB="#E9B92A", lH="#FFE27A", panel_dark=False),
    "gold":      dict(O="#6A4508", D="#C18C14", M="#EABB2C", L="#F6D44E", H="#FFF2A0",
                      lO="#2A2A2E", lB="#6E6E74", lH="#B0B0B6", panel_dark=False),
    "diamond":   dict(O="#125A60", D="#2FA9A6", M="#55D8D2", L="#8CEEE8", H="#D2FFFB",
                      lO="#5A3A08", lB="#E9B92A", lH="#FFE27A", panel_dark=False),
    "netherite": dict(O="#140F0E", D="#2A2222", M="#3E3535", L="#4B4141", H="#5C5050",
                      lO="#5A3A08", lB="#E9B92A", lH="#FFE27A", panel_dark=True),
}
# vanilla oak-plank palette: only used for the wood->copper kit plate (no wooden chest is shipped)
WOOD = dict(O="#3B2812", D="#7A5A31", M="#A07A47", L="#BC9862", H="#D4B37A",
            lO="#2C2C30", lB="#8E8E94", lH="#D6D6DA", panel_dark=False)

def face(w, h, fill):
    return Image.new("RGBA", (w, h), fill)

def put(im, x, y, c):
    if 0 <= x < im.width and 0 <= y < im.height: im.putpixel((x, y), c)

def rect(im, x0, y0, x1, y1, c):           # inclusive coords, outline only
    for x in range(x0, x1+1): put(im, x, y0, c); put(im, x, y1, c)
    for y in range(y0, y1+1): put(im, x0, y, c); put(im, x1, y, c)

def fill(im, x0, y0, x1, y1, c):
    for y in range(y0, y1+1):
        for x in range(x0, x1+1): put(im, x, y, c)

def panel(im, x0, y0, x1, y1, P):
    """Inset panel: fill + bevel (H top/left, D bottom/right). Netherite: darker recessed panel."""
    if P["panel_dark"]:
        fill(im, x0, y0, x1, y1, hx(P["D"]))
        for x in range(x0, x1+1): put(im, x, y0, hx(P["O"]))      # shadow top (recessed)
        for y in range(y0, y1+1): put(im, x0, y, hx(P["O"]))
        for x in range(x0, x1+1): put(im, x, y1, hx(P["L"]))      # lit bottom edge
        for y in range(y0, y1+1): put(im, x1, y, hx(P["L"]))
    else:
        fill(im, x0, y0, x1, y1, hx(P["L"]))
        for x in range(x0, x1+1): put(im, x, y0, hx(P["H"]))
        for y in range(y0, y1+1): put(im, x0, y, hx(P["H"]))
        for x in range(x0+1, x1+1): put(im, x, y1, hx(P["D"]))
        for y in range(y0+1, y1+1): put(im, x1, y, hx(P["D"]))
        put(im, x1, y0, hx(P["M"])); put(im, x0, y1, hx(P["M"]))

def rivets(im, pts, P):
    for (x, y) in pts: put(im, x, y, hx(P["D"] if not P["panel_dark"] else P["H"]))

# ------------------------------------------------- natural-orientation faces
def base_side(P):
    """14x10, natural view (row0 = top). Row0 hidden under the closed lid."""
    im = face(14, 10, hx(P["M"]))
    rect(im, 0, 0, 13, 9, hx(P["O"]))
    panel(im, 2, 2, 11, 7, P)
    rivets(im, [(1,1),(12,1),(1,8),(12,8)], P)
    for x in range(1, 13): put(im, x, 0, hx(P["D"]))   # seam shadow under lid
    return im

def lid_side(P, front=False):
    """14x5 natural view: row0 top edge outline, row4 = seam with base."""
    im = face(14, 5, hx(P["M"]))
    rect(im, 0, 0, 13, 4, hx(P["O"]))
    for x in range(1, 13): put(im, x, 1, hx(P["H"] if not P["panel_dark"] else P["L"]))
    for x in range(1, 13): put(im, x, 2, hx(P["L"]))
    rivets(im, [(1,3),(12,3)], P)
    return im

def lid_top(P):
    """14x14 natural map view (row0 = back edge, row13 = front/latch edge)."""
    im = face(14, 14, hx(P["M"]))
    rect(im, 0, 0, 13, 13, hx(P["O"]))
    panel(im, 2, 2, 11, 11, P)
    rivets(im, [(1,1),(12,1),(1,12),(12,12)], P)
    for x in (3,4,9,10): put(im, x, 1, hx(P["D"]))     # hinge stubs on back edge
    return im

def flat_dark(P, w=14, h=14):
    im = face(w, h, hx(P["D"]))
    rect(im, 0, 0, w-1, h-1, hx(P["O"]))
    return im

def base_top(P):
    """Inside floor (seen when open)."""
    im = face(14, 14, hx(P["D"]))
    rect(im, 0, 0, 13, 13, hx(P["O"]))
    rect(im, 1, 1, 12, 12, hx(P["M"]))
    return im

def base_bottom(P):
    im = face(14, 14, hx(P["M"]))
    rect(im, 0, 0, 13, 13, hx(P["O"]))
    rect(im, 2, 2, 11, 11, hx(P["D"]))
    return im

def latch_front(P):
    im = face(2, 4, hx(P["lB"]))
    put(im, 0, 0, hx(P["lH"])); put(im, 1, 0, hx(P["lH"]))
    put(im, 1, 2, hx(P["lO"]))                        # keyhole
    put(im, 0, 3, hx(P["lO"])); put(im, 1, 3, hx(P["lO"]))
    return im

def latch_side(P):   # 1x4
    im = face(1, 4, hx(P["lO"])); put(im, 0, 0, hx(P["lB"])); put(im, 0, 1, hx(P["lB"])); return im

def latch_top(P):    # 2x1
    return face(2, 1, hx(P["lH"]))

def latch_bottom(P):
    return face(2, 1, hx(P["lO"]))

def latch_back(P):
    return face(2, 4, hx(P["lO"]))

# ------------------------------------------------------------- atlas build
def rot180(im): return im.transpose(Image.ROTATE_180)
def vflip(im):  return im.transpose(Image.FLIP_TOP_BOTTOM)

def paste_box(atlas, u, v, w, h, d, faces):
    """faces: dict with natural-orientation images: top, bottom, west, north, east, south."""
    atlas.paste(vflip(faces["bottom"]), (u+d, v))            # DOWN
    atlas.paste(vflip(faces["top"]),    (u+d+w, v))          # UP
    atlas.paste(rot180(faces["west"]),  (u, v+d))            # WEST
    atlas.paste(rot180(faces["north"]), (u+d, v+d))          # NORTH (back)
    atlas.paste(rot180(faces["east"]),  (u+d+w, v+d))        # EAST
    atlas.paste(rot180(faces["south"]), (u+2*d+w, v+d))      # SOUTH (front, latch side)

def build_atlas(P):
    A = Image.new("RGBA", (64, 64), (0,0,0,0))
    lid = dict(top=lid_top(P), bottom=flat_dark(P), west=lid_side(P), north=lid_side(P),
               east=lid_side(P), south=lid_side(P, front=True))
    base = dict(top=base_top(P), bottom=base_bottom(P), west=base_side(P), north=base_side(P),
                east=base_side(P), south=base_side(P))
    lock = dict(top=latch_top(P), bottom=latch_bottom(P), west=latch_side(P), north=latch_back(P),
                east=latch_side(P), south=latch_front(P))
    paste_box(A, 0, 19, 14, 10, 14, base)
    paste_box(A, 0, 0, 14, 5, 14, lid)
    paste_box(A, 0, 0, 2, 4, 1, lock)
    return A

# ------------------------------------------------ UV sampler (for preview)
def uv_sampler(atlas, u, v, w, h, d):
    """fn(face, s, t) -> colour; (s,t) natural-view coords in [0,1):
       side faces: s = viewer-left->right, t = bottom->top ; top/bottom: s = west->east, t = front->back."""
    px = atlas.load()
    def sample(faceName, s, t):
        s = min(max(s, 0.0), 0.999999); t = min(max(t, 0.0), 0.999999)
        if faceName == "top":
            x = u + d + w + int(s*w); y = v + int(t*d)              # UP: top row = +z(front)
        elif faceName == "bottom":
            x = u + d + int(s*w); y = v + int(t*d)
        else:
            if faceName == "west":  ox, fw = u, d
            if faceName == "north": ox, fw = u+d, w
            if faceName == "east":  ox, fw = u+d+w, d
            if faceName == "south": ox, fw = u+2*d+w, w
            # 180 deg: viewer-left -> right col ; bottom(t=0) -> top row
            x = ox + fw - 1 - int(s*fw); y = v + d + int(t*h)
        return px[x, y]
    return sample

# ------------------------------------------------ iso software renderer
COS30, SIN30 = math.cos(math.radians(30)), 0.5

class Box:
    def __init__(self, x0, y0, z0, sx, sy, sz, sampler):
        self.x0,self.y0,self.z0,self.sx,self.sy,self.sz = x0,y0,z0,sx,sy,sz
        self.sampler = sampler

def shade_col(c, f):
    return (min(255,int(c[0]*f)), min(255,int(c[1]*f)), min(255,int(c[2]*f)), c[3])

def render_iso(boxes, size, scale, shade=(1.0, 0.85, 0.62), origin=None, bg=(0,0,0,0)):
    """Orthographic dimetric: +x recedes right-down, +z (front) recedes left-down. Visible: top, south(+z), east(+x)."""
    img = Image.new("RGBA", (size, size), bg)
    out = img.load()
    cx, cy = origin if origin else (size/2, size/2)
    for py in range(size):
        for pxi in range(size):
            sx, sy = pxi + 0.5, py + 0.5
            best = None   # (depth, colour)
            for b in boxes:
                # top face y = y0+sy
                Y = b.y0 + b.sy
                a = (sx - cx)/(COS30*scale); c = (sy - cy + Y*scale)/(SIN30*scale)
                X = (a + c)/2; Z = (c - a)/2
                if b.x0 <= X < b.x0+b.sx and b.z0 <= Z < b.z0+b.sz:
                    depth = -(X + Z) - Y*0  # nearer = larger X+Z ; use negative so smaller = nearer
                    depth = -(X + Z + Y)
                    col = shade_col(b.sampler("top", (X-b.x0)/b.sx, 1.0 - (Z-b.z0)/b.sz), shade[0])
                    if best is None or depth < best[0]: best = (depth, col)
                # south face z = z0+sz  (viewer left->right is +x)
                Zs = b.z0 + b.sz
                X = (sx - cx)/(COS30*scale) + Zs
                Yv = ((X + Zs)*SIN30*scale - (sy - cy))/scale
                if b.x0 <= X < b.x0+b.sx and b.y0 <= Yv < b.y0+b.sy:
                    depth = -(X + Zs + Yv)
                    col = shade_col(b.sampler("south", (X-b.x0)/b.sx, (Yv-b.y0)/b.sy), shade[1])
                    if best is None or depth < best[0]: best = (depth, col)
                # east face x = x0+sx (viewer left->right is -z, i.e. front(+z) on the left)
                Xe = b.x0 + b.sx
                Z = Xe - (sx - cx)/(COS30*scale)
                Yv = ((Xe + Z)*SIN30*scale - (sy - cy))/scale
                if b.z0 <= Z < b.z0+b.sz and b.y0 <= Yv < b.y0+b.sy:
                    depth = -(Xe + Z + Yv)
                    s = 1.0 - (Z-b.z0)/b.sz   # viewer-left = front(+z)
                    col = shade_col(b.sampler("east", s, (Yv-b.y0)/b.sy), shade[2])
                    if best is None or depth < best[0]: best = (depth, col)
            if best is not None and best[1][3] > 0:
                out[pxi, py] = best[1]
    return img

def preview_from_atlas(atlas, size=224):
    lid = uv_sampler(atlas, 0, 0, 14, 5, 14)
    base = uv_sampler(atlas, 0, 19, 14, 10, 14)
    lock = uv_sampler(atlas, 0, 0, 2, 4, 1)
    boxes = [Box(1,0,1, 14,10,14, base), Box(1,9,1, 14,5,14, lid), Box(7,7,15, 2,4,1, lock)]
    scale = size/30.0
    return render_iso(boxes, size, scale, origin=(size/2, size*0.62))

# ------------------------------------------------ 16x16 item sprite (front-facing 2.5D template, crisp)
def item_sprite(P):
    """14x14 front face (lid 5 rows / seam / base 8 rows) + 1px top strip and 1px right strip for depth."""
    S = 16
    im = Image.new("RGBA", (S, S), (0,0,0,0))
    O,D,M,L,H = (hx(P[k]) for k in "ODMLH")
    lO,lB,lH = hx(P["lO"]), hx(P["lB"]), hx(P["lH"])
    dark = P["panel_dark"]
    # depth strips
    for x in range(2, 15): put(im, x, 0, O)            # top strip outline
    for x in range(3, 14): put(im, x, 0, H if not dark else L)
    put(im, 15, 1, O)
    for y in range(1, 15): put(im, 15, y, O)
    for y in range(2, 14): put(im, 15, y, D if not dark else O)
    # body outline (1..14)
    rect(im, 1, 1, 14, 14, O)
    # lid rows 2..5
    for x in range(2, 14):
        put(im, x, 2, H if not dark else L)
        put(im, x, 3, L if not dark else M)
        put(im, x, 4, L if not dark else M)
        put(im, x, 5, M if not dark else D)
    # seam row 6
    for x in range(2, 14): put(im, x, 6, O)
    # base rows 7..13: frame M, panel with bevel
    fill(im, 2, 7, 13, 13, M)
    if dark:
        fill(im, 3, 8, 12, 12, D)
        for x in range(3, 13): put(im, x, 8, O); put(im, x, 12, L)
        for y in range(8, 13): put(im, 3, y, O); put(im, 12, y, L)
    else:
        fill(im, 3, 8, 12, 12, L)
        for x in range(3, 13): put(im, x, 8, H); put(im, x, 12, D)
        for y in range(8, 13): put(im, 3, y, H); put(im, 12, y, D)
        put(im, 12, 8, M); put(im, 3, 12, M)
    # rivets
    for (x, y) in [(2,7),(13,7),(2,13),(13,13)]: put(im, x, y, D if not dark else H)
    # latch (2 wide, hangs from lid over seam)
    for x in (7, 8):
        put(im, x, 5, lH); put(im, x, 6, lB); put(im, x, 7, lB)
    put(im, 8, 7, lO)
    for x in (7, 8): put(im, x, 8, lO)
    return im

# ------------------------------------------------ 16x16 upgrade kit sprite (hand template)
ARROW = [
"...ooo...",
"..oaAao..",
".oaaAaao.",
"oaaaAaaao",
"oooaAaooo",
"..oaAao..",
"..oaAao..",
"..oaAao..",
"..ooooo..",
]
def kit_sprite(P_src, P_dst):
    S = 16
    im = Image.new("RGBA", (S, S), (0,0,0,0))
    O,D,M,L,H = (hx(P_src[k]) for k in "ODMLH")
    for y in range(1, 15):
        for x in range(1, 15):
            corner = (x in (1,14) and y in (1,14))
            if corner: continue
            edge = x in (1,14) or y in (1,14) or ((x in (2,13)) and (y in (2,13)))
            if edge: c = O
            else:
                if y <= 6: c = L
                elif y <= 11: c = M
                else: c = D
                if (x == 2 or y == 2): c = H
                if (x == 13 or y == 13): c = D
            put(im, x, y, c)
    for (x, y) in [(3,3),(12,3),(3,12),(12,12)]:
        put(im, x, y, hx(P_src["D"] if not P_src["panel_dark"] else P_src["H"]))
    ao, aa, aA = hx(P_dst["O"]), hx(P_dst["L"]), hx(P_dst["H"])
    ox, oy = 4, 3
    for j, row in enumerate(ARROW):
        for i, ch in enumerate(row):
            if ch == 'o': put(im, ox+i, oy+j, ao)
            elif ch == 'a': put(im, ox+i, oy+j, aa)
            elif ch == 'A': put(im, ox+i, oy+j, aA)
    return im

# ------------------------------------------------------------------ main
def upscale(im, k): return im.resize((im.width*k, im.height*k), Image.NEAREST)

if __name__ == "__main__":
    order = ["copper","iron","gold","diamond","netherite"]
    for name in order:
        P = TIERS[name]
        atlas = build_atlas(P)
        atlas.save(os.path.join(ART, f"chest_{name}.png"))
        upscale(atlas, 8).save(os.path.join(PRE, f"chest_{name}_x8.png"))
        preview_from_atlas(atlas, 216).save(os.path.join(PRE, f"chest_{name}_iso.png"))
        spr = item_sprite(P); spr.save(os.path.join(ART, f"item_chest_{name}.png"))
        upscale(spr, 12).save(os.path.join(PRE, f"item_chest_{name}_x12.png"))
    kits = [("wood", "copper", WOOD)] + [(order[i], order[i+1], TIERS[order[i]]) for i in range(4)]
    for src, dst, Psrc in kits:
        k = kit_sprite(Psrc, TIERS[dst]); k.save(os.path.join(ART, f"kit_{src}.png"))
        upscale(k, 12).save(os.path.join(PRE, f"kit_{src}_x12.png"))
    # jar mirror: exact in-jar paths (matches ChestTier.entityTexture / blockName / kitName)
    JAR = os.path.join(ART, "jar", "assets", "tieredironchests", "textures")
    os.makedirs(os.path.join(JAR, "entity", "chest"), exist_ok=True)
    os.makedirs(os.path.join(JAR, "item"), exist_ok=True)
    import shutil
    for name in order:
        shutil.copyfile(os.path.join(ART, f"chest_{name}.png"), os.path.join(JAR, "entity", "chest", f"{name}.png"))
        shutil.copyfile(os.path.join(ART, f"item_chest_{name}.png"), os.path.join(JAR, "item", f"{name}_chest.png"))
    for src, dst, _ in kits:
        shutil.copyfile(os.path.join(ART, f"kit_{src}.png"), os.path.join(JAR, "item", f"{src}_to_{dst}_chest_upgrade.png"))
    # contact sheet: sprites in fake inventory slots + iso previews from the real atlases
    slot = Image.new("RGBA", (18*12, 18*12), (198,198,198,255))
    sheet = Image.new("RGBA", (18*12*10, 18*12 + 216), (139,139,139,255))
    files = [f"item_chest_{n}.png" for n in order] + [f"kit_{s}.png" for s, _, _ in kits]
    for i, f in enumerate(files):
        s = Image.open(os.path.join(ART, f)).convert("RGBA")
        cell = slot.copy(); cell.paste(upscale(s, 12), (12, 12), upscale(s, 12))
        sheet.paste(cell, (i*18*12, 0))
    for i, n in enumerate(order):
        p = Image.open(os.path.join(PRE, f"chest_{n}_iso.png")); sheet.paste(p, (i*216, 216), p)
    sheet.save(os.path.join(PRE, "contact_sheet.png"))
    print("done")
