package archivo;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestor para manejar las operaciones de solicitudes
 */
public class SolicitudManager {
    
    private static final String ARCHIVO_SOLICITUDES = "solicitudes.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Guarda una solicitud en el archivo
     */
    public void guardarSolicitud(Solicitud solicitud) throws IOException {
        try (FileWriter fw = new FileWriter(ARCHIVO_SOLICITUDES, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            String linea = formatearSolicitudParaArchivo(solicitud);
            pw.println(linea);
        }
    }
    
    /**
     * Obtiene todas las solicitudes de un empleado específico
     */
    public List<Solicitud> obtenerSolicitudesPorEmpleado(String empleadoDpi) {
        List<Solicitud> solicitudesEmpleado = new ArrayList<>();
        List<Solicitud> todasLasSolicitudes = cargarTodasLasSolicitudes();
        
        for (Solicitud solicitud : todasLasSolicitudes) {
            if (empleadoDpi.equals(solicitud.getEmpleadoDpi())) {
                solicitudesEmpleado.add(solicitud);
            }
        }
        
        return solicitudesEmpleado;
    }
    
    /**
     * Obtiene todas las solicitudes del sistema
     */
    public List<Solicitud> cargarTodasLasSolicitudes() {
        List<Solicitud> solicitudes = new ArrayList<>();
        
        File archivo = new File(ARCHIVO_SOLICITUDES);
        if (!archivo.exists()) {
            return solicitudes; // Retorna lista vacía si el archivo no existe
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    try {
                        Solicitud solicitud = parsearSolicitudDesdeArchivo(linea);
                        if (solicitud != null) {
                            solicitudes.add(solicitud);
                        }
                    } catch (Exception e) {
                        System.err.println("Error al parsear línea: " + linea + " - " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer archivo de solicitudes: " + e.getMessage());
        }
        
        return solicitudes;
    }
    
    /**
     * Actualiza el estado de una solicitud
     */
    public void actualizarEstadoSolicitud(int solicitudId, Solicitud.EstadoSolicitud nuevoEstado, 
                                        String procesadoPor, String motivoRechazo) throws IOException {
        List<Solicitud> solicitudes = cargarTodasLasSolicitudes();
        boolean encontrada = false;
        
        for (Solicitud solicitud : solicitudes) {
            if (solicitud.getId() == solicitudId) {
                if (nuevoEstado == Solicitud.EstadoSolicitud.APROBADA) {
                    solicitud.aprobar(procesadoPor);
                } else if (nuevoEstado == Solicitud.EstadoSolicitud.RECHAZADA) {
                    solicitud.rechazar(procesadoPor, motivoRechazo);
                }
                encontrada = true;
                break;
            }
        }
        
        if (encontrada) {
            reescribirArchivoSolicitudes(solicitudes);
        } else {
            throw new IllegalArgumentException("Solicitud con ID " + solicitudId + " no encontrada");
        }
    }
    
    /**
     * Formatea una solicitud para guardarla en archivo
     */
    private String formatearSolicitudParaArchivo(Solicitud solicitud) {
        StringBuilder sb = new StringBuilder();
        sb.append(solicitud.getId()).append("|");
        sb.append(solicitud.getEmpleadoDpi()).append("|");
        sb.append(solicitud.getEmpleadoNombre()).append("|");
        sb.append(solicitud.getTipo().name()).append("|");
        sb.append(solicitud.getDescripcion().replace("|", "~")).append("|"); // Reemplazar | para evitar conflictos
        sb.append(solicitud.getFechaInicio().format(FORMATTER)).append("|");
        sb.append(solicitud.getFechaFin().format(FORMATTER)).append("|");
        sb.append(solicitud.getFechaSolicitud().format(FORMATTER)).append("|");
        sb.append(solicitud.getEstado().name()).append("|");
        sb.append(solicitud.getAprobadoPor() != null ? solicitud.getAprobadoPor() : "").append("|");
        sb.append(solicitud.getMotivoRechazo() != null ? solicitud.getMotivoRechazo().replace("|", "~") : "").append("|");
        sb.append(solicitud.getFechaProcesamiento() != null ? solicitud.getFechaProcesamiento().format(FORMATTER) : "");
        
        return sb.toString();
    }
    
    /**
     * Parsea una línea del archivo para crear una solicitud
     */
    private Solicitud parsearSolicitudDesdeArchivo(String linea) {
        String[] partes = linea.split("\\|", -1); // -1 para incluir campos vacíos al final
        
        if (partes.length < 9) {
            return null; // Línea inválida
        }
        
        try {
            // Crear solicitud básica
            int id = Integer.parseInt(partes[0]);
            String empleadoDpi = partes[1];
            String empleadoNombre = partes[2];
            Solicitud.TipoSolicitud tipo = Solicitud.TipoSolicitud.valueOf(partes[3]);
            String descripcion = partes[4].replace("~", "|"); // Restaurar | en descripción
            LocalDateTime fechaInicio = LocalDateTime.parse(partes[5], FORMATTER);
            LocalDateTime fechaFin = LocalDateTime.parse(partes[6], FORMATTER);
            
            Solicitud solicitud = new Solicitud(empleadoDpi, empleadoNombre, tipo, descripcion, fechaInicio, fechaFin);
            solicitud.setId(id);
            
            // Restaurar fecha de solicitud
            if (!partes[7].isEmpty()) {
                solicitud.setFechaSolicitud(LocalDateTime.parse(partes[7], FORMATTER));
            }
            
            // Restaurar estado
            if (!partes[8].isEmpty()) {
                solicitud.setEstado(Solicitud.EstadoSolicitud.valueOf(partes[8]));
            }
            
            // Restaurar campos opcionales si existen
            if (partes.length > 9 && !partes[9].isEmpty()) {
                solicitud.setAprobadoPor(partes[9]);
            }
            
            if (partes.length > 10 && !partes[10].isEmpty()) {
                solicitud.setMotivoRechazo(partes[10].replace("~", "|"));
            }
            
            if (partes.length > 11 && !partes[11].isEmpty()) {
                solicitud.setFechaProcesamiento(LocalDateTime.parse(partes[11], FORMATTER));
            }
            
            return solicitud;
            
        } catch (Exception e) {
            System.err.println("Error al parsear solicitud: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Reescribe completamente el archivo de solicitudes
     */
    private void reescribirArchivoSolicitudes(List<Solicitud> solicitudes) throws IOException {
        try (FileWriter fw = new FileWriter(ARCHIVO_SOLICITUDES, false);
             PrintWriter pw = new PrintWriter(fw)) {
            
            for (Solicitud solicitud : solicitudes) {
                String linea = formatearSolicitudParaArchivo(solicitud);
                pw.println(linea);
            }
        }
    }
    
    /**
     * Obtiene solicitudes pendientes (para administradores)
     */
    public List<Solicitud> obtenerSolicitudesPendientes() {
        List<Solicitud> solicitudesPendientes = new ArrayList<>();
        List<Solicitud> todasLasSolicitudes = cargarTodasLasSolicitudes();
        
        for (Solicitud solicitud : todasLasSolicitudes) {
            if (solicitud.getEstado() == Solicitud.EstadoSolicitud.PENDIENTE) {
                solicitudesPendientes.add(solicitud);
            }
        }
        
        return solicitudesPendientes;
    }
}
