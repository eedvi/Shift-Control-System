package archivo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

/**
 * Ventana para crear solicitudes de vacaciones con componentes de calendario
 */
public class CrearSolicitudVacaciones extends JFrame {

    private Empleado empleadoActual;
    private DatabaseManager dbManager;
    private BitacoraManager bitacoraManager;
    private SolicitudManager solicitudManager;

    // Componentes de la interfaz
    private JComboBox<Solicitud.TipoSolicitud> cmbTipoSolicitud;
    private JDateChooser dateChooserInicio;
    private JDateChooser dateChooserFin;
    private JTextArea txtDescripcion;
    private JButton btnCrear;
    private JButton btnCancelar;

    public CrearSolicitudVacaciones(Empleado empleado) {
        this.empleadoActual = empleado;
        this.dbManager = new DatabaseManager();
        this.bitacoraManager = new BitacoraManager();
        this.solicitudManager = new SolicitudManager();

        initComponents();
        configurarVentana();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título
        JLabel lblTitulo = new JLabel("Crear Solicitud de Vacaciones");
        lblTitulo.setFont(new Font("Tahoma", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        panelPrincipal.add(lblTitulo, gbc);

        // Información del empleado
        JLabel lblEmpleado = new JLabel("Empleado: " + empleadoActual.getNombre() + " (DPI: " + empleadoActual.getDpi() + ")");
        lblEmpleado.setFont(new Font("Tahoma", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 15, 0);
        panelPrincipal.add(lblEmpleado, gbc);

        // Tipo de solicitud
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 10);

        JLabel lblTipo = new JLabel("Tipo de Solicitud:");
        lblTipo.setFont(new Font("Tahoma", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 2;
        panelPrincipal.add(lblTipo, gbc);

        cmbTipoSolicitud = new JComboBox<>(Solicitud.TipoSolicitud.values());
        cmbTipoSolicitud.setSelectedItem(Solicitud.TipoSolicitud.VACACIONES);
        cmbTipoSolicitud.setFont(new Font("Tahoma", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(cmbTipoSolicitud, gbc);

        // Fecha de inicio con calendario
        JLabel lblFechaInicio = new JLabel("Fecha de Inicio:");
        lblFechaInicio.setFont(new Font("Tahoma", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        panelPrincipal.add(lblFechaInicio, gbc);

        dateChooserInicio = new JDateChooser();
        dateChooserInicio.setFont(new Font("Tahoma", Font.PLAIN, 12));
        dateChooserInicio.setDateFormatString("dd/MM/yyyy");
        dateChooserInicio.setMinSelectableDate(new Date()); // No permitir fechas pasadas
        dateChooserInicio.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(dateChooserInicio, gbc);

        // Fecha de fin con calendario
        JLabel lblFechaFin = new JLabel("Fecha de Fin:");
        lblFechaFin.setFont(new Font("Tahoma", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        panelPrincipal.add(lblFechaFin, gbc);

        dateChooserFin = new JDateChooser();
        dateChooserFin.setFont(new Font("Tahoma", Font.PLAIN, 12));
        dateChooserFin.setDateFormatString("dd/MM/yyyy");
        dateChooserFin.setMinSelectableDate(new Date()); // No permitir fechas pasadas
        dateChooserFin.setPreferredSize(new Dimension(150, 25));
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelPrincipal.add(dateChooserFin, gbc);

        // Instrucciones para las fechas
        JLabel lblInstrucciones = new JLabel("<html><i>Seleccione las fechas usando el calendario. Las fechas pasadas no están disponibles.</i></html>");
        lblInstrucciones.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblInstrucciones.setForeground(Color.GRAY);
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 10, 0);
        panelPrincipal.add(lblInstrucciones, gbc);

        // Descripción
        JLabel lblDescripcion = new JLabel("Descripción:");
        lblDescripcion.setFont(new Font("Tahoma", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.insets = new Insets(5, 0, 5, 10);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panelPrincipal.add(lblDescripcion, gbc);

        txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setFont(new Font("Tahoma", Font.PLAIN, 12));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        gbc.gridx = 1; gbc.gridy = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(scrollDescripcion, gbc);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());

        btnCrear = new JButton("Crear Solicitud");
        btnCrear.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnCrear.setBackground(new Color(0, 123, 255));
        btnCrear.setForeground(Color.WHITE);
        btnCrear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                crearSolicitud();
            }
        });

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        panelBotones.add(btnCrear);
        panelBotones.add(btnCancelar);

        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panelPrincipal.add(panelBotones, gbc);

        add(panelPrincipal, BorderLayout.CENTER);
    }

    private void configurarVentana() {
        setTitle("Crear Solicitud de Vacaciones");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void crearSolicitud() {
        try {
            // Validar campos
            if (!validarCampos()) {
                return;
            }

            // Obtener datos del formulario
            Solicitud.TipoSolicitud tipo = (Solicitud.TipoSolicitud) cmbTipoSolicitud.getSelectedItem();
            String descripcion = txtDescripcion.getText().trim();

            // Convertir fechas de Date a LocalDateTime
            LocalDateTime fechaInicio = convertirDateALocalDateTime(dateChooserInicio.getDate());
            LocalDateTime fechaFin = convertirDateALocalDateTime(dateChooserFin.getDate());

            // Validar fechas
            if (fechaInicio.isAfter(fechaFin)) {
                JOptionPane.showMessageDialog(this,
                        "La fecha de inicio no puede ser posterior a la fecha de fin",
                        "Error de fechas", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (fechaInicio.isBefore(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))) {
                JOptionPane.showMessageDialog(this,
                        "La fecha de inicio no puede ser anterior a la fecha actual",
                        "Error de fechas", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear la solicitud
            Solicitud nuevaSolicitud = new Solicitud(
                    empleadoActual.getDpi(),
                    empleadoActual.getNombre(),
                    tipo,
                    descripcion,
                    fechaInicio,
                    fechaFin
            );

            // Guardar la solicitud
            solicitudManager.guardarSolicitud(nuevaSolicitud);

            // Registrar en bitácora
            bitacoraManager.registrarOperacion(empleadoActual.getUsername(), "CREAR_SOLICITUD",
                    "Solicitud creada: " + tipo.getDescripcion(), "ID: " + nuevaSolicitud.getId());

            // Mostrar confirmación
            JOptionPane.showMessageDialog(this,
                    "Solicitud creada exitosamente.\nID de solicitud: " + nuevaSolicitud.getId(),
                    "Solicitud creada", JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al crear la solicitud: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validarCampos() {
        if (dateChooserInicio.getDate() == null) {
            JOptionPane.showMessageDialog(this, "La fecha de inicio es requerida",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (dateChooserFin.getDate() == null) {
            JOptionPane.showMessageDialog(this, "La fecha de fin es requerida",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtDescripcion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La descripción es requerida",
                    "Campo requerido", JOptionPane.WARNING_MESSAGE);
            txtDescripcion.requestFocus();
            return false;
        }

        return true;
    }

    private LocalDateTime convertirDateALocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .withHour(0)
                .withMinute(0)
                .withSecond(0);
    }
}
