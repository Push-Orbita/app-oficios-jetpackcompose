/**
 * gen_cierre_modulo.js
 * Template reutilizable para generar informes de cierre de módulo.
 * Estructura estandarizada según M01 aprobado.
 *
 * Uso:
 *   node gen_cierre_modulo.js --modulo M03 --nombre "Autenticación e Identidad" \
 *        --complejidad "Módulo crítico" --fecha 16/07/2026 \
 *        --dias-est 9 --dias-real 9 --output Informe_Cierre_M03.docx
 *
 * O importar buildCierreModulo(config) desde otro script.
 */

const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Footer, AlignmentType, BorderStyle, WidthType, ShadingType,
  VerticalAlign, PageNumber, LevelFormat, PageBreak, ImageRun
} = require('/sessions/quirky-trusting-hopper/mnt/outputs/lib/node_modules/docx');
const fs = require('fs');
const path = require('path');

// ─── PALETA ──────────────────────────────────────────────────────────────────
const C = {
  VERDE_DARK:  "3D5228",
  VERDE_MID:   "587040",
  VERDE_LIGHT: "DDE8CC",
  VERDE_PALE:  "EEF5E6",
  WHITE:       "FFFFFF",
  BLACK:       "000000",
  GRAY_BG:     "F5F5F5",
  GREEN_OK:    "E2EFDA",
  GREEN_OK_FG: "375623",
  NAVY:        "231D38",
  DARK_TEXT:   "444444",
  MID_TEXT:    "555555",
  LIGHT_TEXT:  "999999",
  FOOTER_TEXT: "888888",
};

const LINE_SPACING = { line: 276, lineRule: "auto" }; // 1.15x

const LOGO_PATH = path.join(__dirname, 'logo_oficios.png');

// ─── HELPERS ─────────────────────────────────────────────────────────────────
function brd(color = "CCCCCC", size = 4) {
  const s = { style: BorderStyle.SINGLE, size, color };
  return { top: s, bottom: s, left: s, right: s };
}

function cell(text, {
  bg = C.WHITE, bold = false, color = C.BLACK, width = 2000,
  align = AlignmentType.LEFT, size = 19, italic = false,
  vAlign = VerticalAlign.TOP
} = {}) {
  return new TableCell({
    borders: brd("DDDDDD", 3),
    width: { size: width, type: WidthType.DXA },
    shading: { fill: bg, type: ShadingType.CLEAR },
    margins: { top: 120, bottom: 120, left: 180, right: 180 },
    verticalAlign: vAlign,
    children: [new Paragraph({
      alignment: align,
      spacing: { ...LINE_SPACING, before: 0, after: 0 },
      children: [new TextRun({ text, bold, color, size, font: "Arial", italics: italic })]
    })]
  });
}

function hdrCell(text, width) {
  return cell(text, {
    bg: C.VERDE_DARK, bold: true, color: C.WHITE,
    width, align: AlignmentType.CENTER, size: 18
  });
}

function hdrRow(cols) {
  return new TableRow({
    tableHeader: true,
    children: cols.map(([t, w]) => hdrCell(t, w))
  });
}

function sp(pts = 140) {
  return new Paragraph({ spacing: { before: pts, after: 0 }, children: [new TextRun("")] });
}

function sectionTitle(text) {
  return new Paragraph({
    spacing: { before: 340, after: 160 },
    keepNext: true,
    border: { bottom: { style: BorderStyle.SINGLE, size: 8, color: C.VERDE_MID, space: 1 } },
    children: [new TextRun({ text, bold: true, size: 30, color: C.VERDE_DARK, font: "Arial" })]
  });
}

function bullet(text) {
  return new Paragraph({
    numbering: { reference: "bullets", level: 0 },
    spacing: { ...LINE_SPACING, before: 60, after: 60 },
    children: [new TextRun({ text, font: "Arial", size: 20 })]
  });
}

function tableRow(cells, isEven) {
  return new TableRow({ children: cells });
}

