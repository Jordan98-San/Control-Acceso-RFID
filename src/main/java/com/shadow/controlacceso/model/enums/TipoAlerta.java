package com.shadow.controlacceso.model.enums;

/** Tipos de anomalia que puede detectar {@code DetectorAnomalias}. */
public enum TipoAlerta {

    TARJETA_DESCONOCIDA("Se uso una tarjeta que no esta registrada"),
    TARJETA_INACTIVA("Se uso una tarjeta bloqueada"),
    INTENTOS_REPETIDOS("Varios intentos fallidos seguidos con la misma tarjeta"),
    FUERA_DE_HORARIO("Acceso fuera del horario laboral");

    private final String descripcion;

    TipoAlerta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
