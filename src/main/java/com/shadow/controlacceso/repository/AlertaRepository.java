package com.shadow.controlacceso.repository;

import java.util.List;

import com.shadow.controlacceso.model.Alerta;

public interface AlertaRepository {

    Alerta guardar(Alerta alerta);

    List<Alerta> listarRecientes(int limite);
}
