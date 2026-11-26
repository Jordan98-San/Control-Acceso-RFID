package com.shadow.controlacceso.report;

import java.time.LocalDateTime;

import com.shadow.controlacceso.model.enums.Resultado;

/**
 * Un registro de acceso "listo para mostrar": con nombres en vez de ids.
 * Lo usan tanto la tabla de la interfaz como los reportes.
 */
public record FilaRegistro(
        LocalDateTime fechaHora,
        String zona,
        String trabajador,
        Resultado resultado) {
}
