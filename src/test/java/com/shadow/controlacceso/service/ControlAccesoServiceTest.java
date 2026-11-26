package com.shadow.controlacceso.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shadow.controlacceso.config.AppConfig;
import com.shadow.controlacceso.model.TarjetaRFID;
import com.shadow.controlacceso.model.enums.Resultado;
import com.shadow.controlacceso.model.enums.TipoAlerta;
import com.shadow.controlacceso.reader.LecturaRFID;

/**
 * Pruebas de las reglas de acceso usando los datos de seed.sql.
 * Cada prueba usa una base de datos en memoria NUEVA (nombre aleatorio), asi no se afectan entre si.
 */
class ControlAccesoServiceTest {

    // UIDs de seed.sql
    private static final String CARLOS_BASICO = "04:A3:1F:7B";
    private static final String LUIS_CRITICO = "04:C1:5E:03";
    private static final String JORGE_BLOQUEADA = "04:E2:10:F5";

    /** Un dia laboral a media manana, para que no salte la alerta de horario. */
    private static final LocalDateTime HORA_LABORAL = LocalDateTime.of(2026, 11, 26, 10, 0);

    private ControlAccesoService servicio;
    private ReporteService reportes;

    @BeforeEach
    void prepararBaseDeDatos() {
        AppConfig config = new AppConfig("jdbc:h2:mem:test-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        config.inicializarBaseDeDatos();
        servicio = config.getControlAccesoService();
        reportes = config.getReporteService();
    }

    private ResultadoLectura pasar(String uid, String zona, LocalDateTime hora) {
        return servicio.procesarLectura(new LecturaRFID(uid, zona, hora));
    }

    @Test
    void permiteAccesoConNivelSuficiente() {
        ResultadoLectura r = pasar(CARLOS_BASICO, "Z-ING", HORA_LABORAL);

        assertEquals(Resultado.PERMITIDO, r.registro().resultado());
        assertEquals("Carlos Perez", r.trabajador().orElseThrow().getNombre());
        assertTrue(r.alertas().isEmpty());
    }

    @Test
    void aceptaElUidEnCualquierFormato() {
        ResultadoLectura r = pasar("04a31f7b", "Z-ING", HORA_LABORAL);

        assertEquals(Resultado.PERMITIDO, r.registro().resultado());
    }

    @Test
    void niegaAccesoConNivelInsuficiente() {
        ResultadoLectura r = pasar(CARLOS_BASICO, "Z-ELE", HORA_LABORAL);

        assertEquals(Resultado.DENEGADO_NIVEL_INSUFICIENTE, r.registro().resultado());
    }

    @Test
    void tarjetaDesconocidaSeNiegaYGeneraAlerta() {
        ResultadoLectura r = pasar("DE:AD:BE:EF", "Z-ING", HORA_LABORAL);

        assertEquals(Resultado.DENEGADO_TARJETA_DESCONOCIDA, r.registro().resultado());
        assertTrue(r.trabajador().isEmpty());
        assertEquals(TipoAlerta.TARJETA_DESCONOCIDA, r.alertas().get(0).tipo());
    }

    @Test
    void tarjetaBloqueadaSeNiega() {
        ResultadoLectura r = pasar(JORGE_BLOQUEADA, "Z-ING", HORA_LABORAL);

        assertEquals(Resultado.DENEGADO_TARJETA_INACTIVA, r.registro().resultado());
        assertEquals(TipoAlerta.TARJETA_INACTIVA, r.alertas().get(0).tipo());
    }

    @Test
    void tresIntentosFallidosGeneranAlertaDeIntentosRepetidos() {
        pasar(CARLOS_BASICO, "Z-ELE", HORA_LABORAL);
        pasar(CARLOS_BASICO, "Z-ELE", HORA_LABORAL.plusMinutes(1));
        ResultadoLectura tercero = pasar(CARLOS_BASICO, "Z-ELE", HORA_LABORAL.plusMinutes(2));

        assertTrue(tercero.alertas().stream().anyMatch(a -> a.tipo() == TipoAlerta.INTENTOS_REPETIDOS));
    }

    @Test
    void accesoNocturnoSePermitePeroGeneraAlerta() {
        ResultadoLectura r = pasar(LUIS_CRITICO, "Z-ELE", HORA_LABORAL.withHour(23));

        assertTrue(r.permitido());
        assertEquals(TipoAlerta.FUERA_DE_HORARIO, r.alertas().get(0).tipo());
    }

    @Test
    void bloquearUnaTarjetaImpideElAcceso() {
        TarjetaRFID tarjetaCarlos = servicio.listarTarjetas().get(0);
        servicio.alternarEstadoTarjeta(tarjetaCarlos);

        ResultadoLectura r = pasar(CARLOS_BASICO, "Z-ING", HORA_LABORAL);

        assertFalse(r.permitido());
        assertEquals(Resultado.DENEGADO_TARJETA_INACTIVA, r.registro().resultado());
    }

    @Test
    void elHistorialMuestraNombresYDesconocidos() {
        pasar(CARLOS_BASICO, "Z-ING", HORA_LABORAL);
        pasar("DE:AD:BE:EF", "Z-BOD", HORA_LABORAL);

        var filas = reportes.registrosDetallados(10);   // del mas nuevo al mas viejo

        assertEquals("Desconocido", filas.get(0).trabajador());
        assertEquals("Bodega de materiales", filas.get(0).zona());
        assertEquals("Carlos Perez", filas.get(1).trabajador());
    }

    @Test
    void zonaInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> pasar(CARLOS_BASICO, "Z-XXX", HORA_LABORAL));
    }
}
