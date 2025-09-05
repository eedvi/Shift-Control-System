package archivo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager for audit logging (Bitácora) - handles all system operation tracking
 */
public class BitacoraManager {
    private List<Bitacora> registros;
    private static final String BITACORA_FILE = "bitacora.txt";

    public BitacoraManager() {
        registros = new ArrayList<>();
        cargarBitacora();
    }

    public void registrarOperacion(String usuario, String tipoOperacion, String detalles, String empleadoAfectado) {
        Bitacora registro = new Bitacora(usuario, tipoOperacion, detalles, empleadoAfectado);
        registros.add(registro);
        guardarRegistro(registro);
    }

    private void guardarRegistro(Bitacora registro) {
        try (FileWriter fw = new FileWriter(BITACORA_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(registro.toString());
        } catch (IOException e) {
            System.err.println("Error guardando en bitácora: " + e.getMessage());
        }
    }

    private void cargarBitacora() {
        try (BufferedReader br = new BufferedReader(new FileReader(BITACORA_FILE))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Aquí podrías parsear las líneas existentes si necesitas cargar el historial
                System.out.println("Registro cargado: " + linea);
            }
        } catch (FileNotFoundException e) {
            // El archivo no existe, se creará cuando sea necesario
        } catch (IOException e) {
            System.err.println("Error cargando bitácora: " + e.getMessage());
        }
    }

    public List<Bitacora> obtenerRegistros() {
        return new ArrayList<>(registros);
    }

    public List<Bitacora> filtrarPorUsuario(String usuario) {
        return registros.stream()
                .filter(r -> r.getUsuario().equals(usuario))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<Bitacora> filtrarPorTipoOperacion(String tipoOperacion) {
        return registros.stream()
                .filter(r -> r.getTipoOperacion().equals(tipoOperacion))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
