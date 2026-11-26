package com.shadow.controlacceso.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.shadow.controlacceso.model.enums.Resultado;

/**
 * Un intento de acceso (exitoso o no). Es un <b>evento</b>: una vez que ocurrio no cambia,
 * por eso es un {@code record} inmutable.
 *
 * @param id         lo asigna la BD (null antes de guardar)
 * @param uidHash    hash de la tarjeta leida (se guarda aunque la tarjeta no exista,
 *                   para poder detectar intentos repetidos)
 * @param tarjetaId  null si la tarjeta es desconocida
 * @param zonaId     zona donde se intento entrar
 * @param fechaHora  momento de la lectura
 * @param resultado  permitido o motivo del rechazo
 */
public record RegistroAcceso(
        Long id,
        String uidHash,
        Long tarjetaId,
        long zonaId,
        LocalDateTime fechaHora,
        Resultado resultado) {

    public RegistroAcceso {
        Objects.requireNonNull(uidHash, "uidHash");
        Objects.requireNonNull(fechaHora, "fechaHora");
        Objects.requireNonNull(resultado, "resultado");
    }

    /** Devuelve una copia con el id generado por la base de datos. */
    public RegistroAcceso conId(long nuevoId) {
        return new RegistroAcceso(nuevoId, uidHash, tarjetaId, zonaId, fechaHora, resultado);
    }
}
