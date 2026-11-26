package com.shadow.controlacceso.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.shadow.controlacceso.config.AppConfig;
import com.shadow.controlacceso.model.Alerta;
import com.shadow.controlacceso.model.TarjetaRFID;
import com.shadow.controlacceso.model.Trabajador;
import com.shadow.controlacceso.model.Zona;
import com.shadow.controlacceso.model.enums.Resultado;
import com.shadow.controlacceso.reader.SimuladorLectorRFID;
import com.shadow.controlacceso.report.FilaRegistro;
import com.shadow.controlacceso.service.ControlAccesoService;
import com.shadow.controlacceso.service.ReporteService;
import com.shadow.controlacceso.service.ResultadoLectura;

/**
 * Ventana de la demo (Swing).
 *
 * <pre>
 * +-----------------------------------------------------------+
 * | Simulador de lector: [Tarjeta v] [Zona v] [ ] 23:00 [Pasar]|
 * |  >>> PERMITIDO - Carlos Perez -> Ingreso principal         |
 * +-----------------------------------------------------------+
 * | [Registros] [Alertas] [Tarjetas]                          |
 * |   tabla...                                                |
 * +-----------------------------------------------------------+
 * | [Generar reporte PDF]   estado...                         |
 * +-----------------------------------------------------------+
 * </pre>
 *
 * <p>La ventana NO tiene reglas de negocio: solo toma datos de los servicios y los muestra.</p>
 */
public class VentanaPrincipal extends JFrame {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Color VERDE = new Color(0, 130, 60);
    private static final Color ROJO = new Color(190, 30, 30);
    private static final int FILAS_A_MOSTRAR = 200;

    private final ControlAccesoService controlAcceso;
    private final ReporteService reportes;
    private final SimuladorLectorRFID lector;

    private final JComboBox<OpcionTarjeta> comboTarjeta = new JComboBox<>();
    private final JComboBox<Zona> comboZona = new JComboBox<>();
    private final JCheckBox chkNocturno = new JCheckBox("Simular hora 23:00 (fuera de horario)");
    private final JLabel lblResultado = new JLabel("Seleccione una tarjeta y una zona, luego presione \"Pasar tarjeta\"");
    private final JLabel lblEstado = new JLabel(" ");

    private final DefaultTableModel modeloRegistros = modeloNoEditable("Fecha / hora", "Zona", "Trabajador", "Resultado");
    private final DefaultTableModel modeloAlertas = modeloNoEditable("Fecha / hora", "Tipo", "Detalle");
    private final DefaultTableModel modeloTarjetas = modeloNoEditable("Id", "Hash (inicio)", "Titular", "Estado", "Emision");
    private final JTable tablaTarjetas = new JTable(modeloTarjetas);
    private List<TarjetaRFID> tarjetasMostradas = List.of();

