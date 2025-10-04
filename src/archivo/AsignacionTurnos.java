package archivo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.toedter.calendar.JDateChooser;

/**
 * Pantalla para asignación de turnos a empleados
 * Implementa el caso de uso AN01 con validaciones y reglas de negocio
 */
public class AsignacionTurnos extends JFrame {
    
    private Empleado administrador;
    private AsignacionTurnoManager turnoManager;
    private BitacoraManager bitacoraManager;
    
    // Componentes de la interfaz
    private JLabel lblTitulo;
    private JLabel lblFechaInicio;
    private JLabel lblFechaFin;
    private JLabel lblEmpleados;
    private JLabel lblTurno;
    
    private JDateChooser fechaInicio;
    private JDateChooser fechaFin;
    private JComboBox<EmpleadoItem> comboEmpleados;
    private JComboBox<AsignacionTurno.TipoTurno> comboTurnos;
    
    private JButton btnGuardar;
    private JButton btnRegresar;
    
    // Clase auxiliar para mostrar empleados en el combo
    private static class EmpleadoItem {
        private Empleado empleado;
        
        public EmpleadoItem(Empleado empleado) {
            this.empleado = empleado;
        }
        
        public Empleado getEmpleado() { return empleado; }
        
        @Override
        public String toString() {
            return String.format("%s - %s", empleado.getNombre(), empleado.getDpi());
        }
    }
    
    public AsignacionTurnos(Empleado administrador) {
        this.administrador = administrador;
        this.turnoManager = new AsignacionTurnoManager();
        this.bitacoraManager = new BitacoraManager();
        
        initComponents();
        configurarVentana();
        cargarDatos();
        
        // Registrar acceso en bitácora
        bitacoraManager.registrarOperacion(administrador.getUsername(), "ACCESO_ASIGNACION_TURNOS",
            "Acceso al módulo de asignación de turnos", administrador.getDpi());
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal con diseño según AN01
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Título
        lblTitulo = new JLabel("ASIGNACION DE TURNOS");
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setOpaque(true);
        lblTitulo.setBackground(new Color(51, 122, 183)); // Color azul del diseño
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 30, 0);
        panelPrincipal.add(lblTitulo, gbc);
        
