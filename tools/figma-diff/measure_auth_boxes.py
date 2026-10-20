#!/usr/bin/env python3
"""Mide bounding boxes de glifos/bordes en capturas Roborazzi vs PNG de Figma.

Figma y el set 440 se miden en el frame 440×980 (1px=1dp).
El set Pixel 9 Pro se mide en dp nativos (px / 3), SIN estirar a 440.

Mayúsculas: se toma la primera fila con trazo ancho (no el primer píxel
antialias de un punto de i o de una minúscula).
"""

from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
from PIL import Image

FIGMA_SIZE = (440, 980)
NAVY = np.array([0, 53, 139], dtype=np.int16)
NAVY_TOL = 28
OUTLINE = np.array([0, 77, 168], dtype=np.int16)


def detect_density(img: Image.Image) -> float:
    w, h = img.size
    if abs(w - 440) <= 2:
        return 1.0
    # xxhdpi 427dp → ~1281px; also accept 1280.
    if abs(w / 3.0 - 427) <= 3 or abs(w / 3.0 - 426.7) <= 3:
        return 3.0
    if abs(w / 2.0 - 440) <= 2:
        return 2.0
    return max(w / 440.0, 1.0)


def load_capture(path: Path, *, figma: bool) -> tuple[np.ndarray, float, float]:
    """Returns (rgb int16, density, width_dp)."""
    img = Image.open(path).convert("RGB")
    if figma:
        if img.size != FIGMA_SIZE:
            img = img.resize(FIGMA_SIZE, Image.Resampling.LANCZOS)
        return np.asarray(img, dtype=np.int16), 1.0, 440.0
    density = detect_density(img)
    width_dp = img.size[0] / density
    return np.asarray(img, dtype=np.int16), density, width_dp


def not_navy(arr: np.ndarray) -> np.ndarray:
    return np.max(np.abs(arr - NAVY), axis=2) > NAVY_TOL


def is_outline(arr: np.ndarray) -> np.ndarray:
    return np.max(np.abs(arr - OUTLINE), axis=2) <= 22


def is_white(arr: np.ndarray, min_v: int = 200) -> np.ndarray:
    return (arr[:, :, 0] >= min_v) & (arr[:, :, 1] >= min_v) & (arr[:, :, 2] >= min_v)


def is_yellow(arr: np.ndarray) -> np.ndarray:
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    return (r > 180) & (g > 120) & (b < 80)


def px(dp: float, density: float) -> int:
    return int(round(dp * density))


def band_mask(
    mask: np.ndarray,
    top_dp: float,
    bottom_dp: float,
    left_dp: float,
    right_dp: float,
    density: float,
) -> np.ndarray:
    h, w = mask.shape
    t = max(0, min(h, px(top_dp, density)))
    b = max(0, min(h, px(bottom_dp, density)))
    l = max(0, min(w, px(left_dp, density)))
    r = max(0, min(w, px(right_dp, density)))
    out = np.zeros_like(mask)
    out[t:b, l:r] = mask[t:b, l:r]
    return out


def bbox_px(mask: np.ndarray) -> tuple[int, int, int, int] | None:
    ys, xs = np.where(mask)
    if len(ys) == 0:
        return None
    return int(xs.min()), int(ys.min()), int(xs.max()), int(ys.max())


def capital_top_px(mask: np.ndarray, min_width_px: int) -> int | None:
    """Primera fila con trazo de mayúscula (ancho mínimo), no un i-dot."""
    if mask.size == 0:
        return None
    counts = mask.sum(axis=1)
    threshold = max(3, min_width_px)
    for y, count in enumerate(counts):
        if count >= threshold:
            nxt = counts[y + 1] if y + 1 < len(counts) else 0
            if nxt >= threshold * 0.35:
                return int(y)
    ys = np.where(counts > 0)[0]
    return int(ys[0]) if len(ys) else None


def box_dp(
    mask: np.ndarray,
    density: float,
    *,
    capital: bool = False,
    min_width_dp: float = 10.0,
) -> tuple[float, float, float, float] | None:
    raw = bbox_px(mask)
    if raw is None:
        return None
    x0, y0, x1, y1 = raw
    if capital:
        cap = capital_top_px(mask, px(min_width_dp, density))
        if cap is not None:
            y0 = cap
    d = density
    return x0 / d, y0 / d, x1 / d, y1 / d


def long_h_span(
    mask: np.ndarray,
    density: float,
    left_dp: float,
    right_dp: float,
    top_dp: float,
    bottom_dp: float,
    min_width_dp: float,
) -> tuple[float, float, float, float] | None:
    """Primera y última fila con un trazo horizontal largo (borde de card)."""
    h, w = mask.shape
    l = max(0, min(w, px(left_dp, density)))
    r = max(0, min(w, px(right_dp, density)))
    t = max(0, min(h, px(top_dp, density)))
    b = max(0, min(h, px(bottom_dp, density)))
    min_run = px(min_width_dp, density)
    counts = mask[t:b, l:r].sum(axis=1)
    rows = [i for i, c in enumerate(counts) if c >= min_run]
    if not rows:
        return box_dp(mask[t:b, l:r], density)
    y0 = t + rows[0]
    y1 = t + rows[-1]
    xs = np.where(mask[y0 : y1 + 1, l:r])[1] if y1 >= y0 else np.array([])
    if len(xs) == 0:
        x0, x1 = l, r - 1
    else:
        x0, x1 = l + int(xs.min()), l + int(xs.max())
    d = density
    return x0 / d, y0 / d, x1 / d, y1 / d


