package archivo;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manager para la gestión de asignaciones de turnos
 * Implementa validaciones y reglas de negocio RN01 y RN02
 */
public class AsignacionTurnoManager {
    
    private static final String ASIGNACIONES_FILE = "asignaciones_turnos.txt";
    private List<AsignacionTurno> asignaciones;
    private DatabaseManager dbManager;
    private BitacoraManager bitacoraManager;
    
    public AsignacionTurnoManager() {
        this.asignaciones = new ArrayList<>();
        this.dbManager = new DatabaseManager();
        this.bitacoraManager = new BitacoraManager();
        cargarAsignaciones();
    }
    
    /**
     * Obtiene todos los empleados disponibles para asignar turnos
     */
    public List<Empleado> getEmpleadosDisponibles() {
        return dbManager.obtenerTodosEmpleados().stream()
            .filter(emp -> "Activo".equals(emp.getEstado()))
            .filter(emp -> "Empleado".equals(emp.getRole()) || "AdminRRHH".equals(emp.getRole()))
            .collect(Collectors.toList());
    }
    
    /**
     * Valida si se puede crear una asignación de turno
     * Implementa reglas de negocio RN01 y RN02
     */
    public ResultadoValidacion validarAsignacion(String empleadoDpi, LocalDate fechaInicio, 
                                               LocalDate fechaFin, AsignacionTurno.TipoTurno turno,
                                               String adminUser) {
        
        // RN01: Validar que el usuario sea administrador de area
        Empleado admin = dbManager.obtenerEmpleadoPorUsername(adminUser);
        if (admin == null || !"AdminRRHH".equals(admin.getRole())) {
            return new ResultadoValidacion(false, 
                "Solo los administradores de area pueden asignar turnos");
        }
        
        // Validar que las fechas sean coherentes
        if (fechaInicio.isAfter(fechaFin)) {
            return new ResultadoValidacion(false, 
                "La fecha de inicio no puede ser posterior a la fecha fin");
        }
        
        // Validar que no sea en el pasado
        if (fechaInicio.isBefore(LocalDate.now())) {
            return new ResultadoValidacion(false, 
                "No se pueden asignar turnos en fechas pasadas");
        }
        
        // RN02: Validar duración del turno (máximo 8 horas por día)
        long diasDuracion = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
        if (diasDuracion > 7) { // Máximo una semana por asignación
            return new ResultadoValidacion(false, 
                "No se pueden asignar turnos por más de 7 días consecutivos");
        }
        
        // Validar que el empleado exista y esté activo
        Empleado empleado = dbManager.obtenerEmpleadoPorDpi(empleadoDpi);
        if (empleado == null) {
            return new ResultadoValidacion(false, "El empleado especificado no existe");
        }
        
        if (!"Activo".equals(empleado.getEstado())) {
            return new ResultadoValidacion(false, "El empleado no está activo en el sistema");
        }
        
        // Validar conflictos con asignaciones existentes
        List<AsignacionTurno> conflictos = verificarConflictos(empleadoDpi, fechaInicio, fechaFin);
        if (!conflictos.isEmpty()) {
            return new ResultadoValidacion(false, 
                "El empleado ya tiene turnos asignados en ese período. " +
                "Debe modificar o cancelar las asignaciones existentes primero.");
        }
        
        return new ResultadoValidacion(true, "La asignación puede ser creada");
    }
    
    /**
     * Verifica conflictos de horarios para un empleado
     */
    private List<AsignacionTurno> verificarConflictos(String empleadoDpi, LocalDate fechaInicio, LocalDate fechaFin) {
        return asignaciones.stream()
            .filter(a -> a.isActivo())
            .filter(a -> a.getEmpleadoDpi().equals(empleadoDpi))
            .filter(a -> !(fechaFin.isBefore(a.getFechaInicio()) || fechaInicio.isAfter(a.getFechaFin())))
            .collect(Collectors.toList());
    }
    
    /**
     * Crea una nueva asignación de turno
     */
    public ResultadoAsignacion crearAsignacion(String empleadoDpi, LocalDate fechaInicio, 
                                             LocalDate fechaFin, AsignacionTurno.TipoTurno turno,
                                             String adminUser) {
        
        // Validar la asignación
        ResultadoValidacion validacion = validarAsignacion(empleadoDpi, fechaInicio, fechaFin, turno, adminUser);
        if (!validacion.isValido()) {
            return new ResultadoAsignacion(false, validacion.getMensaje(), null);
        }
        
        // Obtener información del empleado
        Empleado empleado = dbManager.obtenerEmpleadoPorDpi(empleadoDpi);
        
        // Crear la asignación
        AsignacionTurno asignacion = new AsignacionTurno(
            empleadoDpi, 
            empleado.getNombre(),
            fechaInicio,
            fechaFin,
            turno,
            adminUser
        );
        
        // Guardar en memoria y archivo
        asignaciones.add(asignacion);
        guardarAsignacion(asignacion);
        
        // Registrar en bitácora
        String detalles = String.format("Asignación de %s a %s del %s al %s",
            turno.getDescripcion(),
            empleado.getNombre(),
            fechaInicio.toString(),
            fechaFin.toString());
        
        bitacoraManager.registrarOperacion(adminUser, "ASIGNACION_TURNO",
            detalles, empleadoDpi);
        
        return new ResultadoAsignacion(true, "Asignación creada con éxito", asignacion);
    }
    
