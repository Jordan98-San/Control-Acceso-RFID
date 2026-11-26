package com.shadow.controlacceso.model;

import java.util.Objects;

import com.shadow.controlacceso.model.enums.Nivel;
import com.shadow.controlacceso.model.enums.Rol;

/**
 * Persona que trabaja en la obra.
 *
 * <p>Es una <b>entidad</b>: tiene identidad propia (la cedula) y podria cambiar con el tiempo
 * (de rol o de nivel), por eso es una clase con encapsulamiento y no un {@code record}.</p>
 */
public class Trabajador {

    private Long id;               // lo asigna la base de datos (null antes de guardar)
    private final String cedula;   // identificador natural, no cambia
    private String nombre;
    private Rol rol;
    private Nivel nivelAcceso;

    public Trabajador(Long id, String cedula, String nombre, Rol rol, Nivel nivelAcceso) {
        this.id = id;
        this.cedula = requerirTexto(cedula, "cedula");
        this.nombre = requerirTexto(nombre, "nombre");
        this.rol = Objects.requireNonNull(rol, "rol");
        this.nivelAcceso = Objects.requireNonNull(nivelAcceso, "nivelAcceso");
    }

    /** Regla de negocio principal: ¿este trabajador puede entrar a la zona? */
    public boolean puedeIngresarA(Zona zona) {
        return nivelAcceso.alcanza(zona.getNivelRequerido());
    }

    private static String requerirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio");
        }
        return valor.trim();
    }

    // --- getters / setters -------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = requerirTexto(nombre, "nombre"); }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = Objects.requireNonNull(rol); }
    public Nivel getNivelAcceso() { return nivelAcceso; }
    public void setNivelAcceso(Nivel nivelAcceso) { this.nivelAcceso = Objects.requireNonNull(nivelAcceso); }

    // Dos objetos Trabajador son "el mismo" si tienen la misma cedula.
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Trabajador t && cedula.equals(t.cedula));
    }

    @Override
    public int hashCode() {
        return cedula.hashCode();
    }

    @Override
    public String toString() {
        return nombre + " (" + rol.getDescripcion() + ", nivel " + nivelAcceso.getDescripcion() + ")";
    }
}
