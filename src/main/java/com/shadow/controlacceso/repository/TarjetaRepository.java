package com.shadow.controlacceso.repository;

import java.util.List;
import java.util.Optional;

import com.shadow.controlacceso.model.TarjetaRFID;

public interface TarjetaRepository {

    /** Busca por el HASH del UID (nunca por el UID en claro). */
    Optional<TarjetaRFID> buscarPorUidHash(String uidHash);

    List<TarjetaRFID> listarTodas();

    /** Guarda el estado activa/bloqueada de una tarjeta existente. */
    void actualizarEstado(TarjetaRFID tarjeta);
}
