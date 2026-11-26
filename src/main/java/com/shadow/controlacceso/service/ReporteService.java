package com.shadow.controlacceso.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.shadow.controlacceso.model.Alerta;
import com.shadow.controlacceso.model.RegistroAcceso;
import com.shadow.controlacceso.model.TarjetaRFID;
import com.shadow.controlacceso.model.Trabajador;
import com.shadow.controlacceso.model.Zona;
import com.shadow.controlacceso.report.DatosReporte;
import com.shadow.controlacceso.report.FilaRegistro;
import com.shadow.controlacceso.report.ReporteGenerator;
import com.shadow.controlacceso.repository.AlertaRepository;
import com.shadow.controlacceso.repository.RegistroRepository;
import com.shadow.controlacceso.repository.TarjetaRepository;
import com.shadow.controlacceso.repository.TrabajadorRepository;
import com.shadow.controlacceso.repository.ZonaRepository;

/**
 * Prepara los datos de consulta/reporte (convierte ids en nombres) y delega el
 * "dibujo" del archivo en un {@link ReporteGenerator}.
 */
public class ReporteService {

    private static final DateTimeFormatter NOMBRE_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final RegistroRepository registroRepository;
    private final AlertaRepository alertaRepository;
    private final ZonaRepository zonaRepository;
    private final TarjetaRepository tarjetaRepository;
    private final TrabajadorRepository trabajadorRepository;
    private final ReporteGenerator generador;

    public ReporteService(RegistroRepository registroRepository,
                          AlertaRepository alertaRepository,
                          ZonaRepository zonaRepository,
                          TarjetaRepository tarjetaRepository,
                          TrabajadorRepository trabajadorRepository,
                          ReporteGenerator generador) {
        this.registroRepository = registroRepository;
        this.alertaRepository = alertaRepository;
        this.zonaRepository = zonaRepository;
        this.tarjetaRepository = tarjetaRepository;
        this.trabajadorRepository = trabajadorRepository;
        this.generador = generador;
    }

    /**
     * Ultimos registros con nombres legibles. Carga zonas/tarjetas/trabajadores una sola vez
     * en mapas (son pocos) en vez de hacer una consulta por cada fila.
     */
    public List<FilaRegistro> registrosDetallados(int limite) {
        Map<Long, String> zonas = zonaRepository.listarTodas().stream()
                .collect(Collectors.toMap(Zona::getId, Zona::getNombre));
        Map<Long, Trabajador> trabajadores = trabajadorRepository.listarTodos().stream()
                .collect(Collectors.toMap(Trabajador::getId, Function.identity()));
        Map<Long, Long> tarjetaATrabajador = tarjetaRepository.listarTodas().stream()
                .collect(Collectors.toMap(TarjetaRFID::getId, TarjetaRFID::getTrabajadorId));

        return registroRepository.listarRecientes(limite).stream()
                .map(r -> new FilaRegistro(
                        r.fechaHora(),
                        zonas.getOrDefault(r.zonaId(), "?"),
                        nombreTrabajador(r, tarjetaATrabajador, trabajadores),
                        r.resultado()))
                .toList();
    }

    public List<Alerta> alertasRecientes(int limite) {
        return alertaRepository.listarRecientes(limite);
    }

    /**
     * Genera el reporte en {@code carpeta} y devuelve la ruta del archivo creado.
     * El formato lo decide el generador inyectado (hoy PDF).
     */
    public Path generarReporte(Path carpeta, int limiteRegistros) throws IOException {
        LocalDateTime ahora = LocalDateTime.now();
        DatosReporte datos = new DatosReporte(
                "Reporte de control de acceso - Obra",
                ahora,
                registrosDetallados(limiteRegistros),
                alertasRecientes(limiteRegistros));

        Files.createDirectories(carpeta);
        Path archivo = carpeta.resolve("reporte-" + NOMBRE_ARCHIVO.format(ahora) + "." + generador.getExtension());
        try (OutputStream salida = Files.newOutputStream(archivo)) {
            generador.generar(datos, salida);
        }
        return archivo;
    }

    private static String nombreTrabajador(RegistroAcceso r, Map<Long, Long> tarjetaATrabajador,
                                           Map<Long, Trabajador> trabajadores) {
        if (r.tarjetaId() == null) {
            return "Desconocido";
        }
        Trabajador t = trabajadores.get(tarjetaATrabajador.get(r.tarjetaId()));
        return t == null ? "?" : t.getNombre();
    }
}
