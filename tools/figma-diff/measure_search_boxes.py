#!/usr/bin/env python3
"""Mide bounding boxes del buscador cliente vs PNG de Figma (home/oficios/rubros/resultados)."""

from __future__ import annotations

import argparse
from pathlib import Path

from measure_auth_boxes import (
    box_dp,
    band_mask,
    is_outline,
    is_white,
    is_yellow,
    load_capture,
    long_h_span,
    not_navy,
    find_capture,
)
from PIL import Image
import numpy as np


def pixel_diff_pct(figma_path: Path, app_path: Path, status_bar: float) -> float:
    """% de píxeles distintos en el área de contenido (sin status/nav), ambos a 440×908."""
    figma = Image.open(figma_path).convert("RGB").resize((440, 980), Image.Resampling.LANCZOS)
    app = Image.open(app_path).convert("RGB").resize((440, 980), Image.Resampling.LANCZOS)
    top = 48
    bottom = 980 - 24
    f = np.asarray(figma.crop((0, top, 440, bottom)), dtype=np.int16)
    a = np.asarray(app.crop((0, top, 440, bottom)), dtype=np.int16)
    # Tolera antialias / D-DIN vs DIN Pro: Δ>28 en algún canal.
    different = np.max(np.abs(f - a), axis=2) > 28
    return float(different.mean() * 100.0)


def measure_home(arr, density, width_dp):
    white = is_white(arr)
    outline = is_outline(arr)
    content = not_navy(arr)
    right = width_dp
    return {
        "avatar": box_dp(band_mask(content, 48, 130, 20, 90, density), density),
        "name": box_dp(band_mask(white, 48, 130, 80, 280, density), density, capital=True, min_width_dp=8),
        "bell": long_h_span(outline | content, density, min(350, right - 70), right, 48, 130, 30),
        "search": long_h_span(outline, density, 28, min(412, right), 110, 190, 200),
        "recientes": box_dp(band_mask(white, 170, 250, 20, 280, density), density, capital=True, min_width_dp=14),
        "recent_cards": long_h_span(outline, density, 28, min(180, right), 230, 380, 70),
        "support_card": long_h_span(outline, density, 28, min(412, right), 390, 560, 200),
    }


def measure_categories(arr, density, width_dp):
    white = is_white(arr)
    outline = is_outline(arr)
    right = width_dp
    return {
        "title": box_dp(band_mask(white, 48, 140, 20, 300, density), density, capital=True, min_width_dp=12),
        "bell": long_h_span(outline, density, min(350, right - 70), right, 48, 140, 30),
        "search": long_h_span(outline, density, 28, min(412, right), 110, 200, 200),
        "first_card": long_h_span(outline, density, 28, 220, 190, 400, 120),
        "second_card": long_h_span(outline, density, 220, min(412, right), 190, 400, 120),
    }


def measure_rubros(arr, density, width_dp):
    return measure_categories(arr, density, width_dp)


def measure_ficha(arr, density, width_dp):
    white = is_white(arr)
    outline = is_outline(arr)
    right = width_dp
    return {
        "title": box_dp(band_mask(white, 48, 140, 20, 380, density), density, capital=True, min_width_dp=12),
        "profile_card": long_h_span(outline, density, 28, min(412, right), 90, 420, 200),
        "gallery": long_h_span(outline, density, 28, min(412, right), 420, 820, 200),
    }


def measure_results(arr, density, width_dp):
    white = is_white(arr)
    yellow = is_yellow(arr)
    outline = is_outline(arr)
    right = width_dp
    return {
        "logo": box_dp(band_mask(yellow | white, 48, 180, 20, 110, density), density),
        "title": box_dp(band_mask(white, 48, 180, 90, min(420, right), density), density, capital=True, min_width_dp=12),
        "limit": box_dp(band_mask(white, 160, 230, 20, min(412, right), density), density, capital=True, min_width_dp=6),
        "search": long_h_span(outline, density, 28, min(412, right), 230, 290, 200),
        "worker_card": long_h_span(outline, density, 28, min(412, right), 300, 520, 200),
    }


TARGETS_REL = {
    "home": {"search": 80, "recientes": 144, "recent_cards": 199, "support_card": 373},
    "categories": {"title": 16, "search": 80, "first_card": 144},
    "rubros": {"title": 16, "search": 80, "first_card": 144},
    "results": {"logo": 19, "title": 16, "search": 155, "worker_card": 259},
    "ficha": {"title": 16, "profile_card": 80},
}


def pair_report(name, figma, app, label, status_bar):
    print(f"\n=== {name} [{label}] (relativo a status {status_bar:.0f}) ===")
    print(f"{'item':<16} {'figma_rel':<12} {'app_rel':<12} {'Δrel':>6} {'target'}")
    tmap = TARGETS_REL.get(name, {})
    for key in figma:
        fb, ab = figma[key], app.get(key)
        ftop = (fb[1] - 48.0) if fb else None
        atop = (ab[1] - status_bar) if ab else None
        delta = (atop - ftop) if ftop is not None and atop is not None else None
        dstr = f"{delta:+.1f}" if delta is not None else "—"
        fstr = f"{ftop:.1f}" if ftop is not None else "—"
        astr = f"{atop:.1f}" if atop is not None else "—"
        tgt = tmap.get(key, "")
        print(f"{key:<16} {fstr:<12} {astr:<12} {dstr:>6} {tgt}")
        if ab and key in ("search", "first_card", "worker_card", "support_card", "recent_cards"):
            h = ab[3] - ab[1] + 1
            print(f"{'':16}   mid-edge h={h:.1f} w={ab[2] - ab[0] + 1:.1f}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--captures", default="feature/home/build/outputs/roborazzi")
    parser.add_argument("--refs", default="screens/redisenio")
    parser.add_argument("--set", choices=("440", "p9", "both"), default="both")
    args = parser.parse_args()
    cap = Path(args.captures)
    refs = Path(args.refs)

    sets = []
    if args.set in ("440", "both"):
        sets.append(("440 mdpi", "com.cocido.oficios.feature.home.screenshot.SearchScreenshotTest", 48.0))
    if args.set in ("p9", "both"):
        sets.append(("P9 xxhdpi 427", "com.cocido.oficios.feature.home.screenshot.SearchPixel9ScreenshotTest", 52.0))

    pairs = [
        ("home", refs / "05-nicolas-ayala.png", "home", measure_home),
        ("categories", refs / "06-oficios.png", "categories", measure_categories),
        ("rubros", refs / "07-construccion.png", "rubros", measure_rubros),
        ("results", refs / "08-albaniles-de-oficios-formosa.png", "results", measure_results),
        ("ficha", refs / "09-ficha-completa.png", "ficha", measure_ficha),
    ]

    for set_label, class_prefix, status_bar in sets:
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
            pair_report(name, measurer(f_arr, f_den, f_w), measurer(a_arr, a_den, a_w), set_label, status_bar)
            print(f"  pixel-diff {pixel_diff_pct(figma_path, app_path, status_bar):.2f}%")


if __name__ == "__main__":
    main()
