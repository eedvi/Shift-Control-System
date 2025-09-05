package archivo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Manager for handling employee data operations
 */
public class DatabaseManager {
    private static final String EMPLEADOS_FILE = "Empleadosguardados.txt";
    private static final String SOLICITUDES_FILE = "solicitudes.txt";
    private List<Empleado> empleados;
    private List<Solicitud> solicitudes;

    public DatabaseManager() {
        empleados = new ArrayList<>();
        solicitudes = new ArrayList<>();
        cargarEmpleados();
        cargarSolicitudes();
    }

    /**
     * Load all employees from file
     */
    private void cargarEmpleados() {
        try (BufferedReader br = new BufferedReader(new FileReader(EMPLEADOS_FILE))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                Empleado emp = parsearEmpleado(linea);
                if (emp != null) {
                    empleados.add(emp);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist, will be created when saving
            System.out.println("Archivo de empleados no encontrado, se creará uno nuevo");
        } catch (IOException e) {
            System.err.println("Error cargando empleados: " + e.getMessage());
        }
    }

    /**
     * Load requests from file
     */
    private void cargarSolicitudes() {
        try (BufferedReader br = new BufferedReader(new FileReader(SOLICITUDES_FILE))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // For now, just initialize empty list - file format would need to be defined
                // In a real implementation, you'd parse the solicitudes from file
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist, will be created when saving
            System.out.println("Archivo de solicitudes no encontrado, se creará uno nuevo");
        } catch (IOException e) {
            System.err.println("Error cargando solicitudes: " + e.getMessage());
        }
    }

    /**
     * Parse employee data from file line
     */
    private Empleado parsearEmpleado(String linea) {
        try {
            // Assuming format: username|password|nombre|dpi|area|turno|estado|email
            String[] datos = linea.split("\\|");
            if (datos.length >= 7) {
                Empleado emp = new Empleado();
                emp.setUsername(datos[0]);
                emp.setPassword(datos[1]);
                emp.setNombre(datos[2]);
                emp.setDpi(datos[3]);
                emp.setArea(datos[4]);
                emp.setTurno(datos[5]);
                emp.setEstado(datos[6]);
                if (datos.length > 7) {
                    emp.setEmail(datos[7]);
                }
                if (datos.length > 8) {
                    emp.setRole(datos[8]);
                } else {
                    emp.setRole("Empleado"); // Default role
                }
                return emp;
            }
        } catch (Exception e) {
            System.err.println("Error parseando empleado: " + linea + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all employees
     */
    public List<Empleado> obtenerTodosEmpleados() {
        return new ArrayList<>(empleados);
    }

    /**
     * Search employees by criteria
     */
    public List<Empleado> buscarEmpleados(String criterio) {
        List<Empleado> resultado = new ArrayList<>();
        String criterioLower = criterio.toLowerCase();

        for (Empleado emp : empleados) {
            if (emp.getUsername().toLowerCase().contains(criterioLower) ||
                emp.getNombre().toLowerCase().contains(criterioLower) ||
                emp.getDpi().contains(criterio) ||
                emp.getArea().toLowerCase().contains(criterioLower)) {
                resultado.add(emp);
            }
        }
        return resultado;
    }

    /**
     * Get employee by username
     */
    public Empleado obtenerEmpleadoPorUsername(String username) {
        for (Empleado emp : empleados) {
            if (emp.getUsername().equals(username)) {
                return emp;
            }
        }
        return null;
    }

    /**
     * Deactivate employee
     */
    public boolean desactivarEmpleado(String username, String motivo) {
        for (Empleado emp : empleados) {
            if (emp.getUsername().equals(username)) {
                emp.setEstado("Inactivo");
                guardarEmpleados();
                return true;
            }
        }
        return false;
    }

    /**
     * Save all employees to file
     */
    private void guardarEmpleados() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EMPLEADOS_FILE))) {
            for (Empleado emp : empleados) {
                pw.println(emp.getUsername() + "|" + emp.getPassword() + "|" +
                          emp.getNombre() + "|" + emp.getDpi() + "|" +
                          emp.getArea() + "|" + emp.getTurno() + "|" +
                          emp.getEstado() + "|" +
                          (emp.getEmail() != null ? emp.getEmail() : "") + "|" +
                          (emp.getRole() != null ? emp.getRole() : "Empleado"));
            }
        } catch (IOException e) {
            System.err.println("Error guardando empleados: " + e.getMessage());
        }
    }