        // Reset gridwidth
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Fecha Inicio
        lblFechaInicio = new JLabel("Fecha Inicio:");
        lblFechaInicio.setFont(new Font("Tahoma", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 10, 20);
        panelPrincipal.add(lblFechaInicio, gbc);
        
        fechaInicio = new JDateChooser();
        fechaInicio.setPreferredSize(new Dimension(250, 35));
        fechaInicio.setFont(new Font("Tahoma", Font.PLAIN, 14));
        fechaInicio.setDate(java.sql.Date.valueOf(LocalDate.now()));
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(fechaInicio, gbc);
        
        // Fecha Fin
        lblFechaFin = new JLabel("Fecha Fin:");
        lblFechaFin.setFont(new Font("Tahoma", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panelPrincipal.add(lblFechaFin, gbc);
        
        fechaFin = new JDateChooser();
        fechaFin.setPreferredSize(new Dimension(250, 35));
        fechaFin.setFont(new Font("Tahoma", Font.PLAIN, 14));
        fechaFin.setDate(java.sql.Date.valueOf(LocalDate.now().plusDays(7)));
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(fechaFin, gbc);
        
        // Empleados
        lblEmpleados = new JLabel("Empleados:");
        lblEmpleados.setFont(new Font("Tahoma", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        panelPrincipal.add(lblEmpleados, gbc);
        
        comboEmpleados = new JComboBox<>();
        comboEmpleados.setPreferredSize(new Dimension(250, 35));
        comboEmpleados.setFont(new Font("Tahoma", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(comboEmpleados, gbc);
        
        // Turno
        lblTurno = new JLabel("Turno:");
        lblTurno.setFont(new Font("Tahoma", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        panelPrincipal.add(lblTurno, gbc);
        
        comboTurnos = new JComboBox<>(AsignacionTurno.TipoTurno.values());
        comboTurnos.setPreferredSize(new Dimension(250, 35));
        comboTurnos.setFont(new Font("Tahoma", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(comboTurnos, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        
        btnRegresar = new JButton("Regresar");
        btnRegresar.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnRegresar.setPreferredSize(new Dimension(120, 40));
        btnRegresar.setBackground(new Color(108, 117, 125));
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setBorder(BorderFactory.createRaisedBevelBorder());
        btnRegresar.addActionListener(e -> regresar());
        
        btnGuardar = new JButton("Guardar");
        btnGuardar.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnGuardar.setPreferredSize(new Dimension(120, 40));
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setBorder(BorderFactory.createRaisedBevelBorder());
        btnGuardar.addActionListener(e -> guardarAsignacion());
        
        panelBotones.add(btnRegresar);
        panelBotones.add(btnGuardar);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(30, 0, 0, 0);
        panelPrincipal.add(panelBotones, gbc);
        
        add(panelPrincipal, BorderLayout.CENTER);
        
        // Agregar placeholder inicial a comboEmpleados
        comboEmpleados.addItem(new EmpleadoItem(new Empleado("", "Seleccionar empleado", "", "", "", "", "", "")));
    }
    
    private void configurarVentana() {
        setTitle("Sistema de Control de Turnos - Asignacion de Turnos");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void cargarDatos() {
        // Limpiar combo y cargar empleados disponibles
        comboEmpleados.removeAllItems();
        comboEmpleados.addItem(new EmpleadoItem(new Empleado("", "Seleccionar empleado", "", "", "", "", "", "")));
        
        List<Empleado> empleados = turnoManager.getEmpleadosDisponibles();
        for (Empleado emp : empleados) {
            comboEmpleados.addItem(new EmpleadoItem(emp));
        }
    }
    
    private void guardarAsignacion() {
        try {
            // Validar campos
            if (fechaInicio.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar la fecha de inicio",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (fechaFin.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar la fecha de fin",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            EmpleadoItem empleadoSeleccionado = (EmpleadoItem) comboEmpleados.getSelectedItem();
            if (empleadoSeleccionado == null || empleadoSeleccionado.getEmpleado().getDpi().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un empleado",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            AsignacionTurno.TipoTurno turnoSeleccionado = (AsignacionTurno.TipoTurno) comboTurnos.getSelectedItem();
            if (turnoSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar un turno",
                    "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Convertir fechas
            LocalDate fechaIni = fechaInicio.getDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            LocalDate fechaFinal = fechaFin.getDate().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            // Crear asignación
            AsignacionTurnoManager.ResultadoAsignacion resultado = turnoManager.crearAsignacion(
                empleadoSeleccionado.getEmpleado().getDpi(),
                fechaIni,
                fechaFinal,
                turnoSeleccionado,
                administrador.getUsername()
            );
            
            if (resultado.isExito()) {
                // Mostrar mensaje de éxito según especificación
                JOptionPane.showMessageDialog(this, "Asignación creada con éxito",
                    "Operación exitosa", JOptionPane.INFORMATION_MESSAGE);
                
                // Limpiar formulario
                limpiarFormulario();
                
            } else {
                JOptionPane.showMessageDialog(this, resultado.getMensaje(),
                    "Error en asignación", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error inesperado al crear la asignación: " + e.getMessage(),
                "Error del sistema", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void limpiarFormulario() {
        fechaInicio.setDate(java.sql.Date.valueOf(LocalDate.now()));
        fechaFin.setDate(java.sql.Date.valueOf(LocalDate.now().plusDays(7)));
        comboEmpleados.setSelectedIndex(0);
        comboTurnos.setSelectedIndex(0);
    }
    
    private void regresar() {
        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Esta seguro que desea regresar? Los datos no guardados se perderán.",
            "Confirmar regreso",
            JOptionPane.YES_NO_OPTION);
        
        if (opcion == JOptionPane.YES_OPTION) {
            bitacoraManager.registrarOperacion(administrador.getUsername(), "SALIDA_ASIGNACION_TURNOS",
                "Salida del módulo de asignación de turnos", administrador.getDpi());
            dispose();
        }
    }
    
    @Override
    public void dispose() {
        // Registrar salida si no se ha registrado ya
        super.dispose();
    }
}