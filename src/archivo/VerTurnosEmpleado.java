package archivo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pantalla para que los empleados vean sus turnos asignados
 * Cumple con la postcondición del caso de uso
 */
public class VerTurnosEmpleado extends JFrame {
    
    private Empleado empleado;
    private AsignacionTurnoManager turnoManager;
    private BitacoraManager bitacoraManager;
    
    // Componentes de la interfaz
    private JLabel lblTitulo;
    private JLabel lblEmpleado;
    private JLabel lblTurnoActual;
    private JTable tablaTurnos;
    private DefaultTableModel modeloTabla;
    private JButton btnActualizar;
    private JButton btnCerrar;
    private JTextArea txtResumen;
    
    public VerTurnosEmpleado(Empleado empleado) {
        this.empleado = empleado;
        this.turnoManager = new AsignacionTurnoManager();
        this.bitacoraManager = new BitacoraManager();
        
        initComponents();
        configurarVentana();
        cargarTurnos();
        
        // Registrar acceso en bitácora
        bitacoraManager.registrarOperacion(empleado.getUsername(), "VER_TURNOS_ASIGNADOS",
            "Consulta de turnos asignados", empleado.getDpi());
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior con título y información del empleado
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        lblTitulo = new JLabel("Mis Turnos Asignados");
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        lblEmpleado = new JLabel(String.format("Empleado: %s (%s)", 
            empleado.getNombre(), empleado.getDpi()));
        lblEmpleado.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblEmpleado.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Mostrar turno actual
        AsignacionTurno turnoActual = turnoManager.getTurnoActual(empleado.getDpi());
        String textoTurnoActual;
        if (turnoActual != null) {
            textoTurnoActual = String.format("Turno Actual: %s", turnoActual.getInfoHorario());
        } else {
            textoTurnoActual = "Turno Actual: No asignado";
        }
        
        lblTurnoActual = new JLabel(textoTurnoActual);
        lblTurnoActual.setFont(new Font("Tahoma", Font.BOLD, 14));
        lblTurnoActual.setHorizontalAlignment(SwingConstants.CENTER);
        lblTurnoActual.setForeground(turnoActual != null ? new Color(40, 167, 69) : new Color(220, 53, 69));
        
        JPanel panelInfo = new JPanel(new GridLayout(3, 1, 5, 5));
        panelInfo.add(lblTitulo);
        panelInfo.add(lblEmpleado);
        panelInfo.add(lblTurnoActual);
        
        panelSuperior.add(panelInfo, BorderLayout.CENTER);
        
        // Panel central con tabla de turnos
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        // Configurar tabla
        String[] columnas = {"Fecha Inicio", "Fecha Fin", "Turno", "Horario", "Estado", "Asignado Por"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaTurnos = new JTable(modeloTabla);
        tablaTurnos.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tablaTurnos.setRowHeight(25);
        tablaTurnos.getColumnModel().getColumn(0).setPreferredWidth(90);
        tablaTurnos.getColumnModel().getColumn(1).setPreferredWidth(90);
        tablaTurnos.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaTurnos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaTurnos.getColumnModel().getColumn(4).setPreferredWidth(80);
        tablaTurnos.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        JScrollPane scrollTabla = new JScrollPane(tablaTurnos);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Historial de Turnos Asignados"));
        scrollTabla.setPreferredSize(new Dimension(600, 250));
        
        panelCentral.add(scrollTabla, BorderLayout.CENTER);
        
        // Panel inferior con resumen y botones
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        
        txtResumen = new JTextArea(5, 50);
        txtResumen.setEditable(false);
        txtResumen.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtResumen.setBorder(BorderFactory.createTitledBorder("Resumen"));
        
        JScrollPane scrollResumen = new JScrollPane(txtResumen);
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnActualizar.setPreferredSize(new Dimension(120, 35));
        btnActualizar.setBackground(new Color(0, 123, 255));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.addActionListener(e -> {
            cargarTurnos();
            JOptionPane.showMessageDialog(this, "Información actualizada correctamente",
                "Actualización", JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnCerrar.setPreferredSize(new Dimension(120, 35));
        btnCerrar.setBackground(new Color(108, 117, 125));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.addActionListener(e -> dispose());
        
        panelBotones.add(btnActualizar);
        panelBotones.add(btnCerrar);
        
        panelInferior.add(scrollResumen, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        
        // Agregar paneles a la ventana
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void configurarVentana() {
        setTitle("Sistema de Control de Turnos - Mis Turnos");
        setSize(700, 600);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void cargarTurnos() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);
        
        // Obtener turnos del empleado
        List<AsignacionTurno> turnos = turnoManager.getAsignacionesEmpleado(empleado.getDpi());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate hoy = LocalDate.now();
        
        for (AsignacionTurno turno : turnos) {
            String fechaInicio = turno.getFechaInicio().format(formatter);
            String fechaFin = turno.getFechaFin().format(formatter);
            String tipoTurno = turno.getTurno().getDescripcion();
            String horario = String.format("%s - %s", 
                turno.getTurno().getHoraInicio(), 
                turno.getTurno().getHoraFin());
            
            String estado;
            if (!turno.isActivo()) {
                estado = "Cancelado";
            } else if (turno.getFechaFin().isBefore(hoy)) {
                estado = "Finalizado";
            } else if (turno.getFechaInicio().isAfter(hoy)) {
                estado = "Programado";
            } else {
                estado = "Actual";
            }
            
            String asignadoPor = turno.getAsignadoPor();
            
            modeloTabla.addRow(new Object[]{
                fechaInicio, fechaFin, tipoTurno, horario, estado, asignadoPor
            });
        }
        
        // Actualizar resumen
        actualizarResumen(turnos);
        
        // Actualizar etiqueta de turno actual
        AsignacionTurno turnoActual = turnoManager.getTurnoActual(empleado.getDpi());
        String textoTurnoActual;
        if (turnoActual != null) {
            textoTurnoActual = String.format("Turno Actual: %s", turnoActual.getInfoHorario());
            lblTurnoActual.setForeground(new Color(40, 167, 69));
        } else {
            textoTurnoActual = "Turno Actual: No asignado";
            lblTurnoActual.setForeground(new Color(220, 53, 69));
        }
        lblTurnoActual.setText(textoTurnoActual);
    }
    
    private void actualizarResumen(List<AsignacionTurno> turnos) {
        StringBuilder resumen = new StringBuilder();
        
        resumen.append("RESUMEN DE TURNOS ASIGNADOS\n");
        for (int i = 0; i < 40; i++) resumen.append("=");
        resumen.append("\n\n");
        
        if (turnos.isEmpty()) {
            resumen.append("No tiene turnos asignados actualmente.\n");
            resumen.append("Contacte a su administrador para más información.");
        } else {
            LocalDate hoy = LocalDate.now();
            
            // Contar turnos por estado
            long activos = turnos.stream().filter(AsignacionTurno::isActivo).count();
            long finalizados = turnos.stream()
                .filter(t -> t.isActivo() && t.getFechaFin().isBefore(hoy)).count();
            long programados = turnos.stream()
                .filter(t -> t.isActivo() && t.getFechaInicio().isAfter(hoy)).count();
            long actuales = turnos.stream()
                .filter(t -> t.esVigenteEnFecha(hoy)).count();
            
            resumen.append("Total de asignaciones: ").append(turnos.size()).append("\n");
            resumen.append("- Asignaciones activas: ").append(activos).append("\n");
            resumen.append("- Turnos finalizados: ").append(finalizados).append("\n");
            resumen.append("- Turnos programados: ").append(programados).append("\n");
            resumen.append("- Turnos actuales: ").append(actuales).append("\n\n");
            
            // Próximo turno programado
            AsignacionTurno proximoTurno = turnos.stream()
                .filter(AsignacionTurno::isActivo)
                .filter(t -> t.getFechaInicio().isAfter(hoy))
                .min((t1, t2) -> t1.getFechaInicio().compareTo(t2.getFechaInicio()))
                .orElse(null);
            
            if (proximoTurno != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                resumen.append("Próximo turno programado:\n");
                resumen.append("- Fecha: ").append(proximoTurno.getFechaInicio().format(formatter)).append("\n");
                resumen.append("- Turno: ").append(proximoTurno.getTurno().getDescripcion()).append("\n");
                resumen.append("- Horario: ").append(proximoTurno.getInfoHorario().split(" \\(")[1].replace(")", "")).append("\n");
            } else {
                resumen.append("No hay turnos programados próximamente.\n");
            }
        }
        
        txtResumen.setText(resumen.toString());
    }
}