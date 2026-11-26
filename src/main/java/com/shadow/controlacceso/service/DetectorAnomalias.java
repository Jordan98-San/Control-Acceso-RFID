package com.shadow.controlacceso.service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.shadow.controlacceso.model.Alerta;
import com.shadow.controlacceso.model.RegistroAcceso;
import com.shadow.controlacceso.model.enums.Resultado;
import com.shadow.controlacceso.model.enums.TipoAlerta;
import com.shadow.controlacceso.repository.RegistroRepository;

/**
 * Revisa cada registro de acceso y decide si genera alertas.
 *
 * <p>NO guarda nada: solo devuelve las alertas. Quien lo llama decide que hacer
 * con ellas (guardarlas, mostrarlas...). Eso lo hace facil de probar.</p>
 *
 * <p>Reglas implementadas en la demo:</p>
 * <ol>
 *   <li>Tarjeta desconocida -> alerta.</li>
 *   <li>Tarjeta bloqueada -> alerta.</li>
 *   <li>N o mas intentos denegados con la misma tarjeta en una ventana de tiempo -> alerta.</li>
 *   <li>Acceso PERMITIDO fuera del horario laboral -> alerta (se deja pasar, pero se avisa).</li>
 * </ol>
 */
public class DetectorAnomalias {

    private final RegistroRepository registroRepository;
    private final int umbralIntentosFallidos;
    private final Duration ventanaIntentos;
    private final LocalTime inicioJornada;
    private final LocalTime finJornada;

    public DetectorAnomalias(RegistroRepository registroRepository,
                             int umbralIntentosFallidos,
                             Duration ventanaIntentos,
                             LocalTime inicioJornada,
                             LocalTime finJornada) {
        this.registroRepository = registroRepository;
        this.umbralIntentosFallidos = umbralIntentosFallidos;
        this.ventanaIntentos = ventanaIntentos;
        this.inicioJornada = inicioJornada;
        this.finJornada = finJornada;
    }

    /** @param registro un registro YA guardado (con id) */
    public List<Alerta> analizar(RegistroAcceso registro) {
        List<Alerta> alertas = new ArrayList<>();

        switch (registro.resultado()) {
            case DENEGADO_TARJETA_DESCONOCIDA -> alertas.add(crear(registro, TipoAlerta.TARJETA_DESCONOCIDA,
                    "Se intento ingresar con una tarjeta no registrada"));
            case DENEGADO_TARJETA_INACTIVA -> alertas.add(crear(registro, TipoAlerta.TARJETA_INACTIVA,
                    "Se intento ingresar con una tarjeta bloqueada"));
            default -> { /* los demas resultados no generan alerta por si solos */ }
        }

        if (!registro.resultado().esPermitido()) {
            long fallidos = registroRepository.contarDenegadosDesde(
                    registro.uidHash(), registro.fechaHora().minus(ventanaIntentos));
            if (fallidos >= umbralIntentosFallidos) {
                alertas.add(crear(registro, TipoAlerta.INTENTOS_REPETIDOS,
                        fallidos + " intentos denegados en los ultimos " + ventanaIntentos.toMinutes() + " min"));
            }
        }

        if (registro.resultado() == Resultado.PERMITIDO && fueraDeHorario(registro.fechaHora().toLocalTime())) {
            alertas.add(crear(registro, TipoAlerta.FUERA_DE_HORARIO,
                    "Ingreso a las " + registro.fechaHora().toLocalTime().withNano(0)
                            + " (jornada " + inicioJornada + " - " + finJornada + ")"));
        }

        return alertas;
    }

    private boolean fueraDeHorario(LocalTime hora) {
        return hora.isBefore(inicioJornada) || !hora.isBefore(finJornada);
    }

    private static Alerta crear(RegistroAcceso registro, TipoAlerta tipo, String mensaje) {
        return new Alerta(null, tipo, mensaje, registro.fechaHora(), registro.id());
    }
}
