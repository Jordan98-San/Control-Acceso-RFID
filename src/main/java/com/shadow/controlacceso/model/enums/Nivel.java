package com.shadow.controlacceso.model.enums;

/**
 * Nivel de acceso (credencial de seguridad).
 *
 * <p>Se usa en dos lados: el {@code Trabajador} TIENE un nivel y la {@code Zona} EXIGE un nivel.
 * Un trabajador puede entrar a una zona si su nivel es igual o mayor al requerido.</p>
 *
 * <p>El {@code peso} es explicito (no se usa {@code ordinal()}) para que reordenar las
 * constantes no cambie las reglas de acceso sin querer.</p>
 */
public enum Nivel {

    BASICO(1, "Basico"),
    RESTRINGIDO(2, "Restringido"),
    CRITICO(3, "Critico");

    private final int peso;
    private final String descripcion;

    Nivel(int peso, String descripcion) {
        this.peso = peso;
        this.descripcion = descripcion;
    }

    /** @return true si este nivel es suficiente para entrar a algo que exige {@code requerido}. */
    public boolean alcanza(Nivel requerido) {
        return this.peso >= requerido.peso;
    }

    public int getPeso() {
        return peso;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
