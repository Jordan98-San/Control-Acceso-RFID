package com.shadow.controlacceso.repository.jdbc;

/**
 * Envuelve las {@link java.sql.SQLException} (que son "checked") en una excepcion
 * no comprobada. Asi las interfaces de repositorio no quedan atadas a JDBC: un
 * repositorio en memoria o con JPA no tendria por que lanzar SQLException.
 */
public class DatosAccesoException extends RuntimeException {

    public DatosAccesoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
