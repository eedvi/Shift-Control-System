package archivo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Bitacora {
    private String usuario;
    private LocalDateTime fechaHora;
    private String tipoOperacion;
    private String detalles;
    private String empleadoAfectado;

    public Bitacora(String usuario, String tipoOperacion, String detalles, String empleadoAfectado) {
        this.usuario = usuario;
        this.fechaHora = LocalDateTime.now();
        this.tipoOperacion = tipoOperacion;
        this.detalles = detalles;
        this.empleadoAfectado = empleadoAfectado;
    }

    // Getters
    public String getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public String getDetalles() {
        return detalles;
    }

    public String getEmpleadoAfectado() {
        return empleadoAfectado;
    }

    // Método para formatear la fecha
    public String getFechaHoraFormateada() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return fechaHora.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("[%s] Usuario: %s | Operación: %s | Empleado: %s | Detalles: %s",
                getFechaHoraFormateada(), usuario, tipoOperacion, empleadoAfectado, detalles);
    }
}
