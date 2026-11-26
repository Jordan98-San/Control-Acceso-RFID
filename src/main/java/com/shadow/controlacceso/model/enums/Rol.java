package com.shadow.controlacceso.model.enums;

/**
 * Rol o puesto del trabajador dentro de la obra.
 *
 * <p>Por ahora es solo informativo: el permiso real lo da el {@link Nivel}.
 * (Ver README, "Preguntas para el equipo": si el rol deberia definir el nivel).</p>
 */
public enum Rol {

    OBRERO("Obrero"),
    OPERADOR_MAQUINARIA("Operador de maquinaria"),
    ELECTRICISTA("Electricista"),
    SUPERVISOR("Supervisor"),
    VISITANTE("Visitante");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
