#!/usr/bin/env python3
"""Mide bounding boxes de glifos/bordes en capturas Roborazzi vs PNG de Figma.

Normaliza a 440×980 (sin recortar chrome: el frame Figma ya lo incluye).
El fondo de marca es #00358B; se reportan tops de píxeles no-navy en bandas.
"""

from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
from PIL import Image

TARGET = (440, 980)
NAVY = np.array([0, 53, 139], dtype=np.int16)
NAVY_TOL = 28
OUTLINE = np.array([0, 77, 168], dtype=np.int16)


def normalize(path: Path) -> np.ndarray:
    img = Image.open(path).convert("RGB")
    if img.size != TARGET:
        img = img.resize(TARGET, Image.Resampling.LANCZOS)
    return np.asarray(img, dtype=np.int16)


def not_navy(arr: np.ndarray) -> np.ndarray:
    return np.max(np.abs(arr - NAVY), axis=2) > NAVY_TOL


def is_outline(arr: np.ndarray) -> np.ndarray:
    return np.max(np.abs(arr - OUTLINE), axis=2) <= 22


def is_white(arr: np.ndarray, min_v: int = 200) -> np.ndarray:
    return (arr[:, :, 0] >= min_v) & (arr[:, :, 1] >= min_v) & (arr[:, :, 2] >= min_v)


def is_yellow(arr: np.ndarray) -> np.ndarray:
    r, g, b = arr[:, :, 0], arr[:, :, 1], arr[:, :, 2]
    return (r > 180) & (g > 120) & (b < 80)


def bbox(mask: np.ndarray) -> tuple[int, int, int, int] | None:
    ys, xs = np.where(mask)
    if len(ys) == 0:
        return None
    return int(xs.min()), int(ys.min()), int(xs.max()), int(ys.max())


def band_mask(mask: np.ndarray, top: int, bottom: int, left: int = 0, right: int = 440) -> np.ndarray:
    out = np.zeros_like(mask)
    out[top:bottom, left:right] = mask[top:bottom, left:right]
    return out


def fmt(box: tuple[int, int, int, int] | None) -> str:
    if box is None:
        return "—"
    x0, y0, x1, y1 = box
    return f"x{x0}-{x1} y{y0} h{y1 - y0 + 1} w{x1 - x0 + 1}"


def measure_login(arr: np.ndarray, error: bool = False) -> dict[str, tuple[int, int, int, int] | None]:
    white = is_white(arr)
    yellow = is_yellow(arr)
    outline = is_outline(arr)
    content = not_navy(arr)
    logo_band = 90 if not error else 90
    return {
        "logo": bbox(band_mask(yellow | white, logo_band, 320, 140, 300)),
        "wordmark": bbox(band_mask(white, 300, 380, 40, 400)),
        "first_field": bbox(band_mask(content, 430, 520, 28, 412)),
        "divider": bbox(band_mask(outline | content, 620, 670, 30, 410)),
        "support_card": bbox(band_mask(outline, 700, 900, 28, 420)),
        "badge": bbox(band_mask(content, 690, 760, 360, 439)),
        "support_text": bbox(band_mask(white, 740, 900, 40, 280)),
    }


def measure_role(arr: np.ndarray) -> dict[str, tuple[int, int, int, int] | None]:
    white = is_white(arr)
    yellow = is_yellow(arr)
    outline = is_outline(arr)
    content = not_navy(arr)
    return {
        "header_logo": bbox(band_mask(yellow | white, 50, 180, 20, 110)),
        "header_wordmark": bbox(band_mask(white, 50, 180, 90, 430)),
        "cards": bbox(band_mask(outline, 200, 420, 28, 412)),
        "card_titles": bbox(band_mask(white, 220, 270, 28, 412)),
        "card_icons": bbox(band_mask(white, 250, 400, 28, 412)),
        "divider": bbox(band_mask(outline | content, 390, 430, 28, 412)),
        "support_card": bbox(band_mask(outline, 480, 650, 28, 420)),
        "badge": bbox(band_mask(content, 470, 540, 360, 439)),
        "support_text": bbox(band_mask(white, 510, 650, 40, 280)),
    }


