package archivo;

import java.time.LocalDateTime;

public class Empleado {
    
    private String dpi;
    private String nombre;
    private String username;
    private String area;
    private String turno;
    private String estado;
    private String email;
    private String password;
    private String role;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String motivoInactividad;

    // Constructor vacío para compatibilidad
    public Empleado() {
        this.role = "Empleado";
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }

    // Constructor completo
    public Empleado(String dpi, String nombre, String username, String area, String turno,
                   String estado, String email, String password) {
        this();
        this.dpi = dpi;
        this.nombre = nombre;
        this.username = username;
        this.area = area;
        this.turno = turno;
        this.estado = estado;
        this.email = email;
        this.password = password;
    }

    // Constructor básico para compatibilidad
    public Empleado(String dpi, String nombre, String area, String turno, String estado) {
        this();
        this.dpi = dpi;
        this.nombre = nombre;
        this.area = area;
        this.turno = turno;
        this.estado = estado;
    }

    // Getters y Setters
    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.fechaModificacion = LocalDateTime.now();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        this.fechaModificacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public String getMotivoInactividad() {
        return motivoInactividad;
    }

    public void setMotivoInactividad(String motivoInactividad) {
        this.motivoInactividad = motivoInactividad;
        this.fechaModificacion = LocalDateTime.now();
    }

    // Métodos de validación
    public boolean isValid() {
        return dpi != null && !dpi.trim().isEmpty() &&
               nombre != null && !nombre.trim().isEmpty() &&
               username != null && !username.trim().isEmpty() &&
               area != null && !area.trim().isEmpty() &&
               turno != null && !turno.trim().isEmpty();
    }

    public boolean isActive() {
        return "Activo".equals(estado);
    }

    // Método para cambiar estado con motivo
    public void cambiarEstado(String nuevoEstado, String motivo) {
        this.estado = nuevoEstado;
        if ("Inactivo".equals(nuevoEstado)) {
            this.motivoInactividad = motivo;
        }
        this.fechaModificacion = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Empleado{" +
                "dpi='" + dpi + '\'' +
                ", nombre='" + nombre + '\'' +
                ", username='" + username + '\'' +
                ", area='" + area + '\'' +
                ", turno='" + turno + '\'' +
                ", estado='" + estado + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
