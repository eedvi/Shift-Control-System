package archivo;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager para el sistema de marcajes
 * Maneja entrada, descansos, salida y todas las validaciones de negocio
 */
public class MarcajeManager {
    
    private List<Marcaje> marcajes;
    private static final String MARCAJES_FILE = "marcajes.txt";
    private BitacoraManager bitacoraManager;
    
    public MarcajeManager() {
        this.marcajes = new ArrayList<>();
        this.bitacoraManager = new BitacoraManager();
        cargarMarcajes();
    }
    
    /**
     * Obtiene los marcajes del día actual para un empleado
     */
    public List<Marcaje> getMarcajesDelDia(String empleadoDpi) {
        LocalDate hoy = LocalDate.now();
        return marcajes.stream()
            .filter(m -> m.getEmpleadoDpi().equals(empleadoDpi))
            .filter(m -> m.getFechaHora().toLocalDate().equals(hoy))
            .collect(Collectors.toList());
    }
    
    /**
     * Valida si se puede marcar entrada
     */
    public ResultadoValidacion validarMarcajeEntrada(String empleadoDpi) {
        List<Marcaje> marcajesHoy = getMarcajesDelDia(empleadoDpi);
        
        // Verificar si ya marcó entrada
        boolean yaMarcoEntrada = marcajesHoy.stream()
            .anyMatch(m -> m.getTipo() == Marcaje.TipoMarcaje.ENTRADA);
        
        if (yaMarcoEntrada) {
            return new ResultadoValidacion(false, "Ya ha marcado la entrada hoy");
        }
        
        return new ResultadoValidacion(true, "Puede marcar entrada");
    }
    
    /**
     * Valida si se puede marcar primer descanso
     */
    public ResultadoValidacion validarPrimerDescanso(String empleadoDpi) {
        List<Marcaje> marcajesHoy = getMarcajesDelDia(empleadoDpi);
        
        // Verificar si ya marcó entrada
        boolean yaMarcoEntrada = marcajesHoy.stream()
            .anyMatch(m -> m.getTipo() == Marcaje.TipoMarcaje.ENTRADA);
        
        if (!yaMarcoEntrada) {
            return new ResultadoValidacion(false, 
                "Debe marcar la entrada antes de registrar el descanso.");
        }
        
        // Verificar si ya marcó primer descanso
        boolean yaMarcoDescanso1 = marcajesHoy.stream()
            .anyMatch(m -> m.getTipo() == Marcaje.TipoMarcaje.PRIMER_DESCANSO);
        
        if (yaMarcoDescanso1) {
            return new ResultadoValidacion(false, "Ya ha marcado el primer descanso");
        }
        
        return new ResultadoValidacion(true, "Puede marcar primer descanso");
    }
    
    /**
     * Valida si se puede marcar segundo descanso
     */
    public ResultadoValidacion validarSegundoDescanso(String empleadoDpi) {
        List<Marcaje> marcajesHoy = getMarcajesDelDia(empleadoDpi);
        
        // Verificar si ya marcó primer descanso
        boolean yaMarcoDescanso1 = marcajesHoy.stream()
            .anyMatch(m -> m.getTipo() == Marcaje.TipoMarcaje.PRIMER_DESCANSO);
        
        if (!yaMarcoDescanso1) {
            return new ResultadoValidacion(false, 
                "Debe marcar el primer descanso antes de registrar el segundo descanso.");
        }
        
        // Verificar si ya marcó segundo descanso
        boolean yaMarcoDescanso2 = marcajesHoy.stream()
            .anyMatch(m -> m.getTipo() == Marcaje.TipoMarcaje.SEGUNDO_DESCANSO);
        
        if (yaMarcoDescanso2) {
            return new ResultadoValidacion(false, "Ya ha marcado el segundo descanso");
        }
        
        return new ResultadoValidacion(true, "Puede marcar segundo descanso");
    }
    
    /**
     * Valida si se puede marcar salida
     */
    public ResultadoValidacion validarMarcajeSalida(String empleadoDpi) {
        List<Marcaje> marcajesHoy = getMarcajesDelDia(empleadoDpi);
        
        // Verificar primer descanso
        boolean yaMarcoDescanso1 = marcajesHoy.stream()
            .anyMatch(m -> m.getTipo() == Marcaje.TipoMarcaje.PRIMER_DESCANSO);
        
        if (!yaMarcoDescanso1) {
            return new ResultadoValidacion(false, 
                "Debe marcar el primer descanso antes de registrar la salida.");
        }
        
        // Verificar segundo descanso
        boolean yaMarcoDescanso2 = marcajesHoy.stream()
            .anyMatch(m -> m.getTipo() == Marcaje.TipoMarcaje.SEGUNDO_DESCANSO);
        
        if (!yaMarcoDescanso2) {
            return new ResultadoValidacion(false, 
                "Debe marcar el segundo descanso antes de registrar la salida.");
        }
        
        // Verificar si ya marcó salida
        boolean yaMarcoSalida = marcajesHoy.stream()
            .anyMatch(m -> m.getTipo() == Marcaje.TipoMarcaje.SALIDA);
        
        if (yaMarcoSalida) {
            return new ResultadoValidacion(false, "Ya ha marcado la salida hoy");
        }
        
        return new ResultadoValidacion(true, "Puede marcar salida");
    }
    