def measure_register(arr: np.ndarray) -> dict[str, tuple[int, int, int, int] | None]:
    white = is_white(arr)
    outline = is_outline(arr)
    content = not_navy(arr)
    fields = []
    for top, bottom in ((140, 220), (380, 450), (450, 530), (520, 590)):
        fields.append(bbox(band_mask(content, top, bottom, 28, 412)))
    return {
        "title": bbox(band_mask(white, 50, 130, 20, 400)),
        "subtitle": bbox(band_mask(white, 110, 160, 20, 400)),
        "first_field": bbox(band_mask(content, 145, 220, 28, 412)),
        "hint": bbox(band_mask(white, 320, 400, 28, 412)),
        "password_1": bbox(band_mask(content, 380, 450, 28, 412)),
        "password_2": bbox(band_mask(content, 450, 530, 28, 412)),
        "terms": bbox(band_mask(white, 510, 580, 28, 300)),
        "divider": bbox(band_mask(outline | content, 570, 620, 28, 412)),
        "prompt": bbox(band_mask(white, 590, 650, 28, 250)),
    }


def pair_report(name: str, figma: dict, app: dict) -> None:
    print(f"\n=== {name} ===")
    print(f"{'item':<16} {'figma':<32} {'app':<32} {'Δtop':>6} {'target'}")
    targets = {
        "login": {
            "logo": 112,
            "wordmark": 327,
            "support_card": 746,
            "badge": 724,
        },
        "login_error": {
            "support_card": 750,
        },
        "role": {
            "header_logo": 67,
            "header_wordmark": 67,
            "cards": 228,
            "card_titles": 243,
            "card_icons": 275,
            "support_card": 522,
            "badge": 499,
        },
        "register": {
            "title": 75,
            "first_field": 162,
            "password_1": 410,
            "password_2": 474,
            "terms": 539,
            "divider": 592,
            "prompt": 609,
        },
    }
    tmap = targets.get(name, {})
    for key in figma:
        fb, ab = figma[key], app.get(key)
        ftop = fb[1] if fb else None
        atop = ab[1] if ab else None
        delta = (atop - ftop) if ftop is not None and atop is not None else None
        target = tmap.get(key, "")
        dstr = f"{delta:+d}" if delta is not None else "—"
        print(f"{key:<16} {fmt(fb):<32} {fmt(ab):<32} {dstr:>6} {target}")
        if key in ("support_card", "header_wordmark", "cards", "badge") and ab:
            print(f"{'':16}   app extra: right={ab[2]} h={ab[3]-ab[1]+1} w={ab[2]-ab[0]+1}")
            if fb:
                print(f"{'':16}   fig extra: right={fb[2]} h={fb[3]-fb[1]+1} w={fb[2]-fb[0]+1}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--captures", default="feature/auth/build/outputs/roborazzi")
    parser.add_argument("--refs", default="screens/flujos")
    args = parser.parse_args()
    cap = Path(args.captures)
    refs = Path(args.refs)

    pairs = [
        ("login", refs / "02-ingrese-sus-datos-para-iniciar-sesion.png",
         cap / "com.cocido.oficios.feature.auth.screenshot.AuthScreenshotTest.login.png",
         lambda a: measure_login(a, False)),
        ("login_error", refs / "04-los-datos-de-sesion-no-coinciden.png",
         cap / "com.cocido.oficios.feature.auth.screenshot.AuthScreenshotTest.loginError.png",
         lambda a: measure_login(a, True)),
        ("role", refs / "13-elegi-como-registrarte.png",
         cap / "com.cocido.oficios.feature.auth.screenshot.AuthScreenshotTest.role.png",
         measure_role),
        ("register", refs / "07-registro-de-usuario.png",
         cap / "com.cocido.oficios.feature.auth.screenshot.AuthScreenshotTest.register.png",
         measure_register),
    ]
    for name, figma_path, app_path, measurer in pairs:
        if not figma_path.exists() or not app_path.exists():
            print(f"SKIP {name}: missing {figma_path if not figma_path.exists() else app_path}")
            continue
        pair_report(name, measurer(normalize(figma_path)), measurer(normalize(app_path)))


if __name__ == "__main__":
    main()
