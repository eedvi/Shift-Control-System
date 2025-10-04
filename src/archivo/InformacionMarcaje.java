package archivo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ventana de información detallada de marcajes
 * Permite visualizar historial y filtrar por fechas
 */
public class InformacionMarcaje extends JFrame {
    
    private Empleado empleadoActual;
    private MarcajeManager marcajeManager;
    
    // Componentes de la interfaz
    private JLabel lblTitulo;
    private JLabel lblEmpleado;
    private JTable tablaMarcajes;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> comboFiltroFecha;
    private JButton btnFiltrar;
    private JButton btnLimpiarFiltro;
    private JButton btnCerrar;
    private JTextArea txtResumen;
    
    public InformacionMarcaje(Empleado empleado, MarcajeManager marcajeManager) {
        this.empleadoActual = empleado;
        this.marcajeManager = marcajeManager;
        
        initComponents();
        configurarVentana();
        cargarDatos();
        actualizarResumen();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior con título y empleado
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        lblTitulo = new JLabel("Información de Marcajes", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 22));
        
        lblEmpleado = new JLabel(String.format("Empleado: %s (%s)", 
            empleadoActual.getNombre(), empleadoActual.getDpi()), SwingConstants.CENTER);
        lblEmpleado.setFont(new Font("Tahoma", Font.PLAIN, 14));
        
        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(lblEmpleado, BorderLayout.SOUTH);
        
        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        
        panelFiltros.add(new JLabel("Mostrar:"));
        
        comboFiltroFecha = new JComboBox<>(new String[] {
            "Todos los marcajes",
            "Solo hoy",
            "Última semana",
            "Último mes"
        });
        comboFiltroFecha.setFont(new Font("Tahoma", Font.PLAIN, 12));
        
        btnFiltrar = new JButton("Aplicar Filtro");
        btnFiltrar.addActionListener(e -> aplicarFiltro());
        
        btnLimpiarFiltro = new JButton("Limpiar");
        btnLimpiarFiltro.addActionListener(e -> limpiarFiltro());
        
        panelFiltros.add(comboFiltroFecha);
        panelFiltros.add(btnFiltrar);
        panelFiltros.add(btnLimpiarFiltro);
        
        // Panel central con tabla
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        // Configurar tabla
        String[] columnas = {"Fecha", "Hora", "Tipo de Marcaje", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaMarcajes = new JTable(modeloTabla);
        tablaMarcajes.setFont(new Font("Courier New", Font.PLAIN, 11));
        tablaMarcajes.setRowHeight(25);
        tablaMarcajes.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaMarcajes.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaMarcajes.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaMarcajes.getColumnModel().getColumn(3).setPreferredWidth(100);
        
        JScrollPane scrollTabla = new JScrollPane(tablaMarcajes);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Historial de Marcajes"));
        scrollTabla.setPreferredSize(new Dimension(500, 300));
        
        panelCentral.add(panelFiltros, BorderLayout.NORTH);
        panelCentral.add(scrollTabla, BorderLayout.CENTER);
        
        // Panel inferior con resumen y botones
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        
        txtResumen = new JTextArea(6, 40);
        txtResumen.setEditable(false);
        txtResumen.setFont(new Font("Courier New", Font.PLAIN, 11));
        txtResumen.setBorder(BorderFactory.createTitledBorder("Resumen"));
        
        JScrollPane scrollResumen = new JScrollPane(txtResumen);
        
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnCerrar.addActionListener(e -> dispose());
        panelBotones.add(btnCerrar);
        
        panelInferior.add(scrollResumen, BorderLayout.CENTER);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);
        
        // Agregar paneles a la ventana
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void configurarVentana() {
        setTitle("Sistema de Control de Turnos - Información de Marcajes");
        setSize(650, 700);
        setLocationRelativeTo(null);
        setResizable(true);
    }
    
    private void cargarDatos() {
        List<Marcaje> todosMarcajes = marcajeManager.getTodosMarcajes(empleadoActual.getDpi());
        mostrarMarcajes(todosMarcajes);
    }
    
    private void mostrarMarcajes(List<Marcaje> marcajes) {
        // Limpiar tabla
        modeloTabla.setRowCount(0);
        
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        for (Marcaje marcaje : marcajes) {
            String fecha = marcaje.getFechaHora().format(formatoFecha);
            String hora = marcaje.getFechaHora().format(formatoHora);
            String tipo = marcaje.getTipoDescripcion();
            String estado = determinarEstadoMarcaje(marcaje);
            
            modeloTabla.addRow(new Object[]{fecha, hora, tipo, estado});
        }
    }
    
    private String determinarEstadoMarcaje(Marcaje marcaje) {
        if (marcaje.getTipo() == Marcaje.TipoMarcaje.ENTRADA) {
            // Verificar si es entrada tardía (después de las 8:00 AM)
            if (marcaje.getFechaHora().getHour() > 8 || 
                (marcaje.getFechaHora().getHour() == 8 && marcaje.getFechaHora().getMinute() > 0)) {
                return "TARDIA";
            } else {
                return "PUNTUAL";
            }
        } else {
            return "NORMAL";
        }
    }
    
