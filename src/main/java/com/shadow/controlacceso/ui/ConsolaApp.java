package com.shadow.controlacceso.ui;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

import com.shadow.controlacceso.config.AppConfig;
import com.shadow.controlacceso.model.Alerta;
import com.shadow.controlacceso.model.Zona;
import com.shadow.controlacceso.reader.SimuladorLectorRFID;
import com.shadow.controlacceso.report.FilaRegistro;
import com.shadow.controlacceso.service.ControlAccesoService;
import com.shadow.controlacceso.service.ReporteService;
import com.shadow.controlacceso.service.ResultadoLectura;

/**
 * Interfaz de texto. Hace lo mismo que la ventana pero con un menu; util en
 * servidores sin pantalla o para probar rapido desde la terminal.
 */
public class ConsolaApp {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ControlAccesoService controlAcceso;
    private final ReporteService reportes;
    private final SimuladorLectorRFID lector;
    private final Scanner entrada = new Scanner(System.in);

    public ConsolaApp(AppConfig config) {
        this.controlAcceso = config.getControlAccesoService();
        this.reportes = config.getReporteService();
        this.lector = config.getLector();
        // Cuando el lector "lee" una tarjeta, se procesa y se muestra el resultado
        lector.setOyente(lectura -> mostrar(controlAcceso.procesarLectura(lectura)));
    }

    public void iniciar() {
        System.out.println("=== Control de acceso RFID - Obra (modo consola) ===");
        while (true) {
            System.out.println();
            System.out.println("1) Pasar tarjeta");
            System.out.println("2) Ver ultimos registros");
            System.out.println("3) Ver alertas");
            System.out.println("4) Generar reporte PDF");
            System.out.println("0) Salir");
            switch (leerLinea("> ")) {
                case "1" -> pasarTarjeta();
                case "2" -> verRegistros();
                case "3" -> verAlertas();
                case "4" -> generarReporte();
                case "0", "" -> { return; }
                default -> System.out.println("Opcion invalida");
            }
        }
    }

    private void pasarTarjeta() {
        List<String> uids = SimuladorLectorRFID.UIDS_DEMO;
        System.out.println("Tarjetas:");
        for (int i = 0; i < uids.size(); i++) {
            String titular = controlAcceso.buscarTitular(uids.get(i)).map(Object::toString).orElse("?");
            System.out.printf("  %d) %s  %s%n", i + 1, uids.get(i), titular);
        }
        System.out.printf("  %d) Tarjeta desconocida (aleatoria)%n", uids.size() + 1);
        int t = leerNumero("Tarjeta: ", 1, uids.size() + 1);
        String uid = t <= uids.size() ? uids.get(t - 1) : lector.generarUidAleatorio();

        List<Zona> zonas = controlAcceso.listarZonas();
        System.out.println("Zonas:");
        for (int i = 0; i < zonas.size(); i++) {
            System.out.printf("  %d) %s%n", i + 1, zonas.get(i));
        }
        Zona zona = zonas.get(leerNumero("Zona: ", 1, zonas.size()) - 1);

        boolean nocturno = leerLinea("Simular hora nocturna 23:00? (s/N): ").equalsIgnoreCase("s");
        LocalDateTime hora = nocturno ? LocalDateTime.now().withHour(23).withMinute(0).withSecond(0).withNano(0) : LocalDateTime.now();

        lector.simularLectura(uid, zona.getCodigo(), hora);   // dispara el oyente -> mostrar(...)
    }

    private void mostrar(ResultadoLectura r) {
        System.out.println(">>> " + r.mensaje());
        for (Alerta a : r.alertas()) {
            System.out.println("    ALERTA " + a.tipo() + ": " + a.mensaje());
        }
    }

    private void verRegistros() {
        List<FilaRegistro> filas = reportes.registrosDetallados(20);
        if (filas.isEmpty()) {
            System.out.println("(sin registros)");
        }
        for (FilaRegistro f : filas) {
            System.out.printf("%s | %-22s | %-15s | %s%n",
                    FORMATO.format(f.fechaHora()), f.zona(), f.trabajador(), f.resultado().getDescripcion());
        }
    }

    private void verAlertas() {
        List<Alerta> alertas = reportes.alertasRecientes(20);
        if (alertas.isEmpty()) {
            System.out.println("(sin alertas)");
        }
        for (Alerta a : alertas) {
            System.out.printf("%s | %-20s | %s%n", FORMATO.format(a.fechaHora()), a.tipo(), a.mensaje());
        }
    }

    private void generarReporte() {
        try {
            Path archivo = reportes.generarReporte(AppConfig.CARPETA_REPORTES, 500);
            System.out.println("Reporte generado: " + archivo.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error generando reporte: " + e.getMessage());
        }
    }

    // --- utilidades de lectura -------------------------------------------------

    private String leerLinea(String prompt) {
        System.out.print(prompt);
        return entrada.hasNextLine() ? entrada.nextLine().trim() : "";
    }

    private int leerNumero(String prompt, int min, int max) {
        while (true) {
            String texto = leerLinea(prompt);
            try {
                int n = Integer.parseInt(texto);
                if (n >= min && n <= max) {
                    return n;
                }
            } catch (NumberFormatException ignorada) {
                // se vuelve a preguntar
            }
            if (!entrada.hasNextLine() && texto.isEmpty()) {
                throw new IllegalStateException("Entrada terminada");
            }
            System.out.println("Ingrese un numero entre " + min + " y " + max);
        }
    }
}
