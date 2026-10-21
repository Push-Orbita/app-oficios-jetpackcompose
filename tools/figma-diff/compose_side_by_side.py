#!/usr/bin/env python3
"""Side-by-side Figma vs Roborazzi + real-dp diff para docs/qa-1a1/.

Uso:
    python tools/figma-diff/compose_side_by_side.py \
        --figma docs/qa-1a1/sources/home-trabajador-figma.png \
        --app path/to/roborazzi.png \
        --out docs/qa-1a1/home-trabajador.png \
        --name "home-trabajador"
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont

from measure_auth_boxes import (
    FIGMA_SIZE,
    detect_density,
    is_outline,
    is_white,
    load_capture,
    not_navy,
)
from measure_search_boxes import pixel_diff_pct

TARGET_W, TARGET_H = FIGMA_SIZE
STATUS = 48
NAV = 24
GUTTER = 16
HEADER = 56
LABEL_H = 28


def _font(size: int) -> ImageFont.ImageFont:
    for path in (
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    ):
        try:
            return ImageFont.truetype(path, size)
        except OSError:
            continue
    return ImageFont.load_default()


def normalize(path: Path) -> Image.Image:
    img = Image.open(path).convert("RGB")
    if img.size != (TARGET_W, TARGET_H):
        img = img.resize((TARGET_W, TARGET_H), Image.Resampling.LANCZOS)
    return img


def overlay_diff(figma: Image.Image, app: Image.Image) -> tuple[Image.Image, float]:
    diff = ImageChops.difference(figma, app).convert("L")
    pixels = list(diff.getdata())
    different = sum(1 for p in pixels if p > 10)
    pct = (different / len(pixels)) * 100.0
    mask = diff.point(lambda p: 255 if p > 10 else 0)
    colored = app.copy()
    red = Image.new("RGB", app.size, (220, 32, 32))
    colored.paste(red, mask=mask)
    return colored, pct


def content_diff_pct(figma: Image.Image, app: Image.Image) -> float:
    crop = (0, STATUS, TARGET_W, TARGET_H - NAV)
    f = figma.crop(crop)
    a = app.crop(crop)
    _, pct = overlay_diff(f, a)
    return pct


def measure_home_trabajador(path: Path, *, figma: bool) -> dict[str, tuple[float, float, float, float] | None]:
    """Boxes en dp: (left, top, right, bottom)."""
    arr, density, width_dp = load_capture(path, figma=figma)
    white = is_white(arr)
    outline = is_outline(arr)
    content = not_navy(arr)

    def first_white_row(top: float, bottom: float) -> float | None:
        t = int(round(top * density))
        b = int(round(bottom * density))
        t = max(0, min(arr.shape[0] - 1, t))
        b = max(t + 1, min(arr.shape[0], b))
        rows = white[t:b].any(axis=1)
        hits = rows.nonzero()[0]
        if hits.size == 0:
            return None
        return (t + int(hits[0])) / density

    def span_outline(top: float, bottom: float) -> tuple[float, float, float, float] | None:
        t = int(round(top * density))
        b = int(round(bottom * density))
        l = int(round(28 * density))
        r = int(round(min(412, width_dp) * density))
        t = max(0, min(arr.shape[0] - 1, t))
        b = max(t + 1, min(arr.shape[0], b))
        l = max(0, min(arr.shape[1] - 1, l))
        r = max(l + 1, min(arr.shape[1], r))
        region = outline[t:b, l:r]
        ys, xs = region.nonzero()
        if ys.size == 0:
            region = content[t:b, l:r]
            ys, xs = region.nonzero()
            if ys.size == 0:
                return None
        return (
            (l + int(xs.min())) / density,
            (t + int(ys.min())) / density,
            (l + int(xs.max())) / density,
            (t + int(ys.max())) / density,
        )

    return {
        "profile_title": first_white_row(48, 220),
        "progress": span_outline(140, 280),
        "milestones": span_outline(220, 430),
        "agenda_title": first_white_row(400, 560),
        "first_row": span_outline(460, 620),
        "bottom_nav": span_outline(820, 960),
    }


def dp_delta(a, b) -> str:
    if a is None or b is None:
        return "n/a"
    if isinstance(a, tuple) and isinstance(b, tuple):
        diffs = [abs(x - y) for x, y in zip(a, b)]
        return " ".join(f"{d:.1f}" for d in diffs)
    return f"{abs(float(a) - float(b)):.1f}"


def compose(figma: Image.Image, app: Image.Image, diff_img: Image.Image, title: str, stats: str) -> Image.Image:
    panel_w, panel_h = TARGET_W, TARGET_H
    width = GUTTER * 4 + panel_w * 3
    height = HEADER + LABEL_H + panel_h + GUTTER * 2
    canvas = Image.new("RGB", (width, height), (8, 18, 48))
    draw = ImageDraw.Draw(canvas)
    title_font = _font(22)
    label_font = _font(16)
    small = _font(14)
    draw.text((GUTTER, 16), title, fill=(255, 255, 255), font=title_font)
    draw.text((GUTTER, 40), stats, fill=(180, 210, 255), font=small)
    labels = ("Figma", "Roborazzi P9", "Diff")
    images = (figma, app, diff_img)
    y0 = HEADER + LABEL_H
    for i, (label, img) in enumerate(zip(labels, images)):
        x = GUTTER + i * (panel_w + GUTTER)
        draw.text((x, HEADER), label, fill=(255, 255, 255), font=label_font)
        canvas.paste(img, (x, y0))
    return canvas


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--figma", required=True)
    parser.add_argument("--app", required=True)
    parser.add_argument("--out", required=True)
    parser.add_argument("--name", default="screen")
    parser.add_argument("--threshold", type=float, default=2.0)
    args = parser.parse_args()

    figma_path = Path(args.figma)
    app_path = Path(args.app)
    out_path = Path(args.out)
    if not figma_path.exists() or not app_path.exists():
        print("missing input", file=sys.stderr)
        return 1

    figma = normalize(figma_path)
    app = normalize(app_path)
    diff_img, raw_pct = overlay_diff(figma, app)
    content_pct = content_diff_pct(figma, app)
    try:
        search_pct = pixel_diff_pct(figma_path, app_path, STATUS)
    except Exception:
        search_pct = content_pct

    figma_boxes = measure_home_trabajador(figma_path, figma=True)
    app_boxes = measure_home_trabajador(app_path, figma=False)
    layout_lines = []
    max_box_delta = 0.0
    for key in figma_boxes:
        delta = dp_delta(figma_boxes[key], app_boxes[key])
        layout_lines.append(f"{key}: {delta}")
        if isinstance(figma_boxes[key], tuple) and isinstance(app_boxes[key], tuple):
            max_box_delta = max(
                max_box_delta,
                max(abs(x - y) for x, y in zip(figma_boxes[key], app_boxes[key])),
            )
        elif isinstance(figma_boxes[key], (int, float)) and isinstance(app_boxes[key], (int, float)):
            max_box_delta = max(max_box_delta, abs(float(figma_boxes[key]) - float(app_boxes[key])))

    stats = (
        f"pixel {content_pct:.2f}% (raw {raw_pct:.2f}%, band {search_pct:.2f}%)  "
        f"max box Δ {max_box_delta:.1f}dp  threshold {args.threshold}%"
    )
    image = compose(
        figma,
        app,
        diff_img,
        title=f"{args.name}  Figma vs app",
        stats=stats,
    )
    out_path.parent.mkdir(parents=True, exist_ok=True)
    image.save(out_path)

    report = {
        "screen": args.name,
        "pixel_diff_pct": round(content_pct, 2),
        "raw_diff_pct": round(raw_pct, 2),
        "max_box_delta_dp": round(max_box_delta, 1),
        "layout": layout_lines,
        "output": str(out_path),
        "pass_pixel": content_pct <= args.threshold,
        "pass_layout": max_box_delta <= 3.0 or max_box_delta == 0.0,
    }
    print(json.dumps(report, ensure_ascii=False, indent=2))
    print(f"wrote {out_path}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
