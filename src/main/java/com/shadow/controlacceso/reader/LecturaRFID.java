package com.shadow.controlacceso.reader;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Lo que "dice" un lector cuando alguien acerca una tarjeta:
 * que UID leyo, en que zona esta instalado y cuando.
 *
 * @param uid         UID tal como lo entrega el hardware (ej. "04:A3:1F:7B")
 * @param codigoZona  codigo de la zona donde esta el lector (ej. "Z-BOD")
 * @param fechaHora   momento de la lectura
 */
public record LecturaRFID(String uid, String codigoZona, LocalDateTime fechaHora) {

    public LecturaRFID {
        Objects.requireNonNull(uid, "uid");
        Objects.requireNonNull(codigoZona, "codigoZona");
        Objects.requireNonNull(fechaHora, "fechaHora");
    }
}
