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
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request Management Interface - handles employee requests for time off, permits, etc.
 */
public class Solicitudes extends javax.swing.JFrame {

    private Empleado usuarioActual;
    private DatabaseManager dbManager;
    private BitacoraManager bitacoraManager;
    private EmailService emailService;

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

    public Solicitudes(Empleado usuario, DatabaseManager db, BitacoraManager bitacora, EmailService email) {
        this.usuarioActual = usuario;
        this.dbManager = db;
        this.bitacoraManager = bitacora;
        this.emailService = email;
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

        // Cargar solicitudes pendientes
        cargarSolicitudesPendientes();

        // Agregar algunas solicitudes de ejemplo para demostración
        agregarSolicitudesEjemplo();

        pack();
        setLocationRelativeTo(null);
    }

    private void cargarSolicitudesPendientes() {
        List<Solicitud> solicitudes = dbManager.obtenerSolicitudesPendientes();
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
            Solicitud solicitud = dbManager.obtenerSolicitudPorId(solicitudId);
            if (solicitud != null) {
                solicitud.aprobar(usuarioActual.getUsername());
                dbManager.actualizarSolicitud(solicitud);

                JOptionPane.showMessageDialog(this, "Solicitud aprobada exitosamente",
                                            "Aprobación exitosa", JOptionPane.INFORMATION_MESSAGE);

                // Registrar en bitácora
                bitacoraManager.registrarOperacion(usuarioActual.getUsername(), "APPROVE_REQUEST",
                                                 "Solicitud aprobada: " + tipoSolicitud, solicitud.getEmpleadoDpi());

                // Enviar email de confirmación
                Empleado empleado = dbManager.obtenerEmpleadoPorDpi(solicitud.getEmpleadoDpi());
                if (empleado != null && empleado.getEmail() != null && !empleado.getEmail().isEmpty()) {
                    emailService.enviarAprobacionSolicitud(empleado.getEmail(), empleado.getNombre(), tipoSolicitud);
                }

                // Actualizar tabla
                cargarSolicitudesPendientes();
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
            Solicitud solicitud = dbManager.obtenerSolicitudPorId(solicitudId);
            if (solicitud != null) {
                solicitud.rechazar(usuarioActual.getUsername(), motivoRechazo);
                dbManager.actualizarSolicitud(solicitud);

                JOptionPane.showMessageDialog(this, "Solicitud rechazada",
                                            "Rechazo procesado", JOptionPane.INFORMATION_MESSAGE);

                // Registrar en bitácora
                bitacoraManager.registrarOperacion(usuarioActual.getUsername(), "REJECT_REQUEST",
                                                 "Solicitud rechazada: " + tipoSolicitud + ". Motivo: " + motivoRechazo,
                                                 solicitud.getEmpleadoDpi());

                // Enviar email de rechazo
                Empleado empleado = dbManager.obtenerEmpleadoPorDpi(solicitud.getEmpleadoDpi());
                if (empleado != null && empleado.getEmail() != null && !empleado.getEmail().isEmpty()) {
                    emailService.enviarRechazoSolicitud(empleado.getEmail(), empleado.getNombre(),
                                                      tipoSolicitud, motivoRechazo);
                }

                // Limpiar motivo y actualizar tabla
                txtMotivoRechazo.setText("");
                cargarSolicitudesPendientes();
            }
        }
    }

    private void regresar(ActionEvent e) {
        MantenimientoUsuario menu = new MantenimientoUsuario(usuarioActual, dbManager, bitacoraManager);
        menu.setVisible(true);
        dispose();
    }

    // Agregar solicitudes de ejemplo para demostración
    private void agregarSolicitudesEjemplo() {
        // Create example requests using actual employees from the system
        List<Empleado> empleados = dbManager.obtenerTodosEmpleados();

        // Only create examples if there are employees and no existing requests
        if (!empleados.isEmpty() && dbManager.obtenerSolicitudesPendientes().isEmpty()) {
            // Find employees with "Empleado" role (not admin users)
            for (Empleado emp : empleados) {
                if ("Empleado".equals(emp.getRole())) {
                    // Create a vacation request for the first employee found
                    Solicitud solicitud1 = new Solicitud(emp.getDpi(), emp.getNombre(),
                                                        Solicitud.TipoSolicitud.VACACIONES,
                                                        "Vacaciones familiares",
                                                        LocalDateTime.now().plusDays(7),
                                                        LocalDateTime.now().plusDays(14));
                    dbManager.crearSolicitud(solicitud1);
                    break; // Only create one example
                }
            }
        }

        // Actualizar tabla
        cargarSolicitudesPendientes();
    }

    // Mantener compatibilidad con código existente
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        pack();
    }
}
