#!/usr/bin/env python3
"""
Herramienta de comparación de capturas de pantalla contra referencias de Figma.

Uso:
    python figma_diff.py <figma_png> <screenshot_png> [--output <diff_output>] [--threshold <percent>]

Normaliza ambas imágenes a 440x980, recorta la barra de estado (48px arriba) y
el indicador de navegación (24px abajo), y reporta el porcentaje de píxeles diferentes.
"""

import argparse
import sys
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw, ImageFont


# Dimensiones target de Figma
TARGET_WIDTH = 440
TARGET_HEIGHT = 980

# Píxeles a recortar (sistema)
STATUS_BAR_HEIGHT = 48
NAV_HANDLE_HEIGHT = 24

# Umbral de diferencia por defecto
DEFAULT_THRESHOLD = 2.0  # porcentaje


def normalize_image(image: Image.Image) -> Image.Image:
    """
    Normaliza una imagen a 440x980 manteniendo aspect ratio y centrándola.
    """
    # Convertir a RGB si es necesario
    if image.mode != 'RGB':
        image = image.convert('RGB')
    
    # Calcular el nuevo tamaño manteniendo aspect ratio
    img_width, img_height = image.size
    aspect_ratio = img_width / img_height
    target_aspect = TARGET_WIDTH / TARGET_HEIGHT
    
    if aspect_ratio > target_aspect:
        # Imagen más ancha, ajustar por ancho
        new_width = TARGET_WIDTH
        new_height = int(TARGET_WIDTH / aspect_ratio)
    else:
        # Imagen más alta, ajustar por alto
        new_height = TARGET_HEIGHT
        new_width = int(TARGET_HEIGHT * aspect_ratio)
    
    # Redimensionar con antialiasing
    resized = image.resize((new_width, new_height), Image.Resampling.LANCZOS)
    
    # Crear canvas de 440x980 con fondo negro
    normalized = Image.new('RGB', (TARGET_WIDTH, TARGET_HEIGHT), (0, 0, 0))
    
    # Centrar la imagen redimensionada
    offset_x = (TARGET_WIDTH - new_width) // 2
    offset_y = (TARGET_HEIGHT - new_height) // 2
    normalized.paste(resized, (offset_x, offset_y))
    
    return normalized


def crop_system_chrome(image: Image.Image) -> Image.Image:
    """
    Recorta la barra de estado superior (48px) y el indicador de navegación inferior (24px).
    """
    width, height = image.size
    cropped_height = height - STATUS_BAR_HEIGHT - NAV_HANDLE_HEIGHT
    
    # Recortar: (left, top, right, bottom)
    return image.crop((0, STATUS_BAR_HEIGHT, width, height - NAV_HANDLE_HEIGHT))


def compute_diff(img1: Image.Image, img2: Image.Image) -> tuple[Image.Image, float]:
    """
    Computa la diferencia entre dos imágenes y retorna:
    - Una imagen de diferencia (blanco = diferente, negro = igual)
    - El porcentaje de píxeles diferentes
    """
    # Asegurar mismo tamaño
    if img1.size != img2.size:
        raise ValueError(f"Las imágenes tienen tamaños diferentes: {img1.size} vs {img2.size}")
    
    # Calcular diferencia absoluta
    diff = ImageChops.difference(img1, img2)
    
    # Convertir a escala de grises para análisis
    gray_diff = diff.convert('L')
    
    # Contar píxeles diferentes (umbral de 10 en escala 0-255 para tolerar pequeñas variaciones)
    pixels = list(gray_diff.getdata())
    total_pixels = len(pixels)
    different_pixels = sum(1 for p in pixels if p > 10)
    
    diff_percentage = (different_pixels / total_pixels) * 100
    
    # Crear imagen de diferencia visual (magnificar diferencias)
    visual_diff = diff.convert('L')
    visual_diff = visual_diff.point(lambda p: 255 if p > 10 else 0)
    
    # Colorear diferencias en rojo sobre la imagen original
    colored_diff = img2.copy().convert('RGB')
    diff_mask = visual_diff.point(lambda p: 255 if p > 0 else 0)
    red_overlay = Image.new('RGB', img2.size, (255, 0, 0))
    colored_diff.paste(red_overlay, mask=diff_mask)
    
    return colored_diff, diff_percentage


