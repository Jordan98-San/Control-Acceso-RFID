package com.shadow.controlacceso.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.shadow.controlacceso.model.RegistroAcceso;

public interface RegistroRepository {

    /** Inserta el registro y lo devuelve con su id generado. */
    RegistroAcceso guardar(RegistroAcceso registro);

    /** Los ultimos {@code limite} registros, del mas nuevo al mas viejo. */
    List<RegistroAcceso> listarRecientes(int limite);

    /** Cuantos accesos DENEGADOS tuvo esta tarjeta desde {@code desde} (inclusive). */
    long contarDenegadosDesde(String uidHash, LocalDateTime desde);
}
