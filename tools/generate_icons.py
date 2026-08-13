#!/usr/bin/env python3
"""Generate the Disco Timer logo and every launcher icon asset from one source.

Outputs:
  assets/logo.svg                                  master artwork (512x512)
  app/src/main/res/drawable/ic_launcher_background.xml
  app/src/main/res/drawable/ic_launcher_foreground.xml
  app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml, ic_launcher_round.xml
  app/src/main/res/mipmap-*/ic_launcher.png, ic_launcher_round.png   (API 24-25)

Run: python3 tools/generate_icons.py
"""

import math
import os
import random
import subprocess
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app/src/main/res")
CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"

BG_START, BG_END = "#8E51FF", "#E12AFB"
SPARKLE = "#F3E9FF"

# Mirror tile grid + lighting
N_LAT, N_LON, GAP, SEED = 15, 26, 0.055, 7
LIGHT = (-0.42, 0.62, 0.66)

# brightness ramp: shadow violet -> blue -> cyan -> blown-out mirror
RAMP = [
    (0.00, (0x4E, 0x35, 0x9E)),
    (0.22, (0x6B, 0x4F, 0xC4)),
    (0.42, (0x86, 0x7C, 0xDD)),
    (0.58, (0x5F, 0xB2, 0xEC)),
    (0.74, (0x8C, 0xD6, 0xF6)),
    (0.88, (0xC4, 0xEC, 0xFB)),
    (1.00, (0xFF, 0xFF, 0xFF)),
]

# glint placement, as a fraction of ball radius
GLINT_DX, GLINT_DY, GLINT_R = 0.246, 0.077, 0.289


def _ramp(t):
    t = max(0.0, min(1.0, t))
    for i in range(len(RAMP) - 1):
        t0, c0 = RAMP[i]
        t1, c1 = RAMP[i + 1]
        if t0 <= t <= t1:
            f = (t - t0) / (t1 - t0)
            return tuple(round(a + (b - a) * f) for a, b in zip(c0, c1))
    return RAMP[-1][1]


def tiles(cx, cy, r):
    """Mirror facets of a sphere under orthographic projection, front side only."""
    rng = random.Random(SEED)
    ln = math.sqrt(sum(c * c for c in LIGHT))
    light = tuple(c / ln for c in LIGHT)

    def point(lat, lon):
        return (math.cos(lat) * math.sin(lon), math.sin(lat), math.cos(lat) * math.cos(lon))

    out = []
    for i in range(N_LAT):
        lat0 = -math.pi / 2 + math.pi * i / N_LAT
        lat1 = -math.pi / 2 + math.pi * (i + 1) / N_LAT
        a0, a1 = lat0 + (lat1 - lat0) * GAP, lat1 - (lat1 - lat0) * GAP
        for j in range(N_LON):
            lon0 = 2 * math.pi * j / N_LON
            lon1 = 2 * math.pi * (j + 1) / N_LON
            b0, b1 = lon0 + (lon1 - lon0) * GAP, lon1 - (lon1 - lon0) * GAP

            normal = point((lat0 + lat1) / 2, (lon0 + lon1) / 2)
            if normal[2] <= 0.06:  # back-facing, or grazing the limb
                continue

            diffuse = max(0.0, sum(a * b for a, b in zip(normal, light)))
            t = 0.12 + 0.78 * diffuse + 0.35 * diffuse ** 6
            t += rng.uniform(-0.09, 0.09)
            if rng.random() < 0.10 + 0.22 * diffuse:  # mirrors catching the light
                t = max(t, rng.uniform(0.90, 1.0))

            corners = [point(a0, b0), point(a0, b1), point(a1, b1), point(a1, b0)]
            xy = [(cx + r * p[0], cy - r * p[1]) for p in corners]
            out.append((xy, "#%02X%02X%02X" % _ramp(t)))
    return out


def star(cx, cy, r, waist=0.2):
    """Four-point sparkle."""
    w = r * waist
    pts = [(cx, cy - r), (cx + w, cy - w), (cx + r, cy), (cx + w, cy + w),
           (cx, cy + r), (cx - w, cy + w), (cx - r, cy), (cx - w, cy - w)]
    return "M" + " L".join("%.1f %.1f" % p for p in pts) + " Z"


def glint(cx, cy, r):
    return star(cx + r * GLINT_DX, cy + r * GLINT_DY, r * GLINT_R)


# --------------------------------------------------------------------------- SVG