    /**
     * Add new employee
     */
    public boolean agregarEmpleado(Empleado empleado) {
        // Check if username already exists
        if (obtenerEmpleadoPorUsername(empleado.getUsername()) != null) {
            return false;
        }
        empleados.add(empleado);
        guardarEmpleados();
        return true;
    }

    /**
     * Update employee
     */
    public boolean actualizarEmpleado(Empleado empleadoActualizado) {
        for (int i = 0; i < empleados.size(); i++) {
            if (empleados.get(i).getUsername().equals(empleadoActualizado.getUsername())) {
                empleados.set(i, empleadoActualizado);
                guardarEmpleados();
                return true;
            }
        }
        return false;
    }

    /**
     * Get employee by DPI
     */
    public Empleado obtenerEmpleadoPorDpi(String dpi) {
        for (Empleado emp : empleados) {
            if (emp.getDpi().equals(dpi)) {
                return emp;
            }
        }
        return null;
    }

    /**
     * Authenticate user with username and password
     */
    public Empleado autenticarUsuario(String username, String password) {
        for (Empleado emp : empleados) {
            if (emp.getUsername().equals(username) && emp.getPassword().equals(password)) {
                return emp;
            }
        }
        return null;
    }

    /**
     * Check if username exists
     */
    public boolean existeUsername(String username) {
        return obtenerEmpleadoPorUsername(username) != null;
    }

    /**
     * Check if DPI exists
     */
    public boolean existeDpi(String dpi) {
        return obtenerEmpleadoPorDpi(dpi) != null;
    }

    /**
     * Register new employee
     */
    public boolean registrarEmpleado(Empleado empleado) {
        return agregarEmpleado(empleado);
    }

    /**
     * Assign role to user
     */
    public boolean asignarRol(String username, String rol) {
        Empleado emp = obtenerEmpleadoPorUsername(username);
        if (emp != null) {
            emp.setRole(rol);
            guardarEmpleados();
            return true;
        }
        return false;
    }

    /**
     * Remove role from user (set to default "Empleado")
     */
    public boolean removerRol(String username) {
        Empleado emp = obtenerEmpleadoPorUsername(username);
        if (emp != null) {
            emp.setRole("Empleado");
            guardarEmpleados();
            return true;
        }
        return false;
    }

    /**
     * Get all pending requests
     */
    public List<Solicitud> obtenerSolicitudesPendientes() {
        List<Solicitud> pendientes = new ArrayList<>();
        for (Solicitud sol : solicitudes) {
            if (sol.isPendiente()) {
                pendientes.add(sol);
            }
        }
        return pendientes;
    }

    /**
     * Get request by ID
     */
    public Solicitud obtenerSolicitudPorId(int id) {
        for (Solicitud sol : solicitudes) {
            if (sol.getId() == id) {
                return sol;
            }
        }
        return null;
    }

    /**
     * Update request
     */
    public boolean actualizarSolicitud(Solicitud solicitud) {
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).getId() == solicitud.getId()) {
                solicitudes.set(i, solicitud);
                guardarSolicitudes();
                return true;
            }
        }
        return false;
    }

    /**
     * Create new request
     */
    public boolean crearSolicitud(Solicitud solicitud) {
        solicitudes.add(solicitud);
        guardarSolicitudes();
        return true;
    }

    /**
     * Save all requests to file
     */
    private void guardarSolicitudes() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SOLICITUDES_FILE))) {
            for (Solicitud sol : solicitudes) {
                // Simple format for demonstration - in a real app you'd use JSON or a proper format
                pw.println(sol.getId() + "|" + sol.getEmpleadoDpi() + "|" +
                          sol.getEmpleadoNombre() + "|" + sol.getTipo().name() + "|" +
                          sol.getDescripcion() + "|" + sol.getEstado().name());
            }
        } catch (IOException e) {
            System.err.println("Error guardando solicitudes: " + e.getMessage());
        }
    }
}
