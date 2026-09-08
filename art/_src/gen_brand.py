"""
Tiered Iron Chests - brand art (icon 512 / icon-64 / banner 1920x1080).
Pipeline: draw at 2x supersample -> LANCZOS downsample. Constants reverse-engineered from
mods #1/#2 (bulk-trades, quickstack-nearby) reference PNGs:

  Tile 512: rim #333B4E 12px, outer radius 88, checker 64px (#1A1E29 at even cells, #1F2431 odd)
  Gold pill: ring #3A2508 (9px), gold #FFC531, text #2B1A06 (Arial Black), bottom-right (outer 330,372)-(482,458)
  icon-64 = LANCZOS(icon 512)
  Banner: checker 120px same colours; left panel polygon (0,0)(835,0)(695,1080)(0,1080) in accent-dark;
          10px accent stripe on the diagonal; tile 576px at (131,251) with hard shadow;
          title Arial Black 144 white + accent, stroke #0A0D14 10px round; subtitle Segoe UI Bold #D7DEE9; x=840
"""
from PIL import Image, ImageDraw, ImageFont
import os

ART = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
S = 2  # supersample factor

# ---------------------------------------------------------------- brand constants
RIM, RADIUS, RIM_W = "#333B4E", 88, 12
CHK_A, CHK_B = "#1A1E29", "#1F2431"
PILL_RING, PILL_GOLD, PILL_TEXT = "#3A2508", "#FFC531", "#2B1A06"
STROKE = "#0A0D14"
SUBTITLE_COL = "#D7DEE9"
GHOST_A, GHOST_B = "#20283A", "#2A3448"
ACCENT = "#F0965A"          # copper accent for this mod (green = #1, cyan = #2)
PANEL = "#2A1E18"           # accent-dark left panel
PANEL_SHADOW = "#150F0C"

# diamond-tier hero palette (icon scale)
DIA = dict(O="#0E4A4E", D="#2FA9A6", M="#55D8D2", L="#8CEEE8", H="#D2FFFB", T="#B9FBF6")
TIER_COLS = ["#C06A42", "#BDBDC2", "#EABB2C", "#55D8D2", "#3E3535"]  # copper..netherite (bottom->top)

FONT_TITLE = "C:/Windows/Fonts/ariblk.ttf"
FONT_SUB = "C:/Windows/Fonts/segoeuib.ttf"

def k(v):  # scale a coordinate to supersampled space
    return v * S

def box(x0, y0, x1, y1):
    return [k(x0), k(y0), k(x1), k(y1)]

def pts(seq):
    return [(k(x), k(y)) for (x, y) in seq]

