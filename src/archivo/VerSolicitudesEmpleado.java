package archivo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Ventana para que los empleados vean sus solicitudes de vacaciones
 */
public class VerSolicitudesEmpleado extends JFrame {

    private Empleado empleadoActual;
    private SolicitudManager solicitudManager;
    private BitacoraManager bitacoraManager;
    
    // Componentes de la interfaz
    private JTable tablaSolicitudes;
    private DefaultTableModel modeloTabla;
    private JButton btnActualizar;
    private JButton btnCerrar;
    private JLabel lblEstadisticas;

    public VerSolicitudesEmpleado(Empleado empleado) {
        this.empleadoActual = empleado;
        this.solicitudManager = new SolicitudManager();
        this.bitacoraManager = new BitacoraManager();
        
        initComponents();
        configurarVentana();
        cargarSolicitudes();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior con título e información
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitulo = new JLabel("Mis Solicitudes de Vacaciones");
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 20));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lblEmpleado = new JLabel("Empleado: " + empleadoActual.getNombre() + " (DPI: " + empleadoActual.getDpi() + ")");
        lblEmpleado.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lblEmpleado.setHorizontalAlignment(SwingConstants.CENTER);

        lblEstadisticas = new JLabel();
        lblEstadisticas.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblEstadisticas.setHorizontalAlignment(SwingConstants.CENTER);

        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(lblEmpleado, BorderLayout.CENTER);
        panelSuperior.add(lblEstadisticas, BorderLayout.SOUTH);

        // Configurar tabla
        String[] columnas = {
            "ID", "Tipo", "Descripción", "Fecha Inicio", "Fecha Fin", 
            "Fecha Solicitud", "Estado", "Procesado Por", "Observaciones"
        };
        
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla de solo lectura
            }
        };

        tablaSolicitudes = new JTable(modeloTabla);
        tablaSolicitudes.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tablaSolicitudes.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        tablaSolicitudes.setRowHeight(25);
        tablaSolicitudes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurar anchos de columnas
        tablaSolicitudes.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaSolicitudes.getColumnModel().getColumn(1).setPreferredWidth(120); // Tipo
        tablaSolicitudes.getColumnModel().getColumn(2).setPreferredWidth(200); // Descripción
        tablaSolicitudes.getColumnModel().getColumn(3).setPreferredWidth(100); // Fecha Inicio
        tablaSolicitudes.getColumnModel().getColumn(4).setPreferredWidth(100); // Fecha Fin
        tablaSolicitudes.getColumnModel().getColumn(5).setPreferredWidth(100); // Fecha Solicitud
        tablaSolicitudes.getColumnModel().getColumn(6).setPreferredWidth(80);  // Estado
        tablaSolicitudes.getColumnModel().getColumn(7).setPreferredWidth(120); // Procesado Por
        tablaSolicitudes.getColumnModel().getColumn(8).setPreferredWidth(150); // Observaciones

        JScrollPane scrollPane = new JScrollPane(tablaSolicitudes);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnActualizar.setBackground(new Color(40, 167, 69));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarSolicitudes();
            }
        });

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        panelBotones.add(btnActualizar);
        panelBotones.add(btnCerrar);

        // Agregar componentes al frame
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void configurarVentana() {
        setTitle("Mis Solicitudes - " + empleadoActual.getNombre());
        setSize(900, 500);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void cargarSolicitudes() {
        try {
            // Limpiar tabla
            modeloTabla.setRowCount(0);
            
            // Obtener solicitudes del empleado
            List<Solicitud> solicitudes = solicitudManager.obtenerSolicitudesPorEmpleado(empleadoActual.getDpi());
            
            // Contadores para estadísticas
            int pendientes = 0, aprobadas = 0, rechazadas = 0;
            
            // Llenar tabla
            for (Solicitud solicitud : solicitudes) {
                Object[] fila = {
                    solicitud.getId(),
                    solicitud.getTipo().getDescripcion(),
                    truncarTexto(solicitud.getDescripcion(), 50),
                    solicitud.getFechaInicioFormateada().substring(0, 10), // Solo fecha, sin hora
                    solicitud.getFechaFinFormateada().substring(0, 10),
                    solicitud.getFechaSolicitudFormateada().substring(0, 10),
                    solicitud.getEstado().getDescripcion(),
                    solicitud.getAprobadoPor() != null ? solicitud.getAprobadoPor() : "",
                    solicitud.getMotivoRechazo() != null ? truncarTexto(solicitud.getMotivoRechazo(), 30) : ""
                };
                
                modeloTabla.addRow(fila);
                
                // Contar estados
                switch (solicitud.getEstado()) {
                    case PENDIENTE:
                        pendientes++;
                        break;
                    case APROBADA:
                        aprobadas++;
                        break;
                    case RECHAZADA:
                        rechazadas++;
                        break;
                }
            }
            
            // Actualizar estadísticas
            String estadisticas = String.format("Total: %d solicitudes | Pendientes: %d | Aprobadas: %d | Rechazadas: %d",
                    solicitudes.size(), pendientes, aprobadas, rechazadas);
            lblEstadisticas.setText(estadisticas);
            
            // Aplicar colores a las filas según el estado
            aplicarColoresFilas();
            
            // Registrar consulta en bitácora
            bitacoraManager.registrarOperacion(empleadoActual.getUsername(), "CONSULTA_SOLICITUDES",
                    "Consulta de solicitudes propias", "Total: " + solicitudes.size());
                    
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar las solicitudes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void aplicarColoresFilas() {
        tablaSolicitudes.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    String estado = (String) table.getValueAt(row, 6); // Columna de estado
                    
                    switch (estado) {
                        case "Pendiente":
                            c.setBackground(new Color(255, 243, 205)); // Amarillo claro
                            break;
                        case "Aprobada":
                            c.setBackground(new Color(212, 237, 218)); // Verde claro
                            break;
                        case "Rechazada":
                            c.setBackground(new Color(248, 215, 218)); // Rojo claro
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                            break;
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                }
                
                return c;
            }
        });
    }
    
    private String truncarTexto(String texto, int maxLength) {
        if (texto == null) return "";
        if (texto.length() <= maxLength) return texto;
        return texto.substring(0, maxLength - 3) + "...";
    }
}
