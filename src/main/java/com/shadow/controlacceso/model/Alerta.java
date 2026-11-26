package com.shadow.controlacceso.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.shadow.controlacceso.model.enums.TipoAlerta;

/**
 * Anomalia detectada a partir de un {@link RegistroAcceso}. Igual que el registro,
 * es un evento inmutable.
 *
 * @param registroId registro que la provoco
 */
public record Alerta(
        Long id,
        TipoAlerta tipo,
        String mensaje,
        LocalDateTime fechaHora,
        long registroId) {

    public Alerta {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(mensaje, "mensaje");
        Objects.requireNonNull(fechaHora, "fechaHora");
    }

    public Alerta conId(long nuevoId) {
        return new Alerta(nuevoId, tipo, mensaje, fechaHora, registroId);
    }
}
