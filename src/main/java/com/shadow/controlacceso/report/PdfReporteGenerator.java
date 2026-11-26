package com.shadow.controlacceso.report;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shadow.controlacceso.model.Alerta;

/** Implementacion de {@link ReporteGenerator} que produce un PDF con OpenPDF. */
public class PdfReporteGenerator implements ReporteGenerator {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Color GRIS = new Color(230, 230, 230);
    private static final Color VERDE = new Color(0, 120, 60);
    private static final Color ROJO = new Color(180, 30, 30);

    private final Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private final Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private final Font normal = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private final Font negrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

    @Override
    public String getExtension() {
        return "pdf";
    }

    @Override
    public void generar(DatosReporte datos, OutputStream salida) throws IOException {
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, salida);
            writer.setCloseStream(false);   // el stream lo cierra quien lo abrio
            doc.open();

            doc.add(new Paragraph(datos.titulo(), titulo));
            doc.add(new Paragraph("Generado: " + FORMATO.format(datos.generadoEn()), normal));
            doc.add(new Paragraph(String.format("Registros: %d   |   Permitidos: %d   |   Denegados: %d   |   Alertas: %d",
                    datos.registros().size(), datos.totalPermitidos(), datos.totalDenegados(), datos.alertas().size()),
                    normal));

            doc.add(seccion("Registros de acceso"));
            doc.add(tablaRegistros(datos));

            doc.add(seccion("Alertas"));
            doc.add(tablaAlertas(datos));
        } catch (DocumentException e) {
            throw new IOException("No se pudo generar el PDF", e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
    }

    private Paragraph seccion(String texto) {
        Paragraph p = new Paragraph(texto, subtitulo);
        p.setSpacingBefore(14);
        p.setSpacingAfter(6);
        return p;
    }

    private PdfPTable tablaRegistros(DatosReporte datos) {
        PdfPTable tabla = new PdfPTable(new float[] {2.2f, 2.5f, 2.5f, 3.2f});
        tabla.setWidthPercentage(100);
        encabezados(tabla, "Fecha / hora", "Zona", "Trabajador", "Resultado");

        for (FilaRegistro f : datos.registros()) {
            tabla.addCell(celda(FORMATO.format(f.fechaHora()), normal));
            tabla.addCell(celda(f.zona(), normal));
            tabla.addCell(celda(f.trabajador(), normal));
            Font color = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9,
                    f.resultado().esPermitido() ? VERDE : ROJO);
            tabla.addCell(celda(f.resultado().getDescripcion(), color));
        }
        if (datos.registros().isEmpty()) {
            filaVacia(tabla, 4, "Sin registros");
        }
        return tabla;
    }

    private PdfPTable tablaAlertas(DatosReporte datos) {
        PdfPTable tabla = new PdfPTable(new float[] {2.2f, 3.0f, 5.2f});
        tabla.setWidthPercentage(100);
        encabezados(tabla, "Fecha / hora", "Tipo", "Detalle");

        for (Alerta a : datos.alertas()) {
            tabla.addCell(celda(FORMATO.format(a.fechaHora()), normal));
            tabla.addCell(celda(a.tipo().name(), negrita));
            tabla.addCell(celda(a.mensaje(), normal));
        }
        if (datos.alertas().isEmpty()) {
            filaVacia(tabla, 3, "Sin alertas");
        }
        return tabla;
    }

    private void encabezados(PdfPTable tabla, String... titulos) {
        for (String t : titulos) {
            PdfPCell c = celda(t, negrita);
            c.setBackgroundColor(GRIS);
            tabla.addCell(c);
        }
        tabla.setHeaderRows(1);   // se repite en cada pagina
    }

    private void filaVacia(PdfPTable tabla, int columnas, String texto) {
        PdfPCell c = celda(texto, normal);
        c.setColspan(columnas);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(c);
    }

    private static PdfPCell celda(String texto, Font fuente) {
        PdfPCell c = new PdfPCell(new Phrase(texto, fuente));
        c.setPadding(4);
        return c;
    }
}
