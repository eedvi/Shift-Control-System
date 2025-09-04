package archivo;
import java.io.*;
import javax.swing.JOptionPane;

public class Archivo {
    
    File archivo;
    
    public void crearArchivo() {
        try {
            archivo = new File("Empleadosguardados.txt");
            if (archivo.createNewFile()) {
                JOptionPane.showMessageDialog(null,"Empleado Guardado ");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public void escribirEnArchivo(Empleado empleado){
        try{
            FileWriter escritura = new FileWriter(archivo, true);
            escritura.write(empleado.getDpi() + "%" + empleado.getNombre() + "%" +  empleado.getArea() + "%" + empleado.getTurno() + "%" + empleado.getEstado() + "\r\n");
            escritura.close();
        }catch(IOException e){
            System.out.println(e);
        }
    }

}