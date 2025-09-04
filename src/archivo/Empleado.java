
package archivo;
public class Empleado {
    
    private String dpi;
     private String nombre;
     private String area;
     private String turno;
     private String estado;

    public Empleado(String dpi, String nombre, String area, String turno, String estado) {
        this.dpi = dpi;
        this.nombre = nombre;
        this.area = area;
        this.turno = turno;
        this.estado = estado;
    }

    public String getDpi() {
        return dpi;
    }

    public void setDpi(String dpi) {
        this.dpi = dpi;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    String setTurno() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
     
     
     
}