    /**
     * Realiza un marcaje después de validar
     */
    public ResultadoMarcaje marcarEntrada(Empleado empleado) {
        ResultadoValidacion validacion = validarMarcajeEntrada(empleado.getDpi());
        
        if (!validacion.isValido()) {
            return new ResultadoMarcaje(false, validacion.getMensaje(), null);
        }
        
        Marcaje marcaje = new Marcaje(empleado.getDpi(), Marcaje.TipoMarcaje.ENTRADA);
        marcajes.add(marcaje);
        guardarMarcaje(marcaje);
        
        // Registrar en bitácora
        String detalles = String.format("Marcaje de entrada - %s%s", 
            marcaje.getFechaHora().toString(),
            marcaje.isEsTarde() ? " (ENTRADA TARDE)" : "");
        
        bitacoraManager.registrarOperacion(empleado.getUsername(), "MARCAJE_ENTRADA", 
            detalles, empleado.getDpi());
        
        String mensaje = marcaje.isEsTarde() ? 
            "Marcaje de entrada realizado con éxito (Entrada tarde registrada)" :
            "Marcaje realizado con éxito";
            
        return new ResultadoMarcaje(true, mensaje, marcaje);
    }
    
    /**
     * Marca primer descanso
     */
    public ResultadoMarcaje marcarPrimerDescanso(Empleado empleado) {
        ResultadoValidacion validacion = validarPrimerDescanso(empleado.getDpi());
        
        if (!validacion.isValido()) {
            return new ResultadoMarcaje(false, validacion.getMensaje(), null);
        }
        
        Marcaje marcaje = new Marcaje(empleado.getDpi(), Marcaje.TipoMarcaje.PRIMER_DESCANSO);
        marcajes.add(marcaje);
        guardarMarcaje(marcaje);
        
        // Registrar en bitácora
        bitacoraManager.registrarOperacion(empleado.getUsername(), "MARCAJE_DESCANSO1", 
            "Marcaje de primer descanso - " + marcaje.getFechaHora().toString(), 
            empleado.getDpi());
        
        return new ResultadoMarcaje(true, "Marcaje realizado con éxito", marcaje);
    }
    
    /**
     * Marca segundo descanso
     */
    public ResultadoMarcaje marcarSegundoDescanso(Empleado empleado) {
        ResultadoValidacion validacion = validarSegundoDescanso(empleado.getDpi());
        
        if (!validacion.isValido()) {
            return new ResultadoMarcaje(false, validacion.getMensaje(), null);
        }
        
        Marcaje marcaje = new Marcaje(empleado.getDpi(), Marcaje.TipoMarcaje.SEGUNDO_DESCANSO);
        marcajes.add(marcaje);
        guardarMarcaje(marcaje);
        
        // Registrar en bitácora
        bitacoraManager.registrarOperacion(empleado.getUsername(), "MARCAJE_DESCANSO2", 
            "Marcaje de segundo descanso - " + marcaje.getFechaHora().toString(), 
            empleado.getDpi());
        
        return new ResultadoMarcaje(true, "Marcaje realizado con éxito", marcaje);
    }
    
    /**
     * Marca salida
     */
    public ResultadoMarcaje marcarSalida(Empleado empleado) {
        ResultadoValidacion validacion = validarMarcajeSalida(empleado.getDpi());
        
        if (!validacion.isValido()) {
            return new ResultadoMarcaje(false, validacion.getMensaje(), null);
        }
        
        Marcaje marcaje = new Marcaje(empleado.getDpi(), Marcaje.TipoMarcaje.SALIDA);
        marcajes.add(marcaje);
        guardarMarcaje(marcaje);
        
        // Registrar en bitácora
        bitacoraManager.registrarOperacion(empleado.getUsername(), "MARCAJE_SALIDA", 
            "Marcaje de salida - " + marcaje.getFechaHora().toString(), 
            empleado.getDpi());
        
        return new ResultadoMarcaje(true, "Marcaje realizado con éxito", marcaje);
    }
    
    /**
     * Guarda un marcaje en el archivo
     */
    private void guardarMarcaje(Marcaje marcaje) {
        try (FileWriter fw = new FileWriter(MARCAJES_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(marcaje.toString());
        } catch (IOException e) {
            System.err.println("Error guardando marcaje: " + e.getMessage());
        }
    }
    
    /**
     * Carga los marcajes desde el archivo
     */
    private void cargarMarcajes() {
        try (BufferedReader br = new BufferedReader(new FileReader(MARCAJES_FILE))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                Marcaje marcaje = Marcaje.fromString(linea);
                if (marcaje != null) {
                    marcajes.add(marcaje);
                }
            }
        } catch (FileNotFoundException e) {
            // El archivo no existe, se creará cuando sea necesario
        } catch (IOException e) {
            System.err.println("Error cargando marcajes: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene todos los marcajes de un empleado
     */
    public List<Marcaje> getMarcajesEmpleado(String empleadoDpi) {
        return marcajes.stream()
            .filter(m -> m.getEmpleadoDpi().equals(empleadoDpi))
            .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todos los marcajes de un empleado (alias para compatibilidad)
     */
    public List<Marcaje> getTodosMarcajes(String empleadoDpi) {
        return getMarcajesEmpleado(empleadoDpi);
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
     * Clase para resultado de marcaje
     */
    public static class ResultadoMarcaje {
        private boolean exito;
        private String mensaje;
        private Marcaje marcaje;
        
        public ResultadoMarcaje(boolean exito, String mensaje, Marcaje marcaje) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.marcaje = marcaje;
        }
        
        public boolean isExito() { return exito; }
        public String getMensaje() { return mensaje; }
        public Marcaje getMarcaje() { return marcaje; }
    }
}