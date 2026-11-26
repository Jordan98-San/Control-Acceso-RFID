package com.shadow.controlacceso.reader;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Lector "de mentira" para probar el sistema sin hardware.
 * La interfaz grafica (o la consola) llama a {@link #simularLectura} cuando
 * el usuario presiona "Pasar tarjeta".
 */
public class SimuladorLectorRFID implements LectorRFID {

    /**
     * UIDs impresos en las tarjetas de demo. Coinciden con los hashes de seed.sql.
     * La BD solo tiene los hashes; estos UIDs "en claro" solo existen en las tarjetas fisicas.
     */
    public static final List<String> UIDS_DEMO = List.of(
            "04:A3:1F:7B",
            "04:B7:22:9C",
            "04:C1:5E:03",
            "04:D9:8A:44",
            "04:E2:10:F5");

    private final Random random = new Random();
    private Consumer<LecturaRFID> oyente = lectura -> { };  // por defecto no hace nada

    @Override
    public void setOyente(Consumer<LecturaRFID> oyente) {
        this.oyente = oyente;
    }

    @Override
    public String getNombre() {
        return "Simulador de lector RFID";
    }

    /** Simula que alguien acerca la tarjeta {@code uid} al lector de la zona, ahora mismo. */
    public void simularLectura(String uid, String codigoZona) {
        simularLectura(uid, codigoZona, LocalDateTime.now());
    }

    /** Igual que el anterior pero permite "fingir" la hora (util para probar fuera de horario). */
    public void simularLectura(String uid, String codigoZona, LocalDateTime fechaHora) {
        oyente.accept(new LecturaRFID(uid, codigoZona, fechaHora));
    }

    /** Genera un UID con formato valido que casi seguro no esta registrado. */
    public String generarUidAleatorio() {
        byte[] bytes = new byte[4];
        random.nextBytes(bytes);
        return String.format("%02X:%02X:%02X:%02X", bytes[0], bytes[1], bytes[2], bytes[3]);
    }
}
