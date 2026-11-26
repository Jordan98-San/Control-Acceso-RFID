package com.shadow.controlacceso.report;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Estrategia para "dibujar" un reporte en algun formato.
 *
 * <p>{@code ReporteService} depende de esta interfaz, no de PDF. Para agregar un
 * reporte en CSV o Excel basta con crear otra implementacion (ej. {@code CsvReporteGenerator})
 * y pasarla en {@code AppConfig} (patron Strategy).</p>
 */
public interface ReporteGenerator {

    /** Extension del archivo que produce, sin punto (ej. "pdf"). */
    String getExtension();

    /** Escribe el reporte en {@code salida}. No cierra el stream. */
    void generar(DatosReporte datos, OutputStream salida) throws IOException;
}
