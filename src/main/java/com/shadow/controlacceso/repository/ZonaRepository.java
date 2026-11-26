package com.shadow.controlacceso.repository;

import java.util.List;
import java.util.Optional;

import com.shadow.controlacceso.model.Zona;

public interface ZonaRepository {

    Optional<Zona> buscarPorId(long id);

    Optional<Zona> buscarPorCodigo(String codigo);

    List<Zona> listarTodas();
}
