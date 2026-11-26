package com.shadow.controlacceso.config;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;

import com.shadow.controlacceso.reader.SimuladorLectorRFID;
import com.shadow.controlacceso.report.PdfReporteGenerator;
import com.shadow.controlacceso.repository.AlertaRepository;
import com.shadow.controlacceso.repository.RegistroRepository;
import com.shadow.controlacceso.repository.TarjetaRepository;
import com.shadow.controlacceso.repository.TrabajadorRepository;
import com.shadow.controlacceso.repository.ZonaRepository;
import com.shadow.controlacceso.repository.jdbc.ConexionDB;
import com.shadow.controlacceso.repository.jdbc.JdbcAlertaRepository;
import com.shadow.controlacceso.repository.jdbc.JdbcRegistroRepository;
import com.shadow.controlacceso.repository.jdbc.JdbcTarjetaRepository;
import com.shadow.controlacceso.repository.jdbc.JdbcTrabajadorRepository;
import com.shadow.controlacceso.repository.jdbc.JdbcZonaRepository;
import com.shadow.controlacceso.service.ControlAccesoService;
import com.shadow.controlacceso.service.CriptoService;
import com.shadow.controlacceso.service.DetectorAnomalias;
import com.shadow.controlacceso.service.ReporteService;

/**
 * "Raiz de composicion": el UNICO lugar donde se crean los objetos concretos y se
 * conectan entre si. El resto del codigo recibe sus dependencias por constructor.
 *
 * <p>Si manana se cambia H2 por MySQL, o PDF por CSV, o el simulador por un lector real,
 * el cambio se hace aqui y en ningun otro lado.</p>
 */
public class AppConfig {

    // --- Parametros de la demo (podrian venir de un archivo .properties) ----------
    public static final String URL_BD_POR_DEFECTO = "jdbc:h2:mem:controlacceso;DB_CLOSE_DELAY=-1";
    public static final int UMBRAL_INTENTOS_FALLIDOS = 3;
    public static final Duration VENTANA_INTENTOS = Duration.ofMinutes(5);
    public static final LocalTime INICIO_JORNADA = LocalTime.of(6, 0);
    public static final LocalTime FIN_JORNADA = LocalTime.of(20, 0);
    public static final Path CARPETA_REPORTES = Path.of("reportes");

    private final ConexionDB conexionDB;
    private final ControlAccesoService controlAccesoService;
    private final ReporteService reporteService;
    private final SimuladorLectorRFID lector;

    /** Configuracion normal de la aplicacion. */
    public AppConfig() {
        this(URL_BD_POR_DEFECTO);
    }

    /** Permite otra URL de BD (los tests usan una BD en memoria distinta por cada prueba). */
    public AppConfig(String urlBaseDatos) {
        // 1. Infraestructura
        conexionDB = new ConexionDB(urlBaseDatos, "sa", "");

        // 2. Repositorios (implementaciones JDBC, vistas a traves de sus interfaces)
        TrabajadorRepository trabajadores = new JdbcTrabajadorRepository(conexionDB);
        TarjetaRepository tarjetas = new JdbcTarjetaRepository(conexionDB);
        ZonaRepository zonas = new JdbcZonaRepository(conexionDB);
        RegistroRepository registros = new JdbcRegistroRepository(conexionDB);
        AlertaRepository alertas = new JdbcAlertaRepository(conexionDB);

        // 3. Servicios
        CriptoService cripto = new CriptoService();
        DetectorAnomalias detector = new DetectorAnomalias(
                registros, UMBRAL_INTENTOS_FALLIDOS, VENTANA_INTENTOS, INICIO_JORNADA, FIN_JORNADA);
        controlAccesoService = new ControlAccesoService(
                tarjetas, trabajadores, zonas, registros, alertas, cripto, detector);
        reporteService = new ReporteService(
                registros, alertas, zonas, tarjetas, trabajadores, new PdfReporteGenerator());

        // 4. Hardware (simulado)
        lector = new SimuladorLectorRFID();
    }

    /** Crea las tablas y carga los datos de demo. Llamar una vez al arrancar. */
    public void inicializarBaseDeDatos() {
        conexionDB.inicializar();
    }

    public ControlAccesoService getControlAccesoService() {
        return controlAccesoService;
    }

    public ReporteService getReporteService() {
        return reporteService;
    }

    public SimuladorLectorRFID getLector() {
        return lector;
    }
}