    /**
     * Obtiene las asignaciones de un empleado específico
     */
    public List<AsignacionTurno> getAsignacionesEmpleado(String empleadoDpi) {
        return asignaciones.stream()
            .filter(a -> a.getEmpleadoDpi().equals(empleadoDpi))
            .filter(AsignacionTurno::isActivo)
            .sorted((a1, a2) -> a1.getFechaInicio().compareTo(a2.getFechaInicio()))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las asignaciones vigentes para una fecha específica
     */
    public List<AsignacionTurno> getAsignacionesVigentes(LocalDate fecha) {
        return asignaciones.stream()
            .filter(a -> a.esVigenteEnFecha(fecha))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las asignaciones activas
     */
    public List<AsignacionTurno> getTodasAsignacionesActivas() {
        return asignaciones.stream()
            .filter(AsignacionTurno::isActivo)
            .sorted((a1, a2) -> a1.getFechaInicio().compareTo(a2.getFechaInicio()))
            .collect(Collectors.toList());
    }
    
    /**
     * Desactiva una asignación existente (para modificaciones)
     */
    public ResultadoAsignacion desactivarAsignacion(AsignacionTurno asignacion, String adminUser) {
        // Validar permisos
        Empleado admin = dbManager.obtenerEmpleadoPorUsername(adminUser);
        if (admin == null || !"AdminRRHH".equals(admin.getRole())) {
            return new ResultadoAsignacion(false, 
                "Solo los administradores pueden modificar asignaciones", null);
        }
        
        asignacion.setActivo(false);
        guardarTodasAsignaciones();
        
        // Registrar en bitácora
        String detalles = String.format("Desactivación de asignación: %s", 
            asignacion.toDisplayString());
        bitacoraManager.registrarOperacion(adminUser, "DESACTIVAR_ASIGNACION",
            detalles, asignacion.getEmpleadoDpi());
        
        return new ResultadoAsignacion(true, "Asignación desactivada exitosamente", asignacion);
    }
    
    /**
     * Obtiene el turno actual de un empleado
     */
    public AsignacionTurno getTurnoActual(String empleadoDpi) {
        LocalDate hoy = LocalDate.now();
        return asignaciones.stream()
            .filter(a -> a.getEmpleadoDpi().equals(empleadoDpi))
            .filter(a -> a.esVigenteEnFecha(hoy))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Guarda una asignación en el archivo
     */
    private void guardarAsignacion(AsignacionTurno asignacion) {
        try (FileWriter fw = new FileWriter(ASIGNACIONES_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(asignacion.toString());
        } catch (IOException e) {
            System.err.println("Error guardando asignación: " + e.getMessage());
        }
    }
    
    /**
     * Guarda todas las asignaciones (para actualizaciones)
     */
    private void guardarTodasAsignaciones() {
        try (FileWriter fw = new FileWriter(ASIGNACIONES_FILE);
             PrintWriter pw = new PrintWriter(fw)) {
            for (AsignacionTurno asignacion : asignaciones) {
                pw.println(asignacion.toString());
            }
        } catch (IOException e) {
            System.err.println("Error guardando asignaciones: " + e.getMessage());
        }
    }
    
    /**
     * Carga las asignaciones desde el archivo
     */
    private void cargarAsignaciones() {
        try (BufferedReader br = new BufferedReader(new FileReader(ASIGNACIONES_FILE))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                AsignacionTurno asignacion = AsignacionTurno.fromString(linea);
                if (asignacion != null) {
                    asignaciones.add(asignacion);
                }
            }
        } catch (FileNotFoundException e) {
            // El archivo no existe, se creará cuando sea necesario
        } catch (IOException e) {
            System.err.println("Error cargando asignaciones: " + e.getMessage());
        }
    }
    
    /**
     * Clase para resultado de validación
     */
    public static class ResultadoValidacion {
        private boolean valido;
        private String mensaje;
        
        public ResultadoValidacion(boolean valido, String mensaje) {
            this.valido = valido;
            this.mensaje = mensaje;
        }
        
        public boolean isValido() { return valido; }
        public String getMensaje() { return mensaje; }
    }
    
    /**
     * Clase para resultado de asignación
     */
    public static class ResultadoAsignacion {
        private boolean exito;
        private String mensaje;
        private AsignacionTurno asignacion;
        
        public ResultadoAsignacion(boolean exito, String mensaje, AsignacionTurno asignacion) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.asignacion = asignacion;
        }
        
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public AsignacionTurno getAsignacion() { return asignacion; }
    }
}