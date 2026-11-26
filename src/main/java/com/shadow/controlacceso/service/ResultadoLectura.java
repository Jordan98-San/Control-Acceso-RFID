package com.shadow.controlacceso.service;

import java.util.List;
import java.util.Optional;

import com.shadow.controlacceso.model.Alerta;
import com.shadow.controlacceso.model.RegistroAcceso;
import com.shadow.controlacceso.model.Trabajador;
import com.shadow.controlacceso.model.Zona;

/**
 * Todo lo que la interfaz necesita saber despues de procesar una lectura:
 * el registro guardado, quien era (si se sabe), la zona y las alertas generadas.
 */
public record ResultadoLectura(
        RegistroAcceso registro,
        Optional<Trabajador> trabajador,
        Zona zona,
        List<Alerta> alertas) {

    public boolean permitido() {
        return registro.resultado().esPermitido();
    }

    /** Texto corto para mostrar en pantalla. */
    public String mensaje() {
        String quien = trabajador.map(Trabajador::getNombre).orElse("Desconocido");
        return (permitido() ? "PERMITIDO" : "DENEGADO") + " - " + quien + " -> " + zona.getNombre()
                + " (" + registro.resultado().getDescripcion() + ")";
    }
}