    /** Elemento del combo de tarjetas. uid == null significa "generar una desconocida". */
    private record OpcionTarjeta(String uid, String etiqueta) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }

    public VentanaPrincipal(AppConfig config) {
        super("Control de acceso RFID - Obra (demo)");
        this.controlAcceso = config.getControlAccesoService();
        this.reportes = config.getReporteService();
        this.lector = config.getLector();

        usarLookAndFeelNimbus();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        add(crearPanelLector(), BorderLayout.NORTH);
        add(crearPestanas(), BorderLayout.CENTER);
        add(crearPanelInferior(), BorderLayout.SOUTH);

        cargarCombos();
        // Observer: cuando el lector "lee", se procesa el acceso y se actualiza la pantalla
        lector.setOyente(lectura -> mostrarResultado(controlAcceso.procesarLectura(lectura)));
        refrescarTablas();

        setSize(980, 620);
        setLocationRelativeTo(null);
    }

    // --- Construccion de la interfaz ----------------------------------------------

    private JPanel crearPanelLector() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 8, 0, 8),
                BorderFactory.createTitledBorder("Simulador de lector RFID")));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        panel.add(new JLabel("Tarjeta:"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        panel.add(comboTarjeta, c);

        c.gridx = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        panel.add(new JLabel("Zona:"), c);
        c.gridx = 3; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        panel.add(comboZona, c);

        JButton btnPasar = new JButton("Pasar tarjeta");
        btnPasar.addActionListener(e -> pasarTarjeta());
        c.gridx = 4; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        panel.add(btnPasar, c);

        c.gridx = 1; c.gridy = 1; c.gridwidth = 4;
        panel.add(chkNocturno, c);

        lblResultado.setFont(lblResultado.getFont().deriveFont(Font.BOLD, 15f));
        c.gridx = 0; c.gridy = 2; c.gridwidth = 5;
        panel.add(lblResultado, c);
        return panel;
    }

    private JTabbedPane crearPestanas() {
        JTable tablaRegistros = new JTable(modeloRegistros);
        tablaRegistros.getColumnModel().getColumn(3).setCellRenderer(new RenderResultado());
        tablaRegistros.getColumnModel().getColumn(3).setPreferredWidth(330);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        pestanas.addTab("Registros de acceso", new JScrollPane(tablaRegistros));
        pestanas.addTab("Alertas", new JScrollPane(new JTable(modeloAlertas)));
        pestanas.addTab("Tarjetas", crearPanelTarjetas());
        return pestanas;
    }

    private JPanel crearPanelTarjetas() {
        JButton btnAlternar = new JButton("Bloquear / activar tarjeta seleccionada");
        btnAlternar.addActionListener(e -> alternarTarjetaSeleccionada());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(btnAlternar);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(tablaTarjetas), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelInferior() {
        JButton btnReporte = new JButton("Generar reporte PDF");
        btnReporte.addActionListener(e -> generarReporte());

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.add(btnReporte);
        panel.add(lblEstado);
        return panel;
    }

    private void cargarCombos() {
        for (String uid : SimuladorLectorRFID.UIDS_DEMO) {
            String titular = controlAcceso.buscarTitular(uid).map(Trabajador::toString).orElse("sin titular");
            comboTarjeta.addItem(new OpcionTarjeta(uid, uid + "  -  " + titular));
        }
        comboTarjeta.addItem(new OpcionTarjeta(null, "Tarjeta desconocida (UID aleatorio)"));
        controlAcceso.listarZonas().forEach(comboZona::addItem);
    }

    // --- Acciones -------------------------------------------------------------------

    private void pasarTarjeta() {
        OpcionTarjeta opcion = (OpcionTarjeta) comboTarjeta.getSelectedItem();
        Zona zona = (Zona) comboZona.getSelectedItem();
        if (opcion == null || zona == null) {
            return;
        }
        String uid = opcion.uid() != null ? opcion.uid() : lector.generarUidAleatorio();
        LocalDateTime hora = chkNocturno.isSelected()
                ? LocalDateTime.now().withHour(23).withMinute(0).withSecond(0).withNano(0)
                : LocalDateTime.now();
        try {
            lector.simularLectura(uid, zona.getCodigo(), hora);
        } catch (RuntimeException ex) {
            mostrarError(ex);
        }
    }

    private void mostrarResultado(ResultadoLectura r) {
        String texto = r.mensaje();
        if (!r.alertas().isEmpty()) {
            texto += "   |   " + r.alertas().size() + " alerta(s)";
        }
        lblResultado.setText(texto);
        lblResultado.setForeground(r.permitido() ? VERDE : ROJO);
        refrescarTablas();
    }

    private void alternarTarjetaSeleccionada() {
        int fila = tablaTarjetas.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una tarjeta de la tabla.");
            return;
        }
        controlAcceso.alternarEstadoTarjeta(tarjetasMostradas.get(fila));
        refrescarTablas();
        tablaTarjetas.setRowSelectionInterval(fila, fila);
    }

    private void generarReporte() {
        try {
            Path archivo = reportes.generarReporte(AppConfig.CARPETA_REPORTES, 500);
            lblEstado.setText("Reporte generado: " + archivo.toAbsolutePath());
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(archivo.toFile());
            }
        } catch (IOException | RuntimeException ex) {
            mostrarError(ex);
        }
    }

    // --- Refresco de tablas -----------------------------------------------------------

    private void refrescarTablas() {
        modeloRegistros.setRowCount(0);
        for (FilaRegistro f : reportes.registrosDetallados(FILAS_A_MOSTRAR)) {
            modeloRegistros.addRow(new Object[] {FORMATO.format(f.fechaHora()), f.zona(), f.trabajador(), f.resultado()});
        }

        modeloAlertas.setRowCount(0);
        for (Alerta a : reportes.alertasRecientes(FILAS_A_MOSTRAR)) {
            modeloAlertas.addRow(new Object[] {FORMATO.format(a.fechaHora()), a.tipo(), a.mensaje()});
        }

        modeloTarjetas.setRowCount(0);
        tarjetasMostradas = controlAcceso.listarTarjetas();
        for (TarjetaRFID t : tarjetasMostradas) {
            String titular = controlAcceso.buscarTrabajador(t.getTrabajadorId())
                    .map(Trabajador::getNombre).orElse("?");
            modeloTarjetas.addRow(new Object[] {
                    t.getId(), t.getHashCorto(), titular, t.isActiva() ? "Activa" : "BLOQUEADA", t.getFechaEmision()});
        }
    }

    // --- Utilidades ---------------------------------------------------------------------

    private void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static DefaultTableModel modeloNoEditable(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private static void usarLookAndFeelNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignorada) {
            // si falla, se queda el look and feel por defecto
        }
    }

    /** Pinta la columna "Resultado" en verde o rojo. */
    private static class RenderResultado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                       boolean foco, int fila, int columna) {
            Resultado r = (Resultado) valor;
            super.getTableCellRendererComponent(tabla, r.getDescripcion(), seleccionada, foco, fila, columna);
            if (!seleccionada) {
                setForeground(r.esPermitido() ? VERDE : ROJO);
            }
            return this;
        }
    }
}
