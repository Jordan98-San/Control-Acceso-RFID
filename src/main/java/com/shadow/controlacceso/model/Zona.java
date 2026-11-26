package com.shadow.controlacceso.model;

import java.util.Objects;

import com.shadow.controlacceso.model.enums.Nivel;

/**
 * Area fisica de la obra protegida por un lector RFID
 * (ingreso principal, bodega, grua, cuarto electrico...).
 */
public class Zona {

    private Long id;
    private final String codigo;   // ej. "Z-BOD"; es lo que "conoce" el lector instalado en la puerta
    private String nombre;
    private Nivel nivelRequerido;

    public Zona(Long id, String codigo, String nombre, Nivel nivelRequerido) {
        this.id = id;
        this.codigo = Objects.requireNonNull(codigo, "codigo");
        this.nombre = Objects.requireNonNull(nombre, "nombre");
        this.nivelRequerido = Objects.requireNonNull(nivelRequerido, "nivelRequerido");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public Nivel getNivelRequerido() { return nivelRequerido; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Zona z && codigo.equals(z.codigo));
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }

    /** Se usa directamente en los combos de la interfaz grafica. */
    @Override
    public String toString() {
        return nombre + " [" + nivelRequerido.getDescripcion() + "]";
    }
}
