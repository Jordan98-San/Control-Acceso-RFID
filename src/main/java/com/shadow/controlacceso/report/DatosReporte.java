package com.shadow.controlacceso.report;

import java.time.LocalDateTime;
import java.util.List;

import com.shadow.controlacceso.model.Alerta;

/**
 * Todo lo que necesita un {@link ReporteGenerator} para dibujar un reporte.
 * El generador NO consulta la base de datos: recibe los datos ya preparados.
 */
public record DatosReporte(
        String titulo,
        LocalDateTime generadoEn,
        List<FilaRegistro> registros,
        List<Alerta> alertas) {

    public long totalPermitidos() {
        return registros.stream().filter(r -> r.resultado().esPermitido()).count();
    }

    public long totalDenegados() {
        return registros.size() - totalPermitidos();
    }
}
