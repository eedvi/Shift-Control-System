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
 * Enhanced Employee Consultation Interface with search and status management
 */
public class EmpleadosRegistrados extends javax.swing.JFrame {

    private Empleado usuarioActual;
    private DatabaseManager dbManager;
    private BitacoraManager bitacoraManager;
    private EmailService emailService;

    private JTextField txtBusqueda;
    private JTable tablaEmpleados;
    private DefaultTableModel modeloTabla;
    private JButton btnBuscar;
    private JButton btnDesactivar;
    private JButton btnRegresar;
    private JComboBox<String> cboMotivoInactividad;

    public EmpleadosRegistrados() {
        initComponents();
        // Constructor para compatibilidad
    }

    public EmpleadosRegistrados(Empleado usuario, DatabaseManager db, BitacoraManager bitacora, EmailService email) {
        this.usuarioActual = usuario;
        this.dbManager = db;
        this.bitacoraManager = bitacora;
        this.emailService = email;
        setupEnhancedInterface();
    }

    private void setupEnhancedInterface() {
        setTitle("Consultar Empleados - Sistema RRHH");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior con búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout());
        panelBusqueda.add(new JLabel("Buscar:"));
        txtBusqueda = new JTextField(20);
        panelBusqueda.add(txtBusqueda);

        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(this::buscarEmpleados);
        panelBusqueda.add(btnBuscar);

        // Tabla de empleados
        String[] columnas = {"Username", "Nombre", "DPI", "Área", "Turno", "Estado", "Email"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer tabla no editable
            }
        };

        tablaEmpleados = new JTable(modeloTabla);
        tablaEmpleados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tablaEmpleados);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        // Panel de acciones
        JPanel panelAcciones = new JPanel(new FlowLayout());

        cboMotivoInactividad = new JComboBox<>(new String[]{
            "Permiso Personal", "Vacaciones", "Cita IGSS", "Día de Cumpleaños",
            "Suspensión Laboral", "Otro"
        });
        panelAcciones.add(new JLabel("Motivo:"));
        panelAcciones.add(cboMotivoInactividad);

        btnDesactivar = new JButton("Desactivar Empleado");
        btnDesactivar.addActionListener(this::desactivarEmpleado);
        panelAcciones.add(btnDesactivar);

        btnRegresar = new JButton("Regresar");
        btnRegresar.addActionListener(this::regresar);
        panelAcciones.add(btnRegresar);

        add(panelBusqueda, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelAcciones, BorderLayout.SOUTH);

        // Cargar todos los empleados inicialmente
        cargarTodosEmpleados();

        pack();
        setLocationRelativeTo(null);
    }

    private void cargarTodosEmpleados() {
        List<Empleado> empleados = dbManager.obtenerTodosEmpleados();
        actualizarTabla(empleados);
    }

    private void buscarEmpleados(ActionEvent e) {
        String criterio = txtBusqueda.getText().trim();
        List<Empleado> empleados = dbManager.buscarEmpleados(criterio);
        actualizarTabla(empleados);
    }

    private void actualizarTabla(List<Empleado> empleados) {
        modeloTabla.setRowCount(0); // Limpiar tabla

        for (Empleado emp : empleados) {
            Object[] fila = {
                emp.getUsername(),
                emp.getNombre(),
                emp.getDpi(),
                emp.getArea(),
                emp.getTurno(),
                emp.getEstado(),
                emp.getEmail() != null ? emp.getEmail() : ""
            };
            modeloTabla.addRow(fila);
        }
    }

    private void desactivarEmpleado(ActionEvent e) {
        int filaSeleccionada = tablaEmpleados.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado de la tabla",
                                        "Selección requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombre = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        String estadoActual = (String) modeloTabla.getValueAt(filaSeleccionada, 5);

        if ("Inactivo".equals(estadoActual)) {
            JOptionPane.showMessageDialog(this, "El empleado ya está inactivo",
                                        "Estado actual", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String motivo = (String) cboMotivoInactividad.getSelectedItem();

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de desactivar al empleado " + nombre + "?\nMotivo: " + motivo,
            "Confirmar desactivación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            if (dbManager.desactivarEmpleado(username, motivo)) {
                JOptionPane.showMessageDialog(this,
                    "Empleado " + username + " ha sido desactivado exitosamente",
                    "Desactivación exitosa", JOptionPane.INFORMATION_MESSAGE);

                // Registrar en bitácora
                bitacoraManager.registrarOperacion(usuarioActual.getUsername(), "DEACTIVATE",
                                                 "Empleado desactivado. Motivo: " + motivo, username);

                // Enviar email de notificación
                Empleado empleado = dbManager.obtenerEmpleadoPorUsername(username);
                if (empleado != null && empleado.getEmail() != null && !empleado.getEmail().isEmpty()) {
                    emailService.enviarNotificacionInactividad(empleado.getEmail(), empleado.getNombre(), motivo);
                }

                // Actualizar tabla
                cargarTodosEmpleados();
            } else {
                JOptionPane.showMessageDialog(this, "Error al desactivar empleado",
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void regresar(ActionEvent e) {
        MantenimientoUsuario menu = new MantenimientoUsuario(usuarioActual, dbManager, bitacoraManager);
        menu.setVisible(true);
        dispose();
    }

    // Mantener compatibilidad con el código existente
    @SuppressWarnings("unchecked")
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        pack();
    }
}