def ball_svg(cx, cy, r, indent="    "):
    body = ['%s<circle cx="%g" cy="%g" r="%g" fill="url(#seam)"/>' % (indent, cx, cy, r),
            '%s<g clip-path="url(#ballClip)">' % indent]
    for xy, col in tiles(cx, cy, r):
        pts = " ".join("%.1f,%.1f" % p for p in xy)
        body.append('%s  <polygon points="%s" fill="%s"/>' % (indent, pts, col))
    body.append("%s</g>" % indent)
    body.append('%s<circle cx="%g" cy="%g" r="%g" fill="none" stroke="#5FC4EC" '
                'stroke-width="3" opacity="0.55"/>' % (indent, cx, cy, r))
    return "\n".join(body)


def master_svg(clip=None):
    """clip: None (rounded square), or 'circle'."""
    cx, cy, r = 256, 262, 142
    shape = ('<circle cx="256" cy="256" r="256" fill="url(#bg)"/>' if clip == "circle"
             else '<rect width="512" height="512" rx="96" fill="url(#bg)"/>')
    frame = ('<circle cx="256" cy="256" r="256"/>' if clip == "circle"
             else '<rect width="512" height="512" rx="96"/>')
    sparkles = "\n".join(
        '    <path d="%s" opacity="%s"/>' % (star(x, y, s), o)
        for x, y, s, o in [(112, 147, 55, 0.95), (386, 147, 35, 0.9),
                           (74, 374, 40, 0.9), (146, 441, 29, 0.85), (410, 408, 40, 0.9)])
    return '''<svg width="512" height="512" viewBox="0 0 512 512" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bg" x1="0%%" y1="0%%" x2="100%%" y2="100%%">
      <stop offset="0%%" stop-color="{start}"/>
      <stop offset="100%%" stop-color="{end}"/>
    </linearGradient>

    <!-- shows through the gaps between mirrors -->
    <radialGradient id="seam" cx="36%%" cy="30%%" r="78%%">
      <stop offset="0%%" stop-color="#9FE4FA"/>
      <stop offset="55%%" stop-color="#4FA6DC"/>
      <stop offset="100%%" stop-color="#3B2A78"/>
    </radialGradient>

    <radialGradient id="glow" cx="50%%" cy="50%%" r="50%%">
      <stop offset="0%%" stop-color="#BDF0FF" stop-opacity="0.55"/>
      <stop offset="100%%" stop-color="#BDF0FF" stop-opacity="0"/>
    </radialGradient>

    <clipPath id="ballClip"><circle cx="{cx}" cy="{cy}" r="{r}"/></clipPath>
    <clipPath id="frame">{frame}</clipPath>
  </defs>

  <g clip-path="url(#frame)">
  {shape}

  <!-- Sparkles -->
  <g fill="{sparkle}">
{sparkles}
  </g>

  <circle cx="{cx}" cy="{cy}" r="190" fill="url(#glow)"/>

  <!-- Disco ball -->
{ball}

  <path d="{glint}" fill="#FFFFFF" opacity="0.97"/>
  </g>
</svg>
'''.replace("{start}", BG_START).replace("{end}", BG_END).replace("{sparkle}", SPARKLE) \
   .replace("{cx}", str(cx)).replace("{cy}", str(cy)).replace("{r}", str(r)) \
   .replace("{shape}", shape).replace("{frame}", frame).replace("{sparkles}", sparkles) \
   .replace("{ball}", ball_svg(cx, cy, r, "  ")).replace("{glint}", glint(cx, cy, r)) % ()


# ------------------------------------------------------------- VectorDrawable

VD_HEAD = ('<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
           '    xmlns:aapt="http://schemas.android.com/aapt"\n'
           '    android:width="108dp"\n'
           '    android:height="108dp"\n'
           '    android:viewportWidth="108"\n'
           '    android:viewportHeight="108">\n')


def background_vd():
    return VD_HEAD + '''    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="linear"
                android:startX="0" android:startY="0"
                android:endX="108" android:endY="108">
                <item android:offset="0" android:color="%s"/>
                <item android:offset="1" android:color="%s"/>
            </gradient>
        </aapt:attr>
    </path>
</vector>
''' % (BG_START, BG_END)


