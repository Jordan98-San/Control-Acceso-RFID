package com.shadow.controlacceso;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import com.shadow.controlacceso.config.AppConfig;
import com.shadow.controlacceso.ui.ConsolaApp;
import com.shadow.controlacceso.ui.VentanaPrincipal;

/**
 * Punto de entrada.
 *
 * <ul>
 *   <li>Sin argumentos: abre la ventana grafica (Swing).</li>
 *   <li>{@code --consola}: menu de texto (tambien se usa si no hay pantalla disponible).</li>
 * </ul>
 */
public class Main {

    public static void main(String[] args) {
        AppConfig config = new AppConfig();
        config.inicializarBaseDeDatos();

        boolean modoConsola = Arrays.asList(args).contains("--consola") || GraphicsEnvironment.isHeadless();

        if (modoConsola) {
            new ConsolaApp(config).iniciar();
        } else {
            // Swing exige crear y tocar la interfaz desde su propio hilo (Event Dispatch Thread)
            SwingUtilities.invokeLater(() -> new VentanaPrincipal(config).setVisible(true));
        }
    }
}
