package archivo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Ventana principal del sistema de marcaje
 * Incluye timer, opciones de marcaje y validaciones
 */
public class SistemaMarcaje extends JFrame {
    
    private Empleado empleadoActual;
    private MarcajeManager marcajeManager;
    private BitacoraManager bitacoraManager;
    
    // Componentes de la interfaz
    private JLabel lblTitulo;
    private JLabel lblEmpleado;
    private JLabel lblTimer;
    private JButton btnEntrada;
    private JButton btnDescanso1;
    private JButton btnDescanso2;
    private JButton btnSalida;
    private JButton btnInformacion;
    private JButton btnRegresar;
    private JTextArea txtEstadoMarcajes;
    private Timer clockTimer;
    
    public SistemaMarcaje(Empleado empleado) {
        this.empleadoActual = empleado;
        this.marcajeManager = new MarcajeManager();
        this.bitacoraManager = new BitacoraManager();
        
        initComponents();
        configurarVentana();
        iniciarTimer();
        actualizarEstadoMarcajes();
        
        // Registrar acceso al sistema de marcaje
        bitacoraManager.registrarOperacion(empleado.getUsername(), "ACCESO_MARCAJE",
            "Acceso al sistema de marcaje", empleado.getDpi());
    }
    
    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Panel superior con título y información del empleado
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        lblTitulo = new JLabel("Sistema de Marcaje", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 28));
        
        lblEmpleado = new JLabel(String.format("Empleado: %s (%s)", 
            empleadoActual.getNombre(), empleadoActual.getDpi()), SwingConstants.CENTER);
        lblEmpleado.setFont(new Font("Tahoma", Font.PLAIN, 16));
        
        panelSuperior.add(lblTitulo, BorderLayout.NORTH);
        panelSuperior.add(lblEmpleado, BorderLayout.SOUTH);
        
        // Panel central con timer y botones
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Timer
        lblTimer = new JLabel("", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Courier New", Font.BOLD, 24));
        lblTimer.setBorder(BorderFactory.createTitledBorder("Hora Actual"));
        
        // Panel de botones de marcaje
        JPanel panelBotones = new JPanel(new GridLayout(3, 2, 10, 10));
        panelBotones.setBorder(BorderFactory.createTitledBorder("Opciones de Marcaje"));
        
        btnEntrada = new JButton("Marcar Entrada");
        btnEntrada.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnEntrada.addActionListener(e -> marcarEntrada());
        
        btnDescanso1 = new JButton("Marcar Descanso");
        btnDescanso1.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnDescanso1.addActionListener(e -> marcarDescanso());
        
        btnDescanso2 = new JButton("Marcar Descanso");
        btnDescanso2.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnDescanso2.addActionListener(e -> marcarDescanso());
        
        btnSalida = new JButton("Marcar Salida");
        btnSalida.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnSalida.addActionListener(e -> marcarSalida());
        
        btnInformacion = new JButton("Información del Marcaje");
        btnInformacion.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnInformacion.addActionListener(e -> mostrarInformacionMarcaje());
        
        btnRegresar = new JButton("Regresar");
        btnRegresar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnRegresar.addActionListener(e -> regresar());
        
        panelBotones.add(btnEntrada);
        panelBotones.add(btnDescanso1);
        panelBotones.add(btnDescanso2);
        panelBotones.add(btnSalida);
        panelBotones.add(btnInformacion);
        panelBotones.add(btnRegresar);
        
        panelCentral.add(lblTimer, BorderLayout.NORTH);
        panelCentral.add(panelBotones, BorderLayout.CENTER);
        
        // Panel inferior con estado de marcajes
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        txtEstadoMarcajes = new JTextArea(6, 40);
        txtEstadoMarcajes.setEditable(false);
        txtEstadoMarcajes.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtEstadoMarcajes.setBorder(BorderFactory.createTitledBorder("Estado de Marcajes del Día"));
        
        JScrollPane scrollEstado = new JScrollPane(txtEstadoMarcajes);
        panelInferior.add(scrollEstado, BorderLayout.CENTER);
        
        // Agregar paneles a la ventana
        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void configurarVentana() {
        setTitle("Sistema de Control de Turnos - Marcaje");
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    private void iniciarTimer() {
        clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarTimer();
            }
        });
        clockTimer.start();
        actualizarTimer();
    }
    
    private void actualizarTimer() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        String tiempo = String.format("<html><center>%s<br><small>%s</small></center></html>",
            ahora.format(formatter), ahora.format(dateFormatter));
        lblTimer.setText(tiempo);
    }
    
    private void actualizarEstadoMarcajes() {
        List<Marcaje> marcajesHoy = marcajeManager.getMarcajesDelDia(empleadoActual.getDpi());
        
        StringBuilder estado = new StringBuilder();
        estado.append("MARCAJES DE HOY:\n");
        estado.append("================\n");
        
        if (marcajesHoy.isEmpty()) {
            estado.append("No hay marcajes registrados hoy.\n");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            
            for (Marcaje marcaje : marcajesHoy) {
                estado.append(String.format("%-18s: %s\n", 
                    marcaje.getTipoDescripcion(),
                    marcaje.getFechaHora().format(formatter)));
            }
        }
        
        estado.append("\nPROXIMO MARCAJE DISPONIBLE:\n");
        estado.append("===========================\n");
        estado.append(determinarProximoMarcaje());
        
        txtEstadoMarcajes.setText(estado.toString());
    }
    
    private String determinarProximoMarcaje() {
        MarcajeManager.ResultadoValidacion validacionEntrada = 
            marcajeManager.validarMarcajeEntrada(empleadoActual.getDpi());
        if (validacionEntrada.isValido()) {
            return "→ Marcar Entrada";
        }
        
        MarcajeManager.ResultadoValidacion validacionDescanso1 = 
            marcajeManager.validarPrimerDescanso(empleadoActual.getDpi());
        if (validacionDescanso1.isValido()) {
            return "→ Marcar Primer Descanso";
        }
        
        MarcajeManager.ResultadoValidacion validacionDescanso2 = 
            marcajeManager.validarSegundoDescanso(empleadoActual.getDpi());
        if (validacionDescanso2.isValido()) {
            return "→ Marcar Segundo Descanso";
        }
        
        MarcajeManager.ResultadoValidacion validacionSalida = 
            marcajeManager.validarMarcajeSalida(empleadoActual.getDpi());
        if (validacionSalida.isValido()) {
            return "→ Marcar Salida";
        }
        
        return "→ Todos los marcajes completados";
    }
    
    private void marcarEntrada() {
        MarcajeManager.ResultadoMarcaje resultado = marcajeManager.marcarEntrada(empleadoActual);
        
        if (resultado.isExito()) {
            JOptionPane.showMessageDialog(this, resultado.getMensaje(), 
                "Marcaje Exitoso", JOptionPane.INFORMATION_MESSAGE);
            actualizarEstadoMarcajes();
        } else {
            JOptionPane.showMessageDialog(this, resultado.getMensaje(), 
                "Error en Marcaje", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void marcarDescanso() {
        // Determinar qué descanso se puede marcar
        MarcajeManager.ResultadoValidacion validacionDescanso1 = 
            marcajeManager.validarPrimerDescanso(empleadoActual.getDpi());
        
        MarcajeManager.ResultadoMarcaje resultado;
        
        if (validacionDescanso1.isValido()) {
            resultado = marcajeManager.marcarPrimerDescanso(empleadoActual);
        } else {
            MarcajeManager.ResultadoValidacion validacionDescanso2 = 
                marcajeManager.validarSegundoDescanso(empleadoActual.getDpi());
            
            if (validacionDescanso2.isValido()) {
                resultado = marcajeManager.marcarSegundoDescanso(empleadoActual);
            } else {
                resultado = new MarcajeManager.ResultadoMarcaje(false, 
                    "No se puede marcar descanso en este momento", null);
            }
        }
        
        if (resultado.isExito()) {
            JOptionPane.showMessageDialog(this, resultado.getMensaje(), 
                "Marcaje Exitoso", JOptionPane.INFORMATION_MESSAGE);
            actualizarEstadoMarcajes();
        } else {
            JOptionPane.showMessageDialog(this, resultado.getMensaje(), 
                "Error en Marcaje", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void marcarSalida() {
        MarcajeManager.ResultadoMarcaje resultado = marcajeManager.marcarSalida(empleadoActual);
        
        if (resultado.isExito()) {
            JOptionPane.showMessageDialog(this, resultado.getMensaje(), 
                "Marcaje Exitoso", JOptionPane.INFORMATION_MESSAGE);
            actualizarEstadoMarcajes();
        } else {
            JOptionPane.showMessageDialog(this, resultado.getMensaje(), 
                "Error en Marcaje", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mostrarInformacionMarcaje() {
        InformacionMarcaje ventanaInfo = new InformacionMarcaje(empleadoActual, marcajeManager);
        ventanaInfo.setVisible(true);
    }
    
    private void regresar() {
        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea regresar al menú principal?",
            "Confirmar regreso",
            JOptionPane.YES_NO_OPTION);
        
        if (opcion == JOptionPane.YES_OPTION) {
            if (clockTimer != null) {
                clockTimer.stop();
            }
            
            bitacoraManager.registrarOperacion(empleadoActual.getUsername(), "SALIDA_MARCAJE",
                "Salida del sistema de marcaje", empleadoActual.getDpi());
            
            dispose();
        }
    }
    
    @Override
    public void dispose() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        super.dispose();
    }
}