package archivo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Clase que representa una asignación de turno a un empleado
 * Incluye fechas de inicio/fin, empleado asignado y tipo de turno
 */
public class AsignacionTurno {
    
    public enum TipoTurno {
        MATUTINO("Turno Matutino", "06:00", "14:00"),
        VESPERTINO("Turno Vespertino", "14:00", "22:00"),  
        NOCTURNO("Turno Nocturno", "22:00", "06:00");
        
        private final String descripcion;
        private final String horaInicio;
        private final String horaFin;
        
        TipoTurno(String descripcion, String horaInicio, String horaFin) {
            this.descripcion = descripcion;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
        }
        
        public String getDescripcion() { return descripcion; }
        public String getHoraInicio() { return horaInicio; }
        public String getHoraFin() { return horaFin; }
        
        @Override
        public String toString() {
            return descripcion;
        }
    }
    
    private String empleadoDpi;
    private String empleadoNombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private TipoTurno turno;
    private String asignadoPor; // Usuario que hizo la asignación
    private LocalDate fechaAsignacion;
    private boolean activo;
    
    // Constructor
    public AsignacionTurno(String empleadoDpi, String empleadoNombre, LocalDate fechaInicio, 
                          LocalDate fechaFin, TipoTurno turno, String asignadoPor) {
        this.empleadoDpi = empleadoDpi;
        this.empleadoNombre = empleadoNombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.turno = turno;
        this.asignadoPor = asignadoPor;
        this.fechaAsignacion = LocalDate.now();
        this.activo = true;
    }
    
    // Constructor para cargar desde archivo
    public AsignacionTurno(String empleadoDpi, String empleadoNombre, LocalDate fechaInicio,
                          LocalDate fechaFin, TipoTurno turno, String asignadoPor,
                          LocalDate fechaAsignacion, boolean activo) {
        this.empleadoDpi = empleadoDpi;
        this.empleadoNombre = empleadoNombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.turno = turno;
        this.asignadoPor = asignadoPor;
        this.fechaAsignacion = fechaAsignacion;
        this.activo = activo;
    }
    
    // Getters y Setters
    public String getEmpleadoDpi() { return empleadoDpi; }
    public void setEmpleadoDpi(String empleadoDpi) { this.empleadoDpi = empleadoDpi; }
    
    public String getEmpleadoNombre() { return empleadoNombre; }
    public void setEmpleadoNombre(String empleadoNombre) { this.empleadoNombre = empleadoNombre; }
    
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    
    public TipoTurno getTurno() { return turno; }
    public void setTurno(TipoTurno turno) { this.turno = turno; }
    
    public String getAsignadoPor() { return asignadoPor; }
    public void setAsignadoPor(String asignadoPor) { this.asignadoPor = asignadoPor; }
    
    public LocalDate getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDate fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    /**
     * Calcula los días de duración del turno asignado
     */
    public long getDiasDuracion() {
        return java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
    }
    
    /**
     * Verifica si la asignación está vigente en una fecha específica
     */
    public boolean esVigenteEnFecha(LocalDate fecha) {
        return activo && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }
    
    /**
     * Obtiene información de horarios del turno
     */
    public String getInfoHorario() {
        return String.format("%s (%s - %s)", 
            turno.getDescripcion(), 
            turno.getHoraInicio(), 
            turno.getHoraFin());
    }
    
    /**
     * Convierte a string para persistencia
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s",
            empleadoDpi,
            empleadoNombre,
            fechaInicio.format(formatter),
            fechaFin.format(formatter),
            turno.name(),
            asignadoPor,
            fechaAsignacion.format(formatter),
            activo);
    }
    
    /**
     * Crea AsignacionTurno desde string
     */
    public static AsignacionTurno fromString(String linea) {
        try {
            String[] partes = linea.split("\\|");
            if (partes.length == 8) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                
                return new AsignacionTurno(
                    partes[0], // empleadoDpi
                    partes[1], // empleadoNombre
                    LocalDate.parse(partes[2], formatter), // fechaInicio
                    LocalDate.parse(partes[3], formatter), // fechaFin
                    TipoTurno.valueOf(partes[4]), // turno
                    partes[5], // asignadoPor
                    LocalDate.parse(partes[6], formatter), // fechaAsignacion
                    Boolean.parseBoolean(partes[7]) // activo
                );
            }
        } catch (Exception e) {
            System.err.println("Error parseando asignacion de turno: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Representación para mostrar en interfaces
     */
    public String toDisplayString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("%s - %s (%s a %s) - %s",
            empleadoNombre,
            turno.getDescripcion(),
            fechaInicio.format(formatter),
            fechaFin.format(formatter),
            activo ? "Activo" : "Inactivo");
    }
}