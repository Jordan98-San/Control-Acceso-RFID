package com.shadow.controlacceso.repository;

import java.util.List;
import java.util.Optional;

import com.shadow.controlacceso.model.Trabajador;

/** Acceso a datos de trabajadores. Los servicios dependen de esta interfaz, no de JDBC. */
public interface TrabajadorRepository {

    Optional<Trabajador> buscarPorId(long id);

    List<Trabajador> listarTodos();
}
