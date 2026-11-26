package com.shadow.controlacceso.reader;

import java.util.function.Consumer;

/**
 * Abstraccion de un lector de tarjetas RFID.
 *
 * <p>El resto del sistema solo conoce esta interfaz. Hoy existe
 * {@link SimuladorLectorRFID}; manana se puede escribir, por ejemplo, un
 * {@code LectorSerialRFID} que lea de un Arduino + RC522 por puerto serial,
 * y nada mas del codigo cambia (principio de inversion de dependencias).</p>
 *
 * <p>Funciona con un <b>oyente</b> (patron Observer): el lector avisa cuando lee
 * una tarjeta, en lugar de que alguien le pregunte todo el tiempo.</p>
 */
public interface LectorRFID {

    /** Registra quien va a recibir las lecturas (normalmente el ControlAccesoService). */
    void setOyente(Consumer<LecturaRFID> oyente);

    /** Nombre descriptivo del lector, para mostrar en pantalla o en logs. */
    String getNombre();
}
