
package com.fittrack.service;

import com.fittrack.servicio.TablaReporte;
import com.fittrack.servicio.TipoReporte;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfContentByte;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportePDFService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Color AZUL = new Color(31, 55, 82);
    private static final Color AZUL_CLARO = new Color(226, 235, 245);
    private static final Color GRIS = new Color(245, 247, 250);
    private static final Color TEXTO = new Color(45, 55, 65);

    public void generar(
            Path destino,
            TipoReporte tipo,
            LocalDate desde,
            LocalDate hasta,
            String filtro,
            TablaReporte reporte)
            throws IOException, DocumentException {

        if (destino == null || tipo == null || reporte == null) {
            throw new IllegalArgumentException(
                    "Los datos necesarios para generar el PDF son obligatorios.");
        }

        boolean horizontal = reporte.columnas().size() > 6;

        Rectangle pagina = horizontal
                ? PageSize.A4.rotate()
                : PageSize.A4;

        Document documento = new Document(
                pagina, 36, 36, 42, 42);

        try (FileOutputStream salida =
                     new FileOutputStream(destino.toFile())) {

            PdfWriter escritor =
                    PdfWriter.getInstance(documento, salida);

            escritor.setPageEvent(new NumeracionPaginas());

            documento.addTitle("Reporte FitTrack - " + nombreReporte(tipo));
            documento.addSubject("Reporte del sistema FitTrack");
            documento.addAuthor("FitTrack");

            documento.open();

            Font titulo = fuente(19, Font.BOLD, AZUL);
            Font subtitulo = fuente(9, Font.NORMAL, TEXTO);
            Font etiqueta = fuente(9, Font.BOLD, AZUL);
            Font contenido = fuente(9, Font.NORMAL, TEXTO);

            Paragraph encabezado = new Paragraph("FITTRACK", titulo);
            encabezado.setAlignment(Element.ALIGN_CENTER);
            encabezado.setSpacingAfter(3);
            documento.add(encabezado);

            Paragraph descripcion = new Paragraph(
                    "SISTEMA DE GESTIÓN DE GIMNASIO", subtitulo);
            descripcion.setAlignment(Element.ALIGN_CENTER);
            descripcion.setSpacingAfter(14);
            documento.add(descripcion);

            Paragraph nombre = new Paragraph(
                    nombreReporte(tipo), fuente(14, Font.BOLD, AZUL));
            nombre.setAlignment(Element.ALIGN_CENTER);
            nombre.setSpacingAfter(12);
            documento.add(nombre);

            PdfPTable informacion = new PdfPTable(2);
            informacion.setWidthPercentage(100);
            informacion.setWidths(new float[]{1, 1});
            informacion.setSpacingAfter(12);

            agregarDato(informacion, "Generado:",
                    LocalDateTime.now().format(FORMATO_FECHA_HORA),
                    etiqueta, contenido);

            String periodo = obtenerPeriodo(tipo, desde, hasta);

            agregarDato(informacion, "Periodo:", periodo,
                    etiqueta, contenido);

            agregarDato(informacion, "Filtro:",
                    filtro == null || filtro.isBlank()
                            ? "Sin filtro"
                            : filtro,
                    etiqueta, contenido);

            documento.add(informacion);

            agregarTabla(documento, reporte);

            documento.close();
        }
    }

    private void agregarTabla(
            Document documento,
            TablaReporte reporte) throws DocumentException, IOException {

        List<String> columnas = reporte.columnas();
        List<List<String>> filas = reporte.filas();

        if (columnas.isEmpty()) {
            return;
        }

        PdfPTable tabla = new PdfPTable(columnas.size());
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(5);
        tabla.setSpacingAfter(10);
        tabla.setHeaderRows(1);
        tabla.setSplitRows(true);

        float[] anchos = new float[columnas.size()];

        for (int i = 0; i < columnas.size(); i++) {
            anchos[i] = Math.max(1, Math.min(3, columnas.get(i).length() / 7f));
        }

        tabla.setWidths(anchos);

        Font fuenteEncabezado =
                fuente(8, Font.BOLD, Color.WHITE);

        Font fuenteCelda =
                fuente(8, Font.NORMAL, TEXTO);

        for (String columna : columnas) {
            PdfPCell celda = new PdfPCell(
                    new Phrase(columna, fuenteEncabezado));

            celda.setBackgroundColor(AZUL);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celda.setPadding(7);

            tabla.addCell(celda);
        }

        for (int i = 0; i < filas.size(); i++) {
            List<String> fila = filas.get(i);

            for (int j = 0; j < columnas.size(); j++) {
                String valor = j < fila.size() && fila.get(j) != null
                        ? fila.get(j)
                        : "";

                PdfPCell celda = new PdfPCell(
                        new Phrase(valor, fuenteCelda));

                celda.setBackgroundColor(
                        i % 2 == 0 ? Color.WHITE : GRIS);

                celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
                celda.setPadding(6);

                tabla.addCell(celda);
            }
        }

        documento.add(tabla);
    }

    private void agregarDato(
            PdfPTable tabla,
            String titulo,
            String valor,
            Font fuenteTitulo,
            Font fuenteValor) {

        PdfPCell celda = new PdfPCell();

        celda.addElement(new Paragraph(titulo, fuenteTitulo));
        celda.addElement(new Paragraph(
                valor == null ? "" : valor, fuenteValor));

        celda.setBackgroundColor(AZUL_CLARO);
        celda.setBorderColor(Color.WHITE);
        celda.setPadding(7);

        tabla.addCell(celda);
    }

    private String obtenerPeriodo(
            TipoReporte tipo,
            LocalDate desde,
            LocalDate hasta) {

        if (tipo == TipoReporte.SOCIOS_ACTIVOS) {
            return "No aplica";
        }

        String inicio = desde == null
                ? "No definido"
                : desde.format(FORMATO_FECHA);

        String fin = hasta == null
                ? "No definido"
                : hasta.format(FORMATO_FECHA);

        return inicio + " - " + fin;
    }

    private String nombreReporte(TipoReporte tipo) {
        return switch (tipo) {
            case SOCIOS_ACTIVOS -> "Reporte de socios activos";
            case MEMBRESIAS -> "Reporte de membresías";
            case PAGOS -> "Reporte de pagos";
            case ACCESOS -> "Reporte de accesos";
        };
    }

    private Font fuente(int tamaño, int estilo, Color color)
            throws DocumentException, IOException {

        BaseFont base = BaseFont.createFont(
                BaseFont.HELVETICA,
                BaseFont.WINANSI,
                BaseFont.NOT_EMBEDDED);

        return new Font(base, tamaño, estilo, color);
    }

    private static class NumeracionPaginas
            extends PdfPageEventHelper {

        private PdfTemplate totalPaginas;
        private BaseFont fuente;

        @Override
        public void onOpenDocument(
                PdfWriter escritor,
                Document documento) {

            totalPaginas = escritor.getDirectContent()
                    .createTemplate(50, 12);

            try {
                fuente = BaseFont.createFont(
                        BaseFont.HELVETICA,
                        BaseFont.WINANSI,
                        BaseFont.NOT_EMBEDDED);
            } catch (Exception ex) {
                throw new IllegalStateException(
                        "No se pudo preparar la numeración del PDF.", ex);
            }
        }

        @Override
        public void onEndPage(
                PdfWriter escritor,
                Document documento) {

            PdfContentByte contenido = escritor.getDirectContent();

            float x = documento.right();
            float y = documento.bottom() - 20;

            contenido.saveState();
            contenido.setColorStroke(new Color(210, 215, 220));
            contenido.moveTo(
                    documento.left(), y + 13);
            contenido.lineTo(
                    documento.right(), y + 13);
            contenido.stroke();

            contenido.beginText();
            contenido.setFontAndSize(fuente, 8);
            contenido.setColorFill(Color.GRAY);
            contenido.showTextAligned(
                    Element.ALIGN_RIGHT,
                    "FITTRACK - Reporte administrativo",
                    x, y, 0);

            contenido.showTextAligned(
                    Element.ALIGN_RIGHT,
                    "Página " + escritor.getPageNumber() + " de ",
                    x - 48, y - 12, 0);
            contenido.endText();

            contenido.addTemplate(
                    totalPaginas, x - 45, y - 12);

            contenido.restoreState();
        }

        @Override
        public void onCloseDocument(
                PdfWriter escritor,
                Document documento) {

            totalPaginas.beginText();
            totalPaginas.setFontAndSize(fuente, 8);
            totalPaginas.setColorFill(Color.GRAY);
            totalPaginas.showText(
                    String.valueOf(escritor.getPageNumber() - 1));
            totalPaginas.endText();
        }
    }
}
