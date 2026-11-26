package com.shadow.controlacceso.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Tarjeta fisica asignada a un trabajador.
 *
 * <p>Importante: NO se guarda el UID real de la tarjeta, sino su hash SHA-256
 * ({@code uidHash}), calculado por {@code CriptoService}. Asi, si alguien roba la base
 * de datos, no puede clonar las tarjetas.</p>
 */
public class TarjetaRFID {

    private Long id;
    private final String uidHash;
    private final long trabajadorId;
    private boolean activa;
    private final LocalDate fechaEmision;

    public TarjetaRFID(Long id, String uidHash, long trabajadorId, boolean activa, LocalDate fechaEmision) {
        this.id = id;
        this.uidHash = Objects.requireNonNull(uidHash, "uidHash");
        this.trabajadorId = trabajadorId;
        this.activa = activa;
        this.fechaEmision = Objects.requireNonNull(fechaEmision, "fechaEmision");
    }

    /** Por ejemplo, cuando el trabajador reporta la tarjeta como perdida. */
    public void bloquear() {
        this.activa = false;
    }

    public void activar() {
        this.activa = true;
    }

    /** Primeros caracteres del hash, para mostrar en pantalla sin exponer todo. */
    public String getHashCorto() {
        return uidHash.substring(0, 10) + "...";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUidHash() { return uidHash; }
    public long getTrabajadorId() { return trabajadorId; }
    public boolean isActiva() { return activa; }
    public LocalDate getFechaEmision() { return fechaEmision; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof TarjetaRFID t && uidHash.equals(t.uidHash));
    }

    @Override
    public int hashCode() {
        return uidHash.hashCode();
    }

    @Override
    public String toString() {
        return "Tarjeta[" + getHashCorto() + ", " + (activa ? "activa" : "bloqueada") + "]";
    }
}
