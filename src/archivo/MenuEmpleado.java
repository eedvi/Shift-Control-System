package archivo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Menu principal para empleados
 * Permite crear solicitudes de vacaciones y ver el historial de solicitudes
 */
public class MenuEmpleado extends JFrame {

    private Empleado empleadoActual;
    private DatabaseManager dbManager;
    private BitacoraManager bitacoraManager;
    
    // Componentes de la interfaz
    private JLabel lblBienvenida;
    private JButton btnCrearSolicitud;
    private JButton btnVerSolicitudes;
    private JButton btnCerrarSesion;
    private JLabel lblInfoEmpleado;

    public MenuEmpleado(Empleado empleado) {
        this.empleadoActual = empleado;
        this.dbManager = new DatabaseManager();
        this.bitacoraManager = new BitacoraManager();
        
        initComponents();
        configurarVentana();
        mostrarInformacionEmpleado();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel principal
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título de bienvenida
        lblBienvenida = new JLabel("Portal del Empleado");
        lblBienvenida.setFont(new Font("Tahoma", Font.BOLD, 28));
        lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Información del empleado
        lblInfoEmpleado = new JLabel();
        lblInfoEmpleado.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblInfoEmpleado.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Botones del menú
        btnCrearSolicitud = new JButton("Crear Solicitud de Vacaciones");
        btnCrearSolicitud.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnCrearSolicitud.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCrearSolicitud.setPreferredSize(new Dimension(350, 50));
        btnCrearSolicitud.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirCrearSolicitud();
            }
        });

        btnVerSolicitudes = new JButton("Ver Mis Solicitudes");
        btnVerSolicitudes.setFont(new Font("Tahoma", Font.BOLD, 18));
        btnVerSolicitudes.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVerSolicitudes.setPreferredSize(new Dimension(350, 50));
        btnVerSolicitudes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                abrirVerSolicitudes();
            }
        });

        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setFont(new Font("Tahoma", Font.BOLD, 16));
        btnCerrarSesion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCerrarSesion.setPreferredSize(new Dimension(200, 40));
        btnCerrarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cerrarSesion();
            }
        });

        // Agregar componentes con espaciado
        panelPrincipal.add(lblBienvenida);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 20)));
        panelPrincipal.add(lblInfoEmpleado);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 40)));
        panelPrincipal.add(btnCrearSolicitud);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 20)));
        panelPrincipal.add(btnVerSolicitudes);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 40)));
        panelPrincipal.add(btnCerrarSesion);

        add(panelPrincipal, BorderLayout.CENTER);
    }

    private void configurarVentana() {
        setTitle("Sistema de Control de Turnos - Portal del Empleado");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void mostrarInformacionEmpleado() {
        if (empleadoActual != null) {
            String info = String.format("<html><center>Bienvenido, %s<br>DPI: %s | Área: %s | Turno: %s</center></html>",
                    empleadoActual.getNombre(),
                    empleadoActual.getDpi(),
                    empleadoActual.getArea(),
                    empleadoActual.getTurno());
            lblInfoEmpleado.setText(info);
        }
    }

    private void abrirCrearSolicitud() {
        try {
            bitacoraManager.registrarOperacion(empleadoActual.getUsername(), "MENU_NAVEGACION",
                    "Acceso a creación de solicitudes", "");
            
            CrearSolicitudVacaciones ventanaCrear = new CrearSolicitudVacaciones(empleadoActual);
            ventanaCrear.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error al abrir la ventana de creación de solicitudes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirVerSolicitudes() {
        try {
            bitacoraManager.registrarOperacion(empleadoActual.getUsername(), "MENU_NAVEGACION",
                    "Acceso a visualización de solicitudes", "");
            
            VerSolicitudesEmpleado ventanaVer = new VerSolicitudesEmpleado(empleadoActual);
            ventanaVer.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error al abrir la ventana de solicitudes: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarSesion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cerrar sesión?",
                "Confirmar cierre de sesión",
                JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            bitacoraManager.registrarOperacion(empleadoActual.getUsername(), "LOGOUT",
                    "Cierre de sesión exitoso", "");
            
            Login loginWindow = new Login();
            loginWindow.setVisible(true);
            dispose();
        }
    }
}
