package archivo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase que representa un marcaje en el sistema
 * Incluye entrada, descansos y salida
 */
public class Marcaje {
    
    public enum TipoMarcaje {
        ENTRADA, PRIMER_DESCANSO, SEGUNDO_DESCANSO, SALIDA
    }
    
    private String empleadoDpi;
    private TipoMarcaje tipo;
    private LocalDateTime fechaHora;
    private boolean esTarde; // Para validar si es una entrada tarde
    private String observaciones;
    
    public Marcaje(String empleadoDpi, TipoMarcaje tipo) {
        this.empleadoDpi = empleadoDpi;
        this.tipo = tipo;
        this.fechaHora = LocalDateTime.now();
        this.esTarde = false;
        this.observaciones = "";
        
        // Verificar si es entrada tarde (después de las 8:00 AM)
        if (tipo == TipoMarcaje.ENTRADA) {
            int hora = fechaHora.getHour();
            int minuto = fechaHora.getMinute();
            this.esTarde = (hora > 8) || (hora == 8 && minuto > 0);
        }
    }
    
    public Marcaje(String empleadoDpi, TipoMarcaje tipo, LocalDateTime fechaHora) {
        this.empleadoDpi = empleadoDpi;
        this.tipo = tipo;
        this.fechaHora = fechaHora;
        this.observaciones = "";
        
        // Verificar si es entrada tarde para marcajes con fecha específica
        if (tipo == TipoMarcaje.ENTRADA) {
            int hora = fechaHora.getHour();
            int minuto = fechaHora.getMinute();
            this.esTarde = (hora > 8) || (hora == 8 && minuto > 0);
        }
    }
    
    // Getters y Setters
    public String getEmpleadoDpi() {
        return empleadoDpi;
    }
    
    public void setEmpleadoDpi(String empleadoDpi) {
        this.empleadoDpi = empleadoDpi;
    }
    
    public TipoMarcaje getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoMarcaje tipo) {
        this.tipo = tipo;
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    
    public boolean isEsTarde() {
        return esTarde;
    }
    
    public void setEsTarde(boolean esTarde) {
        this.esTarde = esTarde;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    /**
     * Convierte el marcaje a string para guardado en archivo
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%s|%s|%s|%s|%s", 
            empleadoDpi, 
            tipo.toString(), 
            fechaHora.format(formatter),
            esTarde ? "TARDE" : "PUNTUAL",
            observaciones.isEmpty() ? "N/A" : observaciones
        );
    }
    
    /**
     * Crea un marcaje desde una línea de texto
     */
    public static Marcaje fromString(String linea) {
        String[] partes = linea.split("\\|");
        if (partes.length >= 4) {
            String dpi = partes[0];
            TipoMarcaje tipo = TipoMarcaje.valueOf(partes[1]);
            LocalDateTime fechaHora = LocalDateTime.parse(partes[2], 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            Marcaje marcaje = new Marcaje(dpi, tipo, fechaHora);
            marcaje.setEsTarde("TARDE".equals(partes[3]));
            
            if (partes.length > 4 && !"N/A".equals(partes[4])) {
                marcaje.setObservaciones(partes[4]);
            }
            
            return marcaje;
        }
        return null;
    }
    
    /**
     * Obtiene descripción legible del tipo de marcaje
     */
    public String getTipoDescripcion() {
        switch (tipo) {
            case ENTRADA:
                return "Entrada" + (esTarde ? " (Tarde)" : "");
            case PRIMER_DESCANSO:
                return "Primer Descanso";
            case SEGUNDO_DESCANSO:
                return "Segundo Descanso";
            case SALIDA:
                return "Salida";
            default:
                return tipo.toString();
        }
    }
}