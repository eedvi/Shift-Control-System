package archivo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Interface for Role Management - allows AdminRRHH to assign and remove roles
 */
public class GestionRoles extends JFrame {
    private Empleado usuarioActual;
    private DatabaseManager dbManager;
    private BitacoraManager bitacoraManager;
    private EmailService emailService;
    
    private JComboBox<String> comboEmpleados;
    private JComboBox<String> comboRoles;
    private JButton btnAsignarRol;
    private JButton btnRemoverRol;
    private JButton btnRegresar;
    private JTable tablaEmpleados;
    private DefaultTableModel modeloTabla;
    
    public GestionRoles(Empleado usuario, DatabaseManager db, BitacoraManager bitacora) {
        this.usuarioActual = usuario;
        this.dbManager = db;
        this.bitacoraManager = bitacora;
        this.emailService = new EmailService();
        
        initComponents();
        cargarEmpleados();
    }
    
    private void initComponents() {
        setTitle("Gestión de Roles - Sistema RRHH");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Título
        JLabel lblTitulo = new JLabel("Gestión de Roles");
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 30, 20);
        panelPrincipal.add(lblTitulo, gbc);
        
        // Selección de empleado
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 20, 5, 10);
        gbc.gridx = 0; gbc.gridy = 1;
        panelPrincipal.add(new JLabel("Empleado:"), gbc);
        
        comboEmpleados = new JComboBox<>();
        comboEmpleados.setFont(new Font("Tahoma", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(comboEmpleados, gbc);
        
        // Selección de rol
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panelPrincipal.add(new JLabel("Rol:"), gbc);
        
        comboRoles = new JComboBox<>(new String[]{"Empleado", "AdminRRHH", "Supervisor", "Jefe"});
        comboRoles.setFont(new Font("Tahoma", Font.PLAIN, 14));
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(comboRoles, gbc);
        
        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        
        btnAsignarRol = new JButton("Asignar Rol");
        btnAsignarRol.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnAsignarRol.addActionListener(this::asignarRol);
        panelBotones.add(btnAsignarRol);
        
        btnRemoverRol = new JButton("Remover Rol");
        btnRemoverRol.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRemoverRol.addActionListener(this::removerRol);
        panelBotones.add(btnRemoverRol);
        
        btnRegresar = new JButton("Regresar");
        btnRegresar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRegresar.addActionListener(this::regresar);
        panelBotones.add(btnRegresar);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 20, 20);
        panelPrincipal.add(panelBotones, gbc);
        
        // Crear tabla de empleados
        crearTablaEmpleados();
        
        add(panelPrincipal, BorderLayout.NORTH);
        add(crearPanelTabla(), BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        
        // Cargar información inicial
        cargarDatosTabla();
    }
    
    private void crearTablaEmpleados() {
        // Crear modelo de tabla con columnas específicas
        String[] columnas = {"Usuario", "Nombre Completo", "Rol", "Estado", "Email"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        
        tablaEmpleados = new JTable(modeloTabla);
        
        // Configurar el aspecto de la tabla
        tablaEmpleados.setFont(new Font("Tahoma", Font.PLAIN, 12));
        tablaEmpleados.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        tablaEmpleados.setRowHeight(25);
        tablaEmpleados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Configurar ancho de columnas
        tablaEmpleados.getColumnModel().getColumn(0).setPreferredWidth(100); // Usuario
        tablaEmpleados.getColumnModel().getColumn(1).setPreferredWidth(200); // Nombre
        tablaEmpleados.getColumnModel().getColumn(2).setPreferredWidth(120); // Rol
        tablaEmpleados.getColumnModel().getColumn(3).setPreferredWidth(80);  // Estado
        tablaEmpleados.getColumnModel().getColumn(4).setPreferredWidth(180); // Email
        
        // Agregar ordenamiento
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaEmpleados.setRowSorter(sorter);
        
        // Configurar renderer personalizado para colores alternados
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(240, 248, 255)); // Azul muy claro
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                
                // Colorear según el rol
                if (column == 2 && value != null) { // Columna de rol
                    String rol = value.toString();
                    if (!isSelected) {
                        switch (rol) {
                            case "AdminRRHH":
                                c.setBackground(new Color(255, 240, 240)); // Rojo claro
                                break;
                            case "Supervisor":
                                c.setBackground(new Color(255, 255, 240)); // Amarillo claro
                                break;
                            case "Jefe":
                                c.setBackground(new Color(240, 255, 240)); // Verde claro
                                break;
                            default: // Empleado
                                if (row % 2 == 0) {
                                    c.setBackground(new Color(240, 248, 255));
                                } else {
                                    c.setBackground(Color.WHITE);
                                }
                                break;
                        }
                    }
                }
                
                return c;
            }
        };
        
        // Aplicar el renderer a todas las columnas
        for (int i = 0; i < tablaEmpleados.getColumnCount(); i++) {
            tablaEmpleados.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
    
    private JPanel crearPanelTabla() {
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Información de Empleados", 
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            new Font("Tahoma", Font.BOLD, 14)));
        
        JScrollPane scrollPane = new JScrollPane(tablaEmpleados);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        
        // Panel de estadísticas
        JPanel panelStats = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblStats = new JLabel();
        lblStats.setFont(new Font("Tahoma", Font.ITALIC, 11));
        panelStats.add(lblStats);
        
        panelTabla.add(scrollPane, BorderLayout.CENTER);
        panelTabla.add(panelStats, BorderLayout.SOUTH);
        
        return panelTabla;
    }
    
    private void cargarEmpleados() {
        comboEmpleados.removeAllItems();
        List<Empleado> empleados = dbManager.obtenerTodosEmpleados();
        
        for (Empleado emp : empleados) {
            comboEmpleados.addItem(emp.getUsername() + " - " + emp.getNombre());
        }
    }
    
    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        List<Empleado> empleados = dbManager.obtenerTodosEmpleados();
        
        for (Empleado emp : empleados) {
            Object[] fila = {
                emp.getUsername(),
                emp.getNombre(),
                emp.getRole(),
                emp.getEstado(),
                emp.getEmail() != null ? emp.getEmail() : "No disponible"
            };
            modeloTabla.addRow(fila);
        }
        
        // Actualizar estadísticas
        actualizarEstadisticas(empleados);
    }
    
    private void actualizarEstadisticas(List<Empleado> empleados) {
        int totalEmpleados = empleados.size();
        int adminRRHH = 0, supervisores = 0, jefes = 0, empleadosNormales = 0;
        int activos = 0, inactivos = 0;
        
        for (Empleado emp : empleados) {
            // Contar por rol
            switch (emp.getRole()) {
                case "AdminRRHH": adminRRHH++; break;
                case "Supervisor": supervisores++; break;
                case "Jefe": jefes++; break;
                default: empleadosNormales++; break;
            }
            
            // Contar por estado
            if ("Activo".equals(emp.getEstado())) {
                activos++;
            } else {
                inactivos++;
            }
        }
        
        // Buscar el JLabel de estadísticas y actualizarlo
        Container parent = tablaEmpleados.getParent().getParent().getParent();
        if (parent instanceof JPanel) {
            JPanel panelTabla = (JPanel) parent;
            Component[] components = panelTabla.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    if (panel.getLayout() instanceof FlowLayout) {
                        Component[] subComponents = panel.getComponents();
                        for (Component subComp : subComponents) {
                            if (subComp instanceof JLabel) {
                                JLabel lblStats = (JLabel) subComp;
                                lblStats.setText(String.format(
                                    "Total: %d | AdminRRHH: %d | Supervisores: %d | Jefes: %d | Empleados: %d | Activos: %d | Inactivos: %d",
                                    totalEmpleados, adminRRHH, supervisores, jefes, empleadosNormales, activos, inactivos
                                ));
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
    
    private void asignarRol(ActionEvent e) {
        String seleccion = (String) comboEmpleados.getSelectedItem();
        if (seleccion == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String username = seleccion.split(" - ")[0];
        String nuevoRol = (String) comboRoles.getSelectedItem();
        
        if (dbManager.asignarRol(username, nuevoRol)) {
            JOptionPane.showMessageDialog(this, "Asignación de rol exitosa", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Registrar en bitácora
            bitacoraManager.registrarOperacion(usuarioActual.getUsername(), "ASSIGN_ROLE", 
                                             "Rol " + nuevoRol + " asignado", username);
            
            cargarDatosTabla(); // Actualizar tabla
        } else {
            JOptionPane.showMessageDialog(this, "Error en la asignación de rol", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void removerRol(ActionEvent e) {
        String seleccion = (String) comboEmpleados.getSelectedItem();
        if (seleccion == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un empleado", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String username = seleccion.split(" - ")[0];
        
        if (dbManager.removerRol(username)) {
            JOptionPane.showMessageDialog(this, "Eliminación de rol exitosa", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
            // Registrar en bitácora
            bitacoraManager.registrarOperacion(usuarioActual.getUsername(), "REMOVE_ROLE", 
                                             "Rol removido, asignado rol Empleado", username);
            
            cargarDatosTabla(); // Actualizar tabla
        } else {
            JOptionPane.showMessageDialog(this, "Error en la eliminación de rol", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void regresar(ActionEvent e) {
        Menu menu = new Menu(usuarioActual);
        menu.setVisible(true);
        dispose();
    }
}