// ─── BUILDER PRINCIPAL ───────────────────────────────────────────────────────
/**
 * @param {object} config
 * @param {string} config.num           "02"
 * @param {string} config.nombre        "Design System Base"
 * @param {string} config.complejidad   "Fundación — Módulo transversal de UI"
 * @param {string} config.fecha         "03/07/2026"
 * @param {string} config.diasEst       "5"
 * @param {string} config.diasReal      "5"
 * @param {string} config.variacion     "Sin variación"
 * @param {string} config.estado        "Completado"
 * @param {Array}  config.alcance       [[componente, descripción], ...]
 * @param {Array}  config.decisiones    [string, ...]
 * @param {Array}  config.testing       [[componente, cobertura, notas], ...]
 * @param {Array}  config.dependencias  [string, ...]
 * @param {string} config.output        ruta de salida del .docx
 */
function buildCierreModulo(config) {
  const {
    num, nombre, complejidad, fecha, diasEst, diasReal,
    variacion = "Sin variación", estado = "Completado",
    alcance, decisiones, testing, dependencias, output
  } = config;

  const logoData = fs.readFileSync(LOGO_PATH);

  // ── PORTADA ────────────────────────────────────────────────────────────────
  const portada = [
    sp(160),
    // Título
    new Paragraph({
      spacing: { before: 0, after: 80 },
      children: [new TextRun({ text: "Informe de Cierre de Módulo", bold: true, size: 38, color: C.VERDE_DARK, font: "Arial" })]
    }),
    sp(500),
    // Logo
    new Paragraph({
      spacing: { before: 0, after: 0 },
      children: [new ImageRun({
        data: logoData,
        transformation: { width: 124, height: 203 }
      })]
    }),
    sp(200),
    // Nombre del proyecto
    new Paragraph({
      spacing: { before: 0, after: 60 },
      children: [new TextRun({ text: "Oficios Formosa", bold: true, size: 48, color: C.NAVY, font: "Arial" })]
    }),
    new Paragraph({
      spacing: { before: 0, after: 0 },
      children: [new TextRun({ text: "Plataforma de contratación de trabajadores de oficios", size: 22, color: C.MID_TEXT, font: "Arial" })]
    }),
    sp(600),
    // Tabla metadata portada
    new Table({
      width: { size: 5200, type: WidthType.DXA },
      columnWidths: [1800, 3400],
      rows: [
        ["Fecha de emisión", fecha],
        ["Elaborado por",    "Britez Matías — Android Engineer"],
        ["Versión",         "v1.1"],
      ].map(([k, v]) => new TableRow({ children: [
        cell(k, { bg: C.GRAY_BG, bold: true, width: 1800, size: 19 }),
        cell(v, { width: 3400, size: 19 }),
      ]}))
    }),
    // Page break explícito
    new Paragraph({ children: [new PageBreak()] }),
  ];

  // ── CONTENIDO ──────────────────────────────────────────────────────────────
  const contenido = [
    // Header de contenido
    new Paragraph({
      spacing: { before: 0, after: 60 },
      children: [new TextRun({ text: "Informe de Cierre de Módulo", bold: true, size: 38, color: C.VERDE_DARK, font: "Arial" })]
    }),
    new Paragraph({
      spacing: { before: 0, after: 240 },
      children: [
        new TextRun({ text: `M${num}  |  `, bold: true, size: 24, color: C.VERDE_MID, font: "Arial" }),
        new TextRun({ text: nombre, size: 24, color: C.DARK_TEXT, font: "Arial" }),
        new TextRun({ text: `  |  ${fecha}`, size: 20, color: C.LIGHT_TEXT, font: "Arial" }),
      ]
    }),

    // ── 1. Alcance entregado
    sectionTitle("1. Alcance entregado"),
    sp(80),
    new Table({
      width: { size: 9200, type: WidthType.DXA },
      columnWidths: [2700, 6500],
      rows: [
        hdrRow([["Componente", 2700], ["Descripción", 6500]]),
        ...alcance.map(([comp, desc], i) => new TableRow({ children: [
          cell(comp, { bg: i%2===0 ? C.VERDE_PALE : C.WHITE, bold: true, width: 2700, size: 18, color: C.VERDE_DARK, vAlign: VerticalAlign.TOP }),
          cell(desc, { bg: i%2===0 ? C.VERDE_PALE : C.WHITE, width: 6500, size: 17, vAlign: VerticalAlign.TOP }),
        ]}))
      ]
    }),
    sp(220),

    // ── 2. Decisiones técnicas clave
    sectionTitle("2. Decisiones técnicas clave"),
    sp(80),
    ...decisiones.map(bullet),
    sp(220),

    // ── 3. Estado de testing
    sectionTitle("3. Estado de testing"),
    sp(80),
    new Table({
      width: { size: 9200, type: WidthType.DXA },
      columnWidths: [3200, 1800, 4200],
      rows: [
        hdrRow([["Componente", 3200], ["Tipo de validación", 1800], ["Notas", 4200]]),
        ...testing.map(([comp, tipo, notas], i) => new TableRow({ children: [
          cell(comp,  { bg: i%2===0 ? C.VERDE_PALE : C.WHITE, bold: true, width: 3200, size: 17, color: C.VERDE_DARK }),
          cell(tipo,  { bg: i%2===0 ? C.VERDE_PALE : C.WHITE, width: 1800, size: 17, align: AlignmentType.CENTER }),
          cell(notas, { bg: i%2===0 ? C.VERDE_PALE : C.WHITE, width: 4200, size: 17 }),
        ]}))
      ]
    }),
    sp(220),

    // ── 4. Dependencias que habilita
    sectionTitle("4. Dependencias que habilita"),
    sp(80),
    ...dependencias.map(bullet),
    sp(220),

    // ── Resumen de cierre
    sectionTitle("Resumen de cierre"),
    sp(80),
    new Table({
      width: { size: 9200, type: WidthType.DXA },
      columnWidths: [2800, 1500, 1500, 1600, 1800],
      rows: [
        hdrRow([["Módulo", 2800], ["Días estimados", 1500], ["Días reales", 1500], ["Variación", 1600], ["Estado", 1800]]),
        new TableRow({ children: [
          cell(`M${num} — ${nombre}`,  { bg: C.VERDE_PALE, bold: true, width: 2800, size: 18, color: C.VERDE_DARK }),
          cell(`${diasEst} días`,      { bg: C.VERDE_PALE, width: 1500, align: AlignmentType.CENTER, size: 18 }),
          cell(`${diasReal} días`,     { bg: C.VERDE_PALE, width: 1500, align: AlignmentType.CENTER, size: 18 }),
          cell(variacion,              { bg: C.GREEN_OK,   width: 1600, align: AlignmentType.CENTER, size: 17, bold: true, color: C.GREEN_OK_FG }),
          cell(estado,                 { bg: C.GREEN_OK,   width: 1800, align: AlignmentType.CENTER, size: 18, bold: true, color: C.GREEN_OK_FG }),
        ]})
      ]
    }),
    sp(120),
  ];

  // ── DOCUMENTO ──────────────────────────────────────────────────────────────
  const doc = new Document({
    numbering: {
      config: [{
        reference: "bullets",
        levels: [{
          level: 0, format: LevelFormat.BULLET, text: "•",
          alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 520, hanging: 280 } } }
        }]
      }]
    },
    styles: { default: { document: { run: { font: "Arial", size: 20 } } } },
    sections: [{
      properties: {
        page: {
          size: { width: 11906, height: 16838 },
          margin: { top: 1200, right: 1200, bottom: 1200, left: 1200 }
        }
      },
      footers: {
        default: new Footer({
          children: [new Paragraph({
            alignment: AlignmentType.CENTER,
            border: { top: { style: BorderStyle.SINGLE, size: 3, color: "CCCCCC", space: 4 } },
            spacing: { before: 80 },
            children: [
              new TextRun({ text: "Desarrollador Android Britez Matías — PUSH Software  |  Pág. ", font: "Arial", size: 15, color: C.FOOTER_TEXT }),
              new TextRun({ children: [PageNumber.CURRENT], font: "Arial", size: 15, color: C.FOOTER_TEXT }),
            ]
          })]
        })
      },
      children: [...portada, ...contenido]
    }]
  });

  return Packer.toBuffer(doc).then(buf => {
    fs.writeFileSync(output, buf);
    console.log(`✓ ${path.basename(output)}`);
  });
}

module.exports = { buildCierreModulo };

// ─── CLI ─────────────────────────────────────────────────────────────────────
if (require.main === module) {
  const args = process.argv.slice(2);
  const get = (flag) => { const i = args.indexOf(flag); return i >= 0 ? args[i+1] : null; };
  const modulo = get('--modulo') || 'MXX';
  const outFile = get('--output') || `Informe_Cierre_${modulo}.docx`;
  console.log(`Generando ${outFile}...`);
  // Ejemplo de uso directo: extender con datos reales por módulo
  console.log('Usa buildCierreModulo(config) con los datos del módulo.');
}
