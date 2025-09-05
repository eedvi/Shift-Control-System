package archivo;

import javax.swing.*;
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
    private JTextArea txtAreaInfo;
    
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
        
        // Área de información
        txtAreaInfo = new JTextArea(10, 50);
        txtAreaInfo.setEditable(false);
        txtAreaInfo.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(txtAreaInfo);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Información de Empleados"));
        
        add(panelPrincipal, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(null);
        
        // Cargar información inicial
        mostrarInformacionEmpleados();
    }
    
    private void cargarEmpleados() {
        comboEmpleados.removeAllItems();
        List<Empleado> empleados = dbManager.obtenerTodosEmpleados();
        
        for (Empleado emp : empleados) {
            comboEmpleados.addItem(emp.getUsername() + " - " + emp.getNombre());
        }
    }
    
    private void mostrarInformacionEmpleados() {
        StringBuilder info = new StringBuilder();
        info.append("=== EMPLEADOS Y SUS ROLES ===\n\n");
        
        List<Empleado> empleados = dbManager.obtenerTodosEmpleados();
        for (Empleado emp : empleados) {
            info.append(String.format("Usuario: %s | Nombre: %s | Rol: %s | Estado: %s\n",
                    emp.getUsername(), emp.getNombre(), emp.getRole(), emp.getEstado()));
        }
        
        txtAreaInfo.setText(info.toString());
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
            
            mostrarInformacionEmpleados();
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
            
            mostrarInformacionEmpleados();
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