    private void aplicarFiltro() {
        String filtroSeleccionado = (String) comboFiltroFecha.getSelectedItem();
        List<Marcaje> marcajesFiltrados = obtenerMarcajesFiltrados(filtroSeleccionado);
        mostrarMarcajes(marcajesFiltrados);
        actualizarResumenConFiltro(marcajesFiltrados, filtroSeleccionado);
    }
    
    private void limpiarFiltro() {
        comboFiltroFecha.setSelectedIndex(0);
        cargarDatos();
        actualizarResumen();
    }
    
    private List<Marcaje> obtenerMarcajesFiltrados(String filtro) {
        List<Marcaje> todosMarcajes = marcajeManager.getTodosMarcajes(empleadoActual.getDpi());
        
        LocalDate hoy = LocalDate.now();
        
        switch (filtro) {
            case "Solo hoy":
                return todosMarcajes.stream()
                    .filter(m -> m.getFechaHora().toLocalDate().equals(hoy))
                    .collect(Collectors.toList());
                    
            case "Última semana":
                LocalDate unaSemanaAtras = hoy.minusWeeks(1);
                return todosMarcajes.stream()
                    .filter(m -> m.getFechaHora().toLocalDate().isAfter(unaSemanaAtras) || 
                                m.getFechaHora().toLocalDate().equals(unaSemanaAtras))
                    .collect(Collectors.toList());
                    
            case "Último mes":
                LocalDate unMesAtras = hoy.minusMonths(1);
                return todosMarcajes.stream()
                    .filter(m -> m.getFechaHora().toLocalDate().isAfter(unMesAtras) ||
                                m.getFechaHora().toLocalDate().equals(unMesAtras))
                    .collect(Collectors.toList());
                    
            default:
                return todosMarcajes;
        }
    }
    
    private void actualizarResumen() {
        List<Marcaje> todosMarcajes = marcajeManager.getTodosMarcajes(empleadoActual.getDpi());
        actualizarResumenConFiltro(todosMarcajes, "Todos los marcajes");
    }
    
    private void actualizarResumenConFiltro(List<Marcaje> marcajes, String filtro) {
        StringBuilder resumen = new StringBuilder();
        
        resumen.append("RESUMEN DE MARCAJES - ").append(filtro.toUpperCase()).append("\n");
        for (int i = 0; i < 50; i++) resumen.append("=");
        resumen.append("\n\n");
        
        if (marcajes.isEmpty()) {
            resumen.append("No hay marcajes registrados para el período seleccionado.\n");
        } else {
            // Contar por tipos
            long entradas = marcajes.stream().filter(m -> m.getTipo() == Marcaje.TipoMarcaje.ENTRADA).count();
            long descansos = marcajes.stream().filter(m -> 
                m.getTipo() == Marcaje.TipoMarcaje.PRIMER_DESCANSO || 
                m.getTipo() == Marcaje.TipoMarcaje.SEGUNDO_DESCANSO).count();
            long salidas = marcajes.stream().filter(m -> m.getTipo() == Marcaje.TipoMarcaje.SALIDA).count();
            
            resumen.append("Total de marcajes: ").append(marcajes.size()).append("\n");
            resumen.append("- Entradas: ").append(entradas).append("\n");
            resumen.append("- Descansos: ").append(descansos).append("\n");
            resumen.append("- Salidas: ").append(salidas).append("\n\n");
            
            // Contar entradas tardías
            long entradasTardias = marcajes.stream()
                .filter(m -> m.getTipo() == Marcaje.TipoMarcaje.ENTRADA)
                .filter(m -> m.getFechaHora().getHour() > 8 || 
                           (m.getFechaHora().getHour() == 8 && m.getFechaHora().getMinute() > 0))
                .count();
            
            if (entradas > 0) {
                resumen.append("Análisis de puntualidad:\n");
                resumen.append("- Entradas puntuales: ").append(entradas - entradasTardias).append("\n");
                resumen.append("- Entradas tardías: ").append(entradasTardias).append("\n");
                
                double porcentajePuntualidad = ((double)(entradas - entradasTardias) / entradas) * 100;
                resumen.append("- Porcentaje de puntualidad: ").append(String.format("%.1f%%", porcentajePuntualidad)).append("\n\n");
            }
            
            // Mostrar último marcaje
            if (!marcajes.isEmpty()) {
                Marcaje ultimoMarcaje = marcajes.get(marcajes.size() - 1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                
                resumen.append("Último marcaje:\n");
                resumen.append("- Tipo: ").append(ultimoMarcaje.getTipoDescripcion()).append("\n");
                resumen.append("- Fecha y hora: ").append(ultimoMarcaje.getFechaHora().format(formatter)).append("\n");
            }
        }
        
        txtResumen.setText(resumen.toString());
    }
}