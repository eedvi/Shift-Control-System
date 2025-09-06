/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package archivo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Request Management Interface - handles employee requests for time off, permits, etc.
 */
public class Solicitudes extends javax.swing.JFrame {

    private Empleado usuarioActual;
    private DatabaseManager dbManager;
    private BitacoraManager bitacoraManager;
    private EmailService emailService;
    private SolicitudManager solicitudManager;

    private JTable tablaSolicitudes;
    private DefaultTableModel modeloTabla;
    private JButton btnAprobar;
    private JButton btnRechazar;
    private JButton btnRegresar;
    private JTextArea txtMotivoRechazo;

    /**
     * Creates new form Solicitudes
     */
    public Solicitudes() {
        initComponents();
        // Constructor para compatibilidad
    }

    public Solicitudes(Empleado usuario, DatabaseManager db, BitacoraManager bitacora, EmailService email, SolicitudManager solicitudMgr) {
        this.usuarioActual = usuario;
        this.dbManager = db;
        this.bitacoraManager = bitacora;
        this.emailService = email;
        this.solicitudManager = solicitudMgr;
        setupRequestInterface();
    }

    private void setupRequestInterface() {
        setTitle("Gestión de Solicitudes - Sistema RRHH");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior con información
        JPanel panelInfo = new JPanel();
        panelInfo.add(new JLabel("Solicitudes Pendientes de Aprobación"));

        // Tabla de solicitudes
        String[] columnas = {"ID", "Empleado", "DPI", "Tipo", "Descripción", "Fecha Inicio", "Fecha Fin", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaSolicitudes = new JTable(modeloTabla);
        tablaSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaSolicitudes);
        scrollPane.setPreferredSize(new Dimension(900, 300));

        // Panel de acciones
        JPanel panelAcciones = new JPanel(new BorderLayout());

        // Área para motivo de rechazo
        JPanel panelMotivo = new JPanel(new BorderLayout());
        panelMotivo.add(new JLabel("Motivo de Rechazo (opcional):"), BorderLayout.NORTH);
        txtMotivoRechazo = new JTextArea(3, 30);
        txtMotivoRechazo.setBorder(BorderFactory.createEtchedBorder());
        panelMotivo.add(new JScrollPane(txtMotivoRechazo), BorderLayout.CENTER);

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout());

        btnAprobar = new JButton("Aprobar");
        btnAprobar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnAprobar.setBackground(new Color(76, 175, 80));
        btnAprobar.setForeground(Color.WHITE);
        btnAprobar.addActionListener(this::aprobarSolicitud);
        panelBotones.add(btnAprobar);

        btnRechazar = new JButton("Rechazar");
        btnRechazar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRechazar.setBackground(new Color(244, 67, 54));
        btnRechazar.setForeground(Color.WHITE);
        btnRechazar.addActionListener(this::rechazarSolicitud);
        panelBotones.add(btnRechazar);

        btnRegresar = new JButton("Regresar");
        btnRegresar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRegresar.addActionListener(this::regresar);
        panelBotones.add(btnRegresar);

        panelAcciones.add(panelMotivo, BorderLayout.NORTH);
        panelAcciones.add(panelBotones, BorderLayout.SOUTH);

        add(panelInfo, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelAcciones, BorderLayout.SOUTH);

        // ONLY load existing requests - NO example generation
        cargarSolicitudesPendientes();

        pack();
        setLocationRelativeTo(null);
    }

    private void cargarSolicitudesPendientes() {
        // Use SolicitudManager instead of DatabaseManager
        List<Solicitud> solicitudes = solicitudManager.obtenerSolicitudesPendientes();
        actualizarTabla(solicitudes);
    }

    private void actualizarTabla(List<Solicitud> solicitudes) {
        modeloTabla.setRowCount(0);

        for (Solicitud sol : solicitudes) {
            Object[] fila = {
                sol.getId(),
                sol.getEmpleadoNombre(),
                sol.getEmpleadoDpi(),
                sol.getTipo().getDescripcion(),
                sol.getDescripcion(),
                sol.getFechaInicioFormateada(),
                sol.getFechaFinFormateada(),
                sol.getEstado().getDescripcion()
            };
            modeloTabla.addRow(fila);
        }
    }

    private void aprobarSolicitud(ActionEvent e) {
        int filaSeleccionada = tablaSolicitudes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud de la tabla",
                                        "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int solicitudId = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String empleadoNombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        String tipoSolicitud = (String) modeloTabla.getValueAt(filaSeleccionada, 3);

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Aprobar la solicitud de " + tipoSolicitud + " para " + empleadoNombre + "?",
            "Confirmar Aprobación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                // Use SolicitudManager to update request status
                solicitudManager.actualizarEstadoSolicitud(solicitudId, Solicitud.EstadoSolicitud.APROBADA,
                                                         usuarioActual.getUsername(), null);

                JOptionPane.showMessageDialog(this, "Solicitud aprobada exitosamente",
                                            "Aprobación exitosa", JOptionPane.INFORMATION_MESSAGE);

                // Registrar en bitácora
                bitacoraManager.registrarOperacion(usuarioActual.getUsername(), "APPROVE_REQUEST",
                                                 "Solicitud aprobada: " + tipoSolicitud, "ID: " + solicitudId);

                // Find employee for email notification
                String empleadoDpi = (String) modeloTabla.getValueAt(filaSeleccionada, 2);
                Empleado empleado = dbManager.obtenerEmpleadoPorDpi(empleadoDpi);
                if (empleado != null && empleado.getEmail() != null && !empleado.getEmail().isEmpty()) {
                    emailService.enviarAprobacionSolicitud(empleado.getEmail(), empleado.getNombre(), tipoSolicitud);
                }

                // Actualizar tabla
                cargarSolicitudesPendientes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al aprobar la solicitud: " + ex.getMessage(),
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void rechazarSolicitud(ActionEvent e) {
        int filaSeleccionada = tablaSolicitudes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una solicitud de la tabla",
                                        "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String motivoRechazo = txtMotivoRechazo.getText().trim();
        if (motivoRechazo.isEmpty()) {
            int continuar = JOptionPane.showConfirmDialog(this,
                "No se especificó motivo de rechazo. ¿Continuar?",
                "Motivo vacío", JOptionPane.YES_NO_OPTION);
            if (continuar != JOptionPane.YES_OPTION) {
                return;
            }
            motivoRechazo = "No especificado";
        }

        int solicitudId = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        String empleadoNombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        String tipoSolicitud = (String) modeloTabla.getValueAt(filaSeleccionada, 3);

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Rechazar la solicitud de " + tipoSolicitud + " para " + empleadoNombre + "?\nMotivo: " + motivoRechazo,
            "Confirmar Rechazo", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                // Use SolicitudManager to update request status
                solicitudManager.actualizarEstadoSolicitud(solicitudId, Solicitud.EstadoSolicitud.RECHAZADA,
                                                         usuarioActual.getUsername(), motivoRechazo);

                JOptionPane.showMessageDialog(this, "Solicitud rechazada",
                                            "Rechazo procesado", JOptionPane.INFORMATION_MESSAGE);

                // Registrar en bitácora
                bitacoraManager.registrarOperacion(usuarioActual.getUsername(), "REJECT_REQUEST",
                                                 "Solicitud rechazada: " + tipoSolicitud + ". Motivo: " + motivoRechazo,
                                                 "ID: " + solicitudId);

                // Find employee for email notification
                String empleadoDpi = (String) modeloTabla.getValueAt(filaSeleccionada, 2);
                Empleado empleado = dbManager.obtenerEmpleadoPorDpi(empleadoDpi);
                if (empleado != null && empleado.getEmail() != null && !empleado.getEmail().isEmpty()) {
                    emailService.enviarRechazoSolicitud(empleado.getEmail(), empleado.getNombre(),
                                                      tipoSolicitud, motivoRechazo);
                }

                // Limpiar motivo y actualizar tabla
                txtMotivoRechazo.setText("");
                cargarSolicitudesPendientes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al rechazar la solicitud: " + ex.getMessage(),
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void regresar(ActionEvent e) {
        MantenimientoUsuario menu = new MantenimientoUsuario(usuarioActual, dbManager, bitacoraManager);
        menu.setVisible(true);
        dispose();
    }

    // Mantener compatibilidad con código existente
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        pack();
    }
}