def add_stats_overlay(image: Image.Image, diff_percentage: float, threshold: float) -> Image.Image:
    """
    Agrega texto con estadísticas en la parte superior de la imagen.
    """
    img = image.copy()
    draw = ImageDraw.Draw(img)
    
    # Intentar usar una fuente del sistema, fallback a default
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 24)
    except:
        font = ImageFont.load_default()
    
    status = "PASS ✓" if diff_percentage <= threshold else "FAIL ✗"
    text = f"{diff_percentage:.2f}% pixels different | Threshold: {threshold}% | {status}"
    
    # Fondo semi-transparente
    bbox = draw.textbbox((0, 0), text, font=font)
    text_width = bbox[2] - bbox[0]
    text_height = bbox[3] - bbox[1]
    
    # Dibujar rectángulo de fondo
    bg_color = (0, 100, 0) if diff_percentage <= threshold else (139, 0, 0)
    draw.rectangle([(0, 0), (img.width, text_height + 20)], fill=bg_color)
    
    # Dibujar texto
    draw.text((10, 10), text, fill=(255, 255, 255), font=font)
    
    return img


def main():
    parser = argparse.ArgumentParser(
        description="Compara una captura de pantalla contra una referencia de Figma"
    )
    parser.add_argument("figma_png", type=str, help="Archivo PNG de referencia de Figma")
    parser.add_argument("screenshot_png", type=str, help="Archivo PNG de captura del emulador/dispositivo")
    parser.add_argument("--output", "-o", type=str, help="Archivo de salida para la imagen de diferencia")
    parser.add_argument(
        "--threshold",
        "-t",
        type=float,
        default=DEFAULT_THRESHOLD,
        help=f"Umbral de diferencia en porcentaje (default: {DEFAULT_THRESHOLD}%%)"
    )
    parser.add_argument(
        "--no-crop",
        action="store_true",
        help="No recortar barra de estado ni indicador de navegación"
    )
    
    args = parser.parse_args()
    
    figma_path = Path(args.figma_png)
    screenshot_path = Path(args.screenshot_png)
    
    # Validar que existan los archivos
    if not figma_path.exists():
        print(f"Error: No se encontró el archivo de Figma: {figma_path}", file=sys.stderr)
        return 1
    
    if not screenshot_path.exists():
        print(f"Error: No se encontró el screenshot: {screenshot_path}", file=sys.stderr)
        return 1
    
    try:
        print(f"Cargando imágenes...")
        print(f"  Figma:      {figma_path}")
        print(f"  Screenshot: {screenshot_path}")
        
        # Cargar imágenes
        figma_img = Image.open(figma_path)
        screenshot_img = Image.open(screenshot_path)
        
        print(f"\nNormalizando a {TARGET_WIDTH}x{TARGET_HEIGHT}...")
        figma_normalized = normalize_image(figma_img)
        screenshot_normalized = normalize_image(screenshot_img)
        
        if not args.no_crop:
            print(f"Recortando barra de estado ({STATUS_BAR_HEIGHT}px) y navegación ({NAV_HANDLE_HEIGHT}px)...")
            figma_cropped = crop_system_chrome(figma_normalized)
            screenshot_cropped = crop_system_chrome(screenshot_normalized)
        else:
            figma_cropped = figma_normalized
            screenshot_cropped = screenshot_normalized
        
        print(f"Calculando diferencias...")
        diff_img, diff_percentage = compute_diff(figma_cropped, screenshot_cropped)
        
        # Agregar overlay con stats
        diff_img_with_stats = add_stats_overlay(diff_img, diff_percentage, args.threshold)
        
        # Guardar resultado
        if args.output:
            output_path = Path(args.output)
            output_path.parent.mkdir(parents=True, exist_ok=True)
            diff_img_with_stats.save(output_path)
            print(f"\nImagen de diferencia guardada: {output_path}")
        
        # Reportar resultado
        print(f"\n{'='*60}")
        print(f"RESULTADO: {diff_percentage:.2f}% de píxeles diferentes")
        print(f"Umbral:    {args.threshold}%")
        
        if diff_percentage <= args.threshold:
            print(f"Estado:    ✓ PASS - Las imágenes son suficientemente similares")
            print(f"{'='*60}")
            return 0
        else:
            print(f"Estado:    ✗ FAIL - Las imágenes difieren más del umbral permitido")
            print(f"{'='*60}")
            return 1
    
    except Exception as e:
        print(f"\nError al procesar imágenes: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == "__main__":
    sys.exit(main())
