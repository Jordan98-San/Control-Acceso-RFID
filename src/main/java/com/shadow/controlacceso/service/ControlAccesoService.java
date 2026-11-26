package com.shadow.controlacceso.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.shadow.controlacceso.model.Alerta;
import com.shadow.controlacceso.model.RegistroAcceso;
import com.shadow.controlacceso.model.TarjetaRFID;
import com.shadow.controlacceso.model.Trabajador;
import com.shadow.controlacceso.model.Zona;
import com.shadow.controlacceso.model.enums.Resultado;
import com.shadow.controlacceso.reader.LecturaRFID;
import com.shadow.controlacceso.repository.AlertaRepository;
import com.shadow.controlacceso.repository.RegistroRepository;
import com.shadow.controlacceso.repository.TarjetaRepository;
import com.shadow.controlacceso.repository.TrabajadorRepository;
import com.shadow.controlacceso.repository.ZonaRepository;

/**
 * Corazon del sistema: recibe una lectura del lector, decide si se permite el acceso,
 * guarda el registro y dispara la deteccion de anomalias.
 *
 * <p>Solo depende de INTERFACES (repositorios), nunca de clases JDBC. Todas las
 * dependencias llegan por el constructor (inyeccion de dependencias "a mano",
 * el armado esta en {@code AppConfig}).</p>
 */
public class ControlAccesoService {

    private final TarjetaRepository tarjetaRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final ZonaRepository zonaRepository;
    private final RegistroRepository registroRepository;
    private final AlertaRepository alertaRepository;
    private final CriptoService criptoService;
    private final DetectorAnomalias detectorAnomalias;

    public ControlAccesoService(TarjetaRepository tarjetaRepository,
                                TrabajadorRepository trabajadorRepository,
                                ZonaRepository zonaRepository,
                                RegistroRepository registroRepository,
                                AlertaRepository alertaRepository,
                                CriptoService criptoService,
                                DetectorAnomalias detectorAnomalias) {
        this.tarjetaRepository = tarjetaRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.zonaRepository = zonaRepository;
        this.registroRepository = registroRepository;
        this.alertaRepository = alertaRepository;
        this.criptoService = criptoService;
        this.detectorAnomalias = detectorAnomalias;
    }

    /**
     * Flujo principal:
     * <pre>
     * UID -> hash -> buscar tarjeta -> ¿existe? -> ¿activa? -> ¿nivel suficiente? -> registrar -> alertas
     * </pre>
     */
    public ResultadoLectura procesarLectura(LecturaRFID lectura) {
        Zona zona = zonaRepository.buscarPorCodigo(lectura.codigoZona())
                .orElseThrow(() -> new IllegalArgumentException("Zona no existe: " + lectura.codigoZona()));

        String uidHash = criptoService.hashUid(lectura.uid());
        Optional<TarjetaRFID> tarjeta = tarjetaRepository.buscarPorUidHash(uidHash);
        Optional<Trabajador> trabajador = tarjeta.flatMap(t -> trabajadorRepository.buscarPorId(t.getTrabajadorId()));

        Resultado resultado = evaluar(tarjeta, trabajador, zona);

        RegistroAcceso registro = registroRepository.guardar(new RegistroAcceso(
                null, uidHash, tarjeta.map(TarjetaRFID::getId).orElse(null),
                zona.getId(), lectura.fechaHora(), resultado));

        List<Alerta> alertas = new ArrayList<>();
        for (Alerta alerta : detectorAnomalias.analizar(registro)) {
            alertas.add(alertaRepository.guardar(alerta));
        }

        return new ResultadoLectura(registro, trabajador, zona, alertas);
    }

    /** Las reglas de acceso, en orden. El primer "no" gana. */
    private Resultado evaluar(Optional<TarjetaRFID> tarjeta, Optional<Trabajador> trabajador, Zona zona) {
        if (tarjeta.isEmpty() || trabajador.isEmpty()) {
            return Resultado.DENEGADO_TARJETA_DESCONOCIDA;
        }
        if (!tarjeta.get().isActiva()) {
            return Resultado.DENEGADO_TARJETA_INACTIVA;
        }
        if (!trabajador.get().puedeIngresarA(zona)) {
            return Resultado.DENEGADO_NIVEL_INSUFICIENTE;
        }
        return Resultado.PERMITIDO;
    }

    // --- Consultas de apoyo para la interfaz --------------------------------

    /** ¿De quien es esta tarjeta? (sirve para etiquetar las tarjetas de demo en la UI). */
    public Optional<Trabajador> buscarTitular(String uid) {
        return tarjetaRepository.buscarPorUidHash(criptoService.hashUid(uid))
                .flatMap(t -> trabajadorRepository.buscarPorId(t.getTrabajadorId()));
    }

    public Optional<Trabajador> buscarTrabajador(long id) {
        return trabajadorRepository.buscarPorId(id);
    }

    public List<Zona> listarZonas() {
        return zonaRepository.listarTodas();
    }

    public List<TarjetaRFID> listarTarjetas() {
        return tarjetaRepository.listarTodas();
    }

    /** Bloquea la tarjeta si esta activa, o la activa si esta bloqueada. */
    public void alternarEstadoTarjeta(TarjetaRFID tarjeta) {
        if (tarjeta.isActiva()) {
            tarjeta.bloquear();
        } else {
            tarjeta.activar();
        }
        tarjetaRepository.actualizarEstado(tarjeta);
    }
}