# ---------------------------------------------------------------- pieces
def checker(size_w, size_h, cell):
    im = Image.new("RGBA", (k(size_w), k(size_h)), CHK_A)
    d = ImageDraw.Draw(im)
    for cy in range(0, size_h // cell + 1):
        for cx in range(0, size_w // cell + 1):
            if (cx + cy) % 2 == 1:
                d.rectangle(box(cx*cell, cy*cell, (cx+1)*cell, (cy+1)*cell), fill=CHK_B)
    return im

def tile_mask(size=512, radius=RADIUS, inset=0):
    m = Image.new("L", (k(size), k(size)), 0)
    ImageDraw.Draw(m).rounded_rectangle(box(inset, inset, size-inset, size-inset), radius=k(radius), fill=255)
    return m

def poly_outlined(d, seq, fill, outline, w):
    """Polygon with an outward-expanded dark outline (drawn as a wide centred stroke first)."""
    d.polygon(pts(seq), fill=outline, outline=outline, width=k(w*2))
    d.polygon(pts(seq), fill=fill)

def hero_chest(d, x0=140, y0=178, x1=456, y1=446, dx=28, dy=-24, P=DIA):
    """Front-facing 2.5D chest: front rect + top/right parallelograms, 10px outline, latch in brand gold."""
    ow = 10
    top = [(x0, y0), (x0+dx, y0+dy), (x1+dx, y0+dy), (x1, y0)]
    right = [(x1, y0), (x1+dx, y0+dy), (x1+dx, y1+dy), (x1, y1)]
    # outline silhouette
    d.polygon(pts(top), fill=P["O"], outline=P["O"], width=k(ow*2))
    d.polygon(pts(right), fill=P["O"], outline=P["O"], width=k(ow*2))
    d.rounded_rectangle(box(x0-ow, y0-ow, x1+ow, y1+ow), radius=k(22), fill=P["O"])
    # faces
    d.polygon(pts(top), fill=P["T"])
    d.polygon(pts(right), fill=P["D"])
    d.rounded_rectangle(box(x0, y0, x1, y1), radius=k(12), fill=P["M"])
    # edge lines between faces
    d.line(pts([(x0, y0), (x1, y0), (x1, y1)]), fill=P["O"], width=k(ow), joint="curve")
    d.line(pts([(x1, y0), (x1+dx, y0+dy)]), fill=P["O"], width=k(ow))
    # lid (5/14 of height) + seam
    lid_h = round((y1 - y0) * 5 / 14)
    seam = y0 + lid_h
    d.rectangle(box(x0, y0, x1, seam), fill=P["L"])
    d.rounded_rectangle(box(x0, y0, x1, y0 + 34), radius=k(12), fill=P["H"])     # lid top highlight band
    d.rectangle(box(x0, y0 + 24, x1, y0 + 34), fill=P["H"])
    d.rectangle(box(x0, seam - 5, x1, seam + 5), fill=P["O"])                    # seam
    # base panel with bevel
    bx0, by0, bx1, by1 = x0 + 30, seam + 30, x1 - 30, y1 - 30
    d.rounded_rectangle(box(bx0 + 6, by0 + 6, bx1 + 6, by1 + 6), radius=k(8), fill=P["D"])
    d.rounded_rectangle(box(bx0, by0, bx1, by1), radius=k(8), fill=P["L"])
    d.rounded_rectangle(box(bx0, by0, bx1, by0 + 8), radius=k(4), fill=P["H"])
    d.rectangle(box(bx0, by0, bx0 + 8, by1 - 8), fill=P["H"])
    # rivets
    for (rx, ry) in [(x0+15, seam+15), (x1-15, seam+15), (x0+15, y1-15), (x1-15, y1-15)]:
        d.ellipse(box(rx-6, ry-6, rx+6, ry+6), fill=P["D"])
        d.ellipse(box(rx-6, ry-6, rx+2, ry+2), fill=P["O"])
    # latch (brand gold)
    cx = (x0 + x1) // 2
    lx0, ly0, lx1, ly1 = cx - 24, seam - 38, cx + 24, seam + 34
    d.rounded_rectangle(box(lx0 - 8, ly0 - 8, lx1 + 8, ly1 + 8), radius=k(14), fill=PILL_RING)
    d.rounded_rectangle(box(lx0, ly0, lx1, ly1), radius=k(8), fill=PILL_GOLD)
    d.rounded_rectangle(box(lx0, ly0, lx1, ly0 + 16), radius=k(6), fill="#FFE27A")
    d.rounded_rectangle(box(cx - 7, seam - 2, cx + 7, seam + 18), radius=k(3), fill=PILL_RING)

def tier_ladder(d, x=40, y_bottom=430, size=42, gap=10):
    """5 tier squares stacked bottom(copper)->top(netherite), dark outline."""
    for i, col in enumerate(TIER_COLS):
        y1 = y_bottom - i * (size + gap); y0 = y1 - size
        d.rounded_rectangle(box(x - 5, y0 - 5, x + size + 5, y1 + 5), radius=k(11), fill=STROKE)
        d.rounded_rectangle(box(x, y0, x + size, y1), radius=k(7), fill=col)
        d.rounded_rectangle(box(x + 6, y0 + 6, x + size - 6, y0 + 14), radius=k(3), fill="#FFFFFF" if i != 1 else "#F4F4F6")
    return y_bottom - 5 * (size + gap) + gap  # top y of ladder

def chevrons(d, cx=61, top=64, n=2, w=46, h=26, step=30, colour=PILL_GOLD):
    """Small up-chevrons (tier-up arrows) with dark outline, stacked."""
    for i in range(n):
        y = top + i * step
        seq = [(cx - w//2, y + h), (cx, y), (cx + w//2, y + h)]
        d.line(pts(seq), fill=PILL_RING, width=k(24), joint="curve")
    for i in range(n):
        y = top + i * step
        seq = [(cx - w//2, y + h), (cx, y), (cx + w//2, y + h)]
        d.line(pts(seq), fill=colour, width=k(12), joint="curve")

def gold_pill(d, text="5", x0=330, y0=372, x1=482, y1=458):
    d.rounded_rectangle(box(x0, y0, x1, y1), radius=k(24), fill=PILL_RING)
    d.rounded_rectangle(box(x0+9, y0+9, x1-9, y1-9), radius=k(15), fill=PILL_GOLD)
    f = ImageFont.truetype(FONT_TITLE, k(58))
    bb = f.getbbox(text)
    tw, th = bb[2]-bb[0], bb[3]-bb[1]
    cx, cy = k((x0+x1)/2), k((y0+y1)/2)
    d.text((cx - tw/2 - bb[0], cy - th/2 - bb[1]), text, font=f, fill=PILL_TEXT)

def icon_composite(size=512):
    """Returns supersampled RGBA icon (size*S)."""
    base = checker(size, size, 64)
    rim = Image.new("RGBA", (k(size), k(size)), (0,0,0,0))
    ImageDraw.Draw(rim).rounded_rectangle(box(0,0,size,size), radius=k(RADIUS), fill=RIM)
    inner = tile_mask(size, RADIUS - RIM_W, RIM_W)
    out = Image.new("RGBA", (k(size), k(size)), (0,0,0,0))
    out.paste(rim, (0,0), tile_mask(size))
    out.paste(base, (0,0), inner)
    d = ImageDraw.Draw(out)
    tier_ladder(d)
    chevrons(d, cx=61, top=108)
    hero_chest(d)
    gold_pill(d, "5")
    return out

def down(im, w, h):
    return im.resize((w, h), Image.LANCZOS)

# ---------------------------------------------------------------- text helpers
def draw_text_stroke(d, xy, text, font, fill, stroke_w):
    d.text(xy, text, font=font, fill=fill, stroke_width=stroke_w, stroke_fill=STROKE)

def place_text(d, x_ink, cap_top, text, font, fill, stroke_w=0):
    """Position so that ink left edge == x_ink and glyph top (of 'H'-height) == cap_top (both in 1x coords)."""
    bb_h = font.getbbox("H")           # (l, t, r, b) at supersampled size
    bb = font.getbbox(text)
    x = k(x_ink) - bb[0]
    y = k(cap_top) - bb_h[1]
    draw_text_stroke(d, (x, y), text, font, fill, stroke_w) if stroke_w else d.text((x, y), text, font=font, fill=fill)
    return (bb[2] - bb[0]) / S  # ink width in 1x px

def ghost(d):
    """Faded hero echo top-right: big chest silhouette (clipped by canvas), lid band, seam, panel, latch."""
    x0, y0, x1, y1 = 1560, -90, 2060, 470
    d.rounded_rectangle(box(x0, y0, x1, y1), radius=k(60), fill=GHOST_A)
    seam = y0 + round((y1 - y0) * 5 / 14)
    d.rectangle(box(x0, y0, x1, seam), fill=GHOST_B)
    d.rectangle(box(x0, seam - 10, x1, seam + 10), fill=CHK_A)
    d.rounded_rectangle(box(x0 + 60, seam + 60, x1 - 60, y1 - 60), radius=k(24), fill=GHOST_B)
    d.rounded_rectangle(box(1705, seam - 70, 1775, seam + 70), radius=k(18), fill=CHK_A)
    d.rounded_rectangle(box(1715, seam - 60, 1765, seam + 60), radius=k(12), fill=GHOST_B)

def banner(icon_ss):
    W, H = 1920, 1080
    out = checker(W, H, 120)
    d = ImageDraw.Draw(out)
    ghost(d)
    d.polygon(pts([(0,0), (835,0), (695,H), (0,H)]), fill=PANEL)
    d.polygon(pts([(835,0), (845,0), (705,H), (695,H)]), fill=ACCENT)
    # tile shadow + tile
    tile = down(icon_ss, k(576), k(576))
    mask = Image.new("L", tile.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, tile.size[0]-1, tile.size[1]-1], radius=k(88*576/512), fill=255)
    shadow = Image.new("RGBA", tile.size, PANEL_SHADOW)
    out.paste(shadow, (k(131+16), k(251+16)), mask)
    out.paste(tile, (k(131), k(251)), tile)
    # title
    f_title = ImageFont.truetype(FONT_TITLE, k(144))
    place_text(d, 840, 372, "Tiered", f_title, "#FFFFFF", stroke_w=k(10))
    place_text(d, 840, 514, "Iron Chests", f_title, ACCENT, stroke_w=k(10))
    # subtitle: largest size <= 60 whose ink ends at x <= 1810 (>=110px right margin)
    sub = "The classic chest progression, modernized."
    for sz in range(60, 39, -1):
        f_sub = ImageFont.truetype(FONT_SUB, k(sz))
        bb = f_sub.getbbox(sub)
        if 840 + (bb[2]-bb[0]) / S <= 1810: break
    place_text(d, 840, 652, sub, f_sub, SUBTITLE_COL)
    print("subtitle size", sz, "ink right edge", 840 + (bb[2]-bb[0]) / S)
    return down(out, W, H).convert("RGB")

if __name__ == "__main__":
    ss = icon_composite(512)
    icon = down(ss, 512, 512)
    icon.save(os.path.join(ART, "icon.png"))
    down(icon, 64, 64).save(os.path.join(ART, "icon-64.png"))
    banner(ss).save(os.path.join(ART, "banner.png"))
    print("brand done")
