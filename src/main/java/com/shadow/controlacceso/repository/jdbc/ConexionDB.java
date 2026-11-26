package com.shadow.controlacceso.repository.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Fabrica de conexiones JDBC.
 *
 * <p><b>Decision de diseno:</b> NO es un singleton que guarda una unica {@code Connection}
 * abierta para siempre. Cada vez que un repositorio necesita la BD llama a
 * {@link #abrir()}, recibe una conexion nueva y la cierra al terminar con
 * try-with-resources:</p>
 *
 * <pre>{@code
 * try (Connection cn = conexionDB.abrir();
 *      PreparedStatement ps = cn.prepareStatement(SQL)) {
 *     ...
 * } // aqui se cierran solas, aunque haya una excepcion
 * }</pre>
 *
 * <p>Ventajas: no hay conexiones "colgadas" ni compartidas entre hilos (la interfaz grafica
 * y los lectores podrian trabajar en hilos distintos), y si la BD se reinicia la siguiente
 * operacion simplemente abre otra conexion. Si en el futuro hace falta rendimiento, se cambia
 * {@code DriverManager} por un pool (HikariCP) aqui mismo, sin tocar los repositorios.</p>
 */
public class ConexionDB {

    private final String url;
    private final String usuario;
    private final String clave;

    public ConexionDB(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    /** Abre una conexion NUEVA. Quien la pide es responsable de cerrarla. */
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    /** Crea las tablas y carga los datos de prueba (schema.sql + seed.sql). */
    public void inicializar() {
        ejecutarScript("/schema.sql");
        ejecutarScript("/seed.sql");
    }

    /**
     * Ejecutor de scripts SQL muy simple: quita comentarios "--" y separa por ';'.
     * Suficiente para nuestros scripts (no soporta ';' dentro de textos).
     */
    private void ejecutarScript(String recurso) {
        List<String> sentencias = Arrays.stream(leerRecurso(recurso).split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        try (Connection cn = abrir(); Statement st = cn.createStatement()) {
            for (String sql : sentencias) {
                st.execute(sql);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error ejecutando " + recurso + ": " + e.getMessage(), e);
        }
    }

    private static String leerRecurso(String recurso) {
        try (InputStream in = ConexionDB.class.getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IllegalStateException("No se encontro el recurso " + recurso);
            }
            String texto = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            // Elimina comentarios de linea completos o al final de una linea
            return texto.lines()
                    .map(linea -> linea.replaceAll("--.*$", ""))
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + recurso, e);
        }
    }
}
