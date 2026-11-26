package com.shadow.controlacceso.model.enums;

/**
 * Resultado de un intento de acceso. Hay un solo valor positivo y varios
 * negativos, uno por cada motivo de rechazo, para que el registro explique POR QUE se denego.
 */
public enum Resultado {

    PERMITIDO("Acceso permitido"),
    DENEGADO_TARJETA_DESCONOCIDA("Tarjeta no registrada"),
    DENEGADO_TARJETA_INACTIVA("Tarjeta bloqueada / inactiva"),
    DENEGADO_NIVEL_INSUFICIENTE("Nivel de acceso insuficiente para la zona");

    private final String descripcion;

    Resultado(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean esPermitido() {
        return this == PERMITIDO;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
