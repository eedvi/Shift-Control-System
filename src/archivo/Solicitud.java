package archivo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class representing an employee request (solicitud)
 */
public class Solicitud {

    public enum TipoSolicitud {
        VACACIONES("Vacaciones"),
        PERMISO_PERSONAL("Permiso Personal"),
        CITA_IGSS("Cita IGSS"),
        DIA_CUMPLEANOS("Día de Cumpleaños"),
        LICENCIA_MEDICA("Licencia Médica"),
        OTRO("Otro");

        private final String descripcion;

        TipoSolicitud(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum EstadoSolicitud {
        PENDIENTE("Pendiente"),
        APROBADA("Aprobada"),
        RECHAZADA("Rechazada");

        private final String descripcion;

        EstadoSolicitud(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private static int contadorId = 1;

    private int id;
    private String empleadoDpi;
    private String empleadoNombre;
    private TipoSolicitud tipo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;
    private String aprobadoPor;
    private String motivoRechazo;
    private LocalDateTime fechaProcesamiento;

    // Constructor
    public Solicitud(String empleadoDpi, String empleadoNombre, TipoSolicitud tipo,
                    String descripcion, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.id = contadorId++;
        this.empleadoDpi = empleadoDpi;
        this.empleadoNombre = empleadoNombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = EstadoSolicitud.PENDIENTE;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmpleadoDpi() {
        return empleadoDpi;
    }

    public void setEmpleadoDpi(String empleadoDpi) {
        this.empleadoDpi = empleadoDpi;
    }

    public String getEmpleadoNombre() {
        return empleadoNombre;
    }

    public void setEmpleadoNombre(String empleadoNombre) {
        this.empleadoNombre = empleadoNombre;
    }

    public TipoSolicitud getTipo() {
        return tipo;
    }

    public void setTipo(TipoSolicitud tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public String getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(String aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public LocalDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(LocalDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    // Utility methods
    public String getFechaInicioFormateada() {
        return fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFechaFinFormateada() {
        return fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFechaSolicitudFormateada() {
        return fechaSolicitud.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // Business logic methods
    public void aprobar(String aprobadoPor) {
        this.estado = EstadoSolicitud.APROBADA;
        this.aprobadoPor = aprobadoPor;
        this.fechaProcesamiento = LocalDateTime.now();
    }

    public void rechazar(String rechazadoPor, String motivo) {
        this.estado = EstadoSolicitud.RECHAZADA;
        this.aprobadoPor = rechazadoPor; // Usuario que procesó la solicitud
        this.motivoRechazo = motivo;
        this.fechaProcesamiento = LocalDateTime.now();
    }

    public boolean isPendiente() {
        return estado == EstadoSolicitud.PENDIENTE;
    }

    public boolean isAprobada() {
        return estado == EstadoSolicitud.APROBADA;
    }

    public boolean isRechazada() {
        return estado == EstadoSolicitud.RECHAZADA;
    }

    @Override
    public String toString() {
        return "Solicitud{" +
                "id=" + id +
                ", empleadoDpi='" + empleadoDpi + '\'' +
                ", empleadoNombre='" + empleadoNombre + '\'' +
                ", tipo=" + tipo +
                ", descripcion='" + descripcion + '\'' +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", estado=" + estado +
                '}';
    }
}