def fmt(box: tuple[float, float, float, float] | None) -> str:
    if box is None:
        return "—"
    x0, y0, x1, y1 = box
    return f"x{x0:.0f}-{x1:.0f} y{y0:.0f} h{y1 - y0 + 1 / 1:.0f} w{x1 - x0 + 1:.0f}"


def measure_login(arr: np.ndarray, density: float, width_dp: float, error: bool = False) -> dict:
    white = is_white(arr)
    yellow = is_yellow(arr)
    outline = is_outline(arr)
    content = not_navy(arr)
    right = width_dp
    return {
        "logo": box_dp(band_mask(yellow | white, 90, 320, 140, min(300, right), density), density),
        "wordmark": box_dp(
            band_mask(white, 318, 380, 40, 150, density),
            density,
            capital=True,
            min_width_dp=12,
        ),
        "first_field": box_dp(band_mask(content, 430, 520, 28, min(412, right), density), density),
        "divider": box_dp(band_mask(outline | content, 620, 670, 30, min(410, right), density), density),
        "support_card": long_h_span(outline, density, 28, min(410, right), 730, 900, 200),
        "badge": long_h_span(outline, density, min(360, right - 50), right, 700, 760, 18),
        "support_text": box_dp(
            band_mask(white, 740, 900, 40, min(280, right), density),
            density,
            capital=True,
            min_width_dp=8,
        ),
    }


def measure_role(arr: np.ndarray, density: float, width_dp: float) -> dict:
    white = is_white(arr)
    yellow = is_yellow(arr)
    outline = is_outline(arr)
    content = not_navy(arr)
    right = width_dp
    return {
        "header_logo": box_dp(band_mask(yellow | white, 50, 180, 20, min(110, right), density), density),
        "header_wordmark": box_dp(
            band_mask(white, 50, 180, 90, min(430, right), density),
            density,
            capital=True,
            min_width_dp=14,
        ),
        "cards": long_h_span(outline, density, 28, 210, 210, 400, 140),
        "card_titles": box_dp(
            band_mask(white, 220, 270, 28, min(412, right), density),
            density,
            capital=True,
            min_width_dp=8,
        ),
        "card_icons": box_dp(band_mask(white, 250, 400, 28, min(412, right), density), density),
        "divider": long_h_span(outline, density, 28, min(412, right), 400, 430, 200),
        "prompt": box_dp(
            band_mask(white, 410, 470, 28, min(250, right), density),
            density,
            capital=True,
            min_width_dp=6,
        ),
        "support_card": long_h_span(outline, density, 28, min(410, right), 510, 650, 200),
        "badge": long_h_span(outline, density, min(340, right - 50), right, 480, 540, 18),
        "support_text": box_dp(
            band_mask(white, 510, 650, 40, min(280, right), density),
            density,
            capital=True,
            min_width_dp=8,
        ),
    }


def measure_register(arr: np.ndarray, density: float, width_dp: float) -> dict:
    white = is_white(arr)
    outline = is_outline(arr)
    content = not_navy(arr)
    right = width_dp
    return {
        "title": box_dp(
            band_mask(white, 50, 130, 20, min(400, right), density),
            density,
            capital=True,
            min_width_dp=12,
        ),
        "subtitle": box_dp(
            band_mask(white, 100, 160, 20, min(400, right), density),
            density,
            capital=True,
            min_width_dp=8,
        ),
        "first_field": long_h_span(outline, density, 28, min(412, right), 145, 220, 200),
        "hint": box_dp(
            band_mask(white, 320, 400, 28, min(412, right), density),
            density,
            capital=True,
            min_width_dp=6,
        ),
        "password_1": long_h_span(outline, density, 28, min(412, right), 380, 460, 200),
        "password_2": long_h_span(outline, density, 28, min(412, right), 450, 530, 200),
        "terms": box_dp(
            band_mask(white, 510, 580, 28, min(300, right), density),
            density,
            capital=True,
            min_width_dp=8,
        ),
        "divider": long_h_span(outline, density, 28, min(412, right), 560, 620, 200),
        "prompt": box_dp(
            band_mask(white, 590, 650, 28, min(250, right), density),
            density,
            capital=True,
            min_width_dp=6,
        ),
    }


TARGETS = {
    "login": {
        "logo": 112,
        "wordmark": 328,
        "support_card": 746,
        "badge": 724,
        "support_card_h": 101,
        "badge_w": 31,
        "badge_h": 33,
    },
    "login_error": {
        "support_card": 750,
        "badge": 728,
        "support_card_h": 101,
    },
    "role": {
        "header_logo": 67,
        "header_wordmark": 67,
        "cards": 228,
        "card_titles": 243,
        "card_icons": 275,
        "support_card": 522,
        "badge": 499,
        "cards_h": 160,
        "support_card_h": 80,
        "badge_w": 31,
        "badge_h": 33,
        "divider": 410,
        "prompt": 427,
    },
    "register": {
        "title": 75,
        "subtitle": 118,
        "first_field": 162,
        "hint": 357,
        "password_1": 410,
        "password_2": 474,
        "terms": 539,
        "divider": 592,
        "prompt": 609,
    },
}


def pair_report(name: str, figma: dict, app: dict, label: str) -> None:
    print(f"\n=== {name} [{label}] ===")
    print(f"{'item':<16} {'figma':<32} {'app':<32} {'Δtop':>6} {'target'}")
    tmap = TARGETS.get(name, {})
    for key in figma:
        fb, ab = figma[key], app.get(key)
        ftop = fb[1] if fb else None
        atop = ab[1] if ab else None
        delta = (atop - ftop) if ftop is not None and atop is not None else None
        target = tmap.get(key, "")
        dstr = f"{delta:+.1f}" if delta is not None else "—"
        tgt = f"{target}" if target != "" else ""
        print(f"{key:<16} {fmt(fb):<32} {fmt(ab):<32} {dstr:>6} {tgt}")
        if key in ("support_card", "header_wordmark", "cards", "badge") and ab:
            h = ab[3] - ab[1] + (1 / 1)
            w = ab[2] - ab[0] + 1
            print(f"{'':16}   app extra: right={ab[2]:.0f} h={h:.1f} w={w:.1f}")
            hkey = f"{key}_h"
            wkey = f"{key}_w"
            extras = []
            if hkey in tmap:
                extras.append(f"hΔ={h - tmap[hkey]:+.1f} (tgt {tmap[hkey]})")
            if wkey in tmap:
                extras.append(f"wΔ={w - tmap[wkey]:+.1f} (tgt {tmap[wkey]})")
            if key == "badge" and "badge_h" in tmap:
                extras.append(f"hΔ={h - tmap['badge_h']:+.1f} (tgt {tmap['badge_h']})")
            if extras:
                print(f"{'':16}   {', '.join(extras)}")


def find_capture(cap: Path, class_prefix: str, method: str) -> Path | None:
    patterns = [
        f"{class_prefix}.{method}.png",
        f"{class_prefix}/{method}.png",
    ]
    for pat in patterns:
        hits = list(cap.rglob(pat))
        if hits:
            return hits[0]
    return None


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--captures", default="feature/auth/build/outputs/roborazzi")
    parser.add_argument("--refs", default="screens/flujos")
    parser.add_argument(
        "--set",
        choices=("440", "p9", "both"),
        default="both",
        help="Cuál set de capturas medir",
    )
    args = parser.parse_args()
    cap = Path(args.captures)
    refs = Path(args.refs)

    sets = []
    if args.set in ("440", "both"):
        sets.append(
            (
                "440 mdpi",
                "com.cocido.oficios.feature.auth.screenshot.AuthScreenshotTest",
            )
        )
    if args.set in ("p9", "both"):
        sets.append(
            (
                "P9 xxhdpi 427",
                "com.cocido.oficios.feature.auth.screenshot.AuthPixel9ScreenshotTest",
            )
        )

    pairs = [
        ("login", refs / "02-ingrese-sus-datos-para-iniciar-sesion.png", "login",
         lambda a, d, w: measure_login(a, d, w, False)),
        ("login_error", refs / "04-los-datos-de-sesion-no-coinciden.png", "loginError",
         lambda a, d, w: measure_login(a, d, w, True)),
        ("role", refs / "13-elegi-como-registrarte.png", "role", measure_role),
        ("register", refs / "07-registro-de-usuario.png", "register", measure_register),
    ]

    for set_label, class_prefix in sets:
        print(f"\n######## {set_label} ########")
        for name, figma_path, method, measurer in pairs:
            app_path = find_capture(cap, class_prefix, method)
            if not figma_path.exists():
                print(f"SKIP {name}: missing figma {figma_path}")
                continue
            if app_path is None:
                print(f"SKIP {name}: missing capture {class_prefix}.{method}.png")
                continue
            f_arr, f_den, f_w = load_capture(figma_path, figma=True)
            a_arr, a_den, a_w = load_capture(app_path, figma=False)
            print(f"  capture {app_path.name} {a_arr.shape[1]}x{a_arr.shape[0]}px density={a_den} width_dp={a_w:.1f}")
            pair_report(name, measurer(f_arr, f_den, f_w), measurer(a_arr, a_den, a_w), set_label)


if __name__ == "__main__":
    main()