def foreground_vd():
    # Adaptive icons show the middle 72dp of 108dp; keep everything inside the
    # 66dp guaranteed-safe circle (radius 33 from centre).
    cx, cy, r = 54.0, 53.0, 24.0
    left, top, size = cx - r, cy - r, r * 2

    out = [VD_HEAD]

    # halo
    out.append('''    <path android:pathData="M54,53m-46,0a46,46 0,1 1,92 0a46,46 0,1 1,-92 0">
        <aapt:attr name="android:fillColor">
            <gradient android:type="radial"
                android:centerX="54" android:centerY="53" android:gradientRadius="46">
                <item android:offset="0" android:color="#8CBDF0FF"/>
                <item android:offset="1" android:color="#00BDF0FF"/>
            </gradient>
        </aapt:attr>
    </path>\n''')

    # sparkles, tucked between the ball edge and the safe circle
    for sx, sy, ss, alpha in [(33.0, 32.0, 5.0, "F2"), (75.0, 34.0, 4.0, "E6"),
                              (32.0, 74.0, 4.5, "E6"), (75.0, 74.0, 4.0, "E6")]:
        out.append('    <path android:pathData="%s" android:fillColor="#%s%s"/>\n'
                   % (star(sx, sy, ss), alpha, SPARKLE.lstrip("#")))

    # seam layer behind the mirrors
    out.append('''    <path android:pathData="M%g,%gm-%g,0a%g,%g 0,1 1,%g 0a%g,%g 0,1 1,-%g 0">
        <aapt:attr name="android:fillColor">
            <gradient android:type="radial"
                android:centerX="%.2f" android:centerY="%.2f" android:gradientRadius="%.2f">
                <item android:offset="0" android:color="#9FE4FA"/>
                <item android:offset="0.55" android:color="#4FA6DC"/>
                <item android:offset="1" android:color="#3B2A78"/>
            </gradient>
        </aapt:attr>
    </path>\n''' % (cx, cy, r, r, r, r * 2, r, r, r * 2,
                    left + 0.36 * size, top + 0.30 * size, 0.78 * size))

    # mirror facets
    for xy, col in tiles(cx, cy, r):
        d = "M" + " L".join("%.2f %.2f" % p for p in xy) + " Z"
        out.append('    <path android:pathData="%s" android:fillColor="%s"/>\n' % (d, col))

    out.append('    <path android:pathData="M%g,%gm-%g,0a%g,%g 0,1 1,%g 0a%g,%g 0,1 1,-%g 0" '
               'android:strokeColor="#5FC4EC" android:strokeWidth="0.5" '
               'android:strokeAlpha="0.55"/>\n'
               % (cx, cy, r, r, r, r * 2, r, r, r * 2))
    out.append('    <path android:pathData="%s" android:fillColor="#F7FFFFFF"/>\n'
               % glint(cx, cy, r))
    out.append("</vector>\n")
    return "".join(out)


ADAPTIVE = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
'''


# -------------------------------------------------------------------- raster

DENSITIES = [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]
SUPERSAMPLE = 4


def render_png(svg, out_path, size):
    """Rasterise SVG at 4x through headless Chrome, then downsample with sips."""
    with tempfile.TemporaryDirectory() as tmp:
        big = size * SUPERSAMPLE
        src = os.path.join(tmp, "icon.svg")
        with open(src, "w") as fh:
            fh.write(svg.replace('width="512" height="512"',
                                 'width="%d" height="%d"' % (big, big), 1))
        subprocess.run(
            [CHROME, "--headless", "--disable-gpu", "--hide-scrollbars",
             "--force-device-scale-factor=1",
             "--default-background-color=00000000",
             "--screenshot=%s" % os.path.join(tmp, "out.png"),
             "--window-size=%d,%d" % (big, big), "file://" + src],
            check=True, capture_output=True)
        os.makedirs(os.path.dirname(out_path), exist_ok=True)
        subprocess.run(["sips", "-z", str(size), str(size), os.path.join(tmp, "out.png"),
                        "--out", out_path], check=True, capture_output=True)


def write(path, content):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as fh:
        fh.write(content)
    print("  " + os.path.relpath(path, ROOT))


def main():
    print("master artwork")
    write(os.path.join(ROOT, "assets/logo.svg"), master_svg())

    print("adaptive icon (API 26+)")
    write(os.path.join(RES, "drawable/ic_launcher_background.xml"), background_vd())
    write(os.path.join(RES, "drawable/ic_launcher_foreground.xml"), foreground_vd())
    write(os.path.join(RES, "mipmap-anydpi-v26/ic_launcher.xml"), ADAPTIVE)
    write(os.path.join(RES, "mipmap-anydpi-v26/ic_launcher_round.xml"), ADAPTIVE)

    print("legacy icons (API 24-25)")
    square, round_ = master_svg(), master_svg(clip="circle")
    for name, size in DENSITIES:
        render_png(square, os.path.join(RES, "mipmap-%s/ic_launcher.png" % name), size)
        print("  mipmap-%s/ic_launcher.png (%dpx)" % (name, size))
        render_png(round_, os.path.join(RES, "mipmap-%s/ic_launcher_round.png" % name), size)
        print("  mipmap-%s/ic_launcher_round.png (%dpx)" % (name, size))

    print("\nPlay Store icon: assets/logo.svg -> export at 512x512")


if __name__ == "__main__":
    main()
