# Sistema de Control de Turnos - Documentación Técnica Exhaustiva

## Tabla de Contenidos
1. [Arquitectura General del Sistema](#arquitectura-general)
2. [Análisis Detallado de Clases](#análisis-detallado-de-clases)
3. [Flujo de Datos y Procesos](#flujo-de-datos)
4. [Gestión de Archivos y Persistencia](#gestión-de-archivos)
5. [Sistema de Autenticación y Roles](#sistema-de-autenticación)
6. [Interfaz Gráfica y Componentes](#interfaz-gráfica)
7. [Sistema de Solicitudes de Vacaciones](#sistema-de-solicitudes)
8. [Configuración y Despliegue](#configuración)

---

## 1. Arquitectura General del Sistema {#arquitectura-general}

### 1.1 Patrón Arquitectónico
El sistema implementa un patrón **MVC (Modelo-Vista-Controlador)** simplificado:

- **Modelo**: Clases de datos (`Empleado.java`, `Solicitud.java`, `Bitacora.java`)
- **Vista**: Interfaces gráficas Swing (archivos `.java` que extienden `JFrame`)
- **Controlador**: Managers (`DatabaseManager.java`, `SolicitudManager.java`, `BitacoraManager.java`)

### 1.2 Estructura de Paquetes
```
archivo/
├── Modelos de Datos
│   ├── Empleado.java          # Entidad empleado
│   ├── Solicitud.java         # Entidad solicitud de vacaciones
│   └── Bitacora.java          # Entidad registro de auditoría
├── Gestores (Managers)
│   ├── DatabaseManager.java   # Gestor de datos de empleados
│   ├── SolicitudManager.java  # Gestor de solicitudes
│   ├── BitacoraManager.java   # Gestor de auditoría
│   └── EmailService.java      # Gestor de notificaciones
├── Interfaces de Usuario
│   ├── Login.java             # Pantalla de inicio de sesión
│   ├── Menu.java              # Menú principal (Admin)
│   ├── MenuEmpleado.java      # Menú empleado
│   ├── MantenimientoUsuario.java
│   ├── RegistrodeEmpleados.java
│   ├── EmpleadosRegistrados.java
│   ├── GestionRoles.java
│   ├── Solicitudes.java       # Gestión solicitudes (Admin)
│   ├── CrearSolicitudVacaciones.java
│   └── VerSolicitudesEmpleado.java
└── Utilidades
    ├── Archivo.java           # Operaciones de archivos
    └── EmailTest.java         # Pruebas de email
```

---

## 2. Análisis Detallado de Clases {#análisis-detallado-de-clases}

### 2.1 Clase `Empleado.java` - Modelo de Datos Principal

#### Líneas 1-8: Declaración del paquete e imports
```java
package archivo;

import java.time.LocalDateTime;

public class Empleado {
```
- **Línea 1**: Define el paquete `archivo` donde reside la clase
- **Línea 3**: Importa `LocalDateTime` para manejo de fechas y timestamps
- **Línea 5**: Declaración de la clase pública `Empleado`

#### Líneas 9-19: Atributos privados de la entidad
```java
private String dpi;                    // Documento de identidad único
private String nombre;                 // Nombre completo del empleado
private String username;               // Usuario para login
private String area;                   // Departamento/área de trabajo
private String turno;                  // Turno asignado (Matutino/Vespertino/Nocturno)
private String estado;                 // Estado (Activo/Inactivo)
private String email;                  // Correo electrónico
private String password;               // Contraseña encriptada
private String role;                   // Rol del usuario (Empleado/AdminRRHH/etc.)
private LocalDateTime fechaCreacion;   // Timestamp de creación del registro
private LocalDateTime fechaModificacion; // Timestamp de última modificación
private String motivoInactividad;      // Razón de inactivación (si aplica)
```

**Análisis detallado de cada atributo:**
- **`dpi`**: Clave primaria natural, usado para identificación única
- **`username`**: Usado en el proceso de autenticación
- **`role`**: Determina permisos y acceso a funcionalidades
- **`fechaCreacion/fechaModificacion`**: Implementan auditoría temporal automática

#### Líneas 20-28: Constructor vacío con inicialización predeterminada
```java
public Empleado() {
    this.role = "Empleado";                          // Rol por defecto
    this.fechaCreacion = LocalDateTime.now();        // Timestamp actual
    this.fechaModificacion = LocalDateTime.now();    // Timestamp actual
}
```
- **Línea 21**: Establece rol predeterminado para nuevos empleados
- **Líneas 22-23**: Inicializa timestamps automáticamente

#### Líneas 29-40: Constructor completo
```java
public Empleado(String dpi, String nombre, String username, String area, String turno,
               String estado, String email, String password) {
    this();                            // Llama al constructor vacío
    this.dpi = dpi;
    this.nombre = nombre;
    this.username = username;
    this.area = area;
    this.turno = turno;
    this.estado = estado;
    this.email = email;
    this.password = password;
}
```
- **Línea 31**: Delegación al constructor vacío para inicialización base
- **Líneas 32-40**: Asignación directa de parámetros a atributos

#### Líneas 50-65: Métodos getter con funcionalidad de timestamp automático
```java
public void setDpi(String dpi) {
    this.dpi = dpi;
    this.fechaModificacion = LocalDateTime.now();    // Actualiza timestamp
}
```
**Patrón implementado**: Cada setter actualiza automáticamente `fechaModificacion`, implementando auditoría de cambios transparente.

#### Líneas 150-165: Métodos de validación y lógica de negocio
```java
public boolean isValid() {
    return dpi != null && !dpi.trim().isEmpty() &&
           nombre != null && !nombre.trim().isEmpty() &&
           username != null && !username.trim().isEmpty() &&
           area != null && !area.trim().isEmpty() &&
           turno != null && !turno.trim().isEmpty();
}
```
**Análisis**: Implementa validación de campos obligatorios usando operador AND lógico en cadena.

```java
public boolean isActive() {
    return "Activo".equals(estado);
}
```
**Análisis**: Método de conveniencia que encapsula la lógica de estado activo.

```java
public void cambiarEstado(String nuevoEstado, String motivo) {
    this.estado = nuevoEstado;
    if ("Inactivo".equals(nuevoEstado)) {
        this.motivoInactividad = motivo;
    }
    this.fechaModificacion = LocalDateTime.now();
}
```
**Análisis línea por línea**:
- **Línea 1**: Cambia el estado del empleado
- **Líneas 2-4**: Si el nuevo estado es "Inactivo", registra el motivo
- **Línea 5**: Actualiza timestamp de modificación

### 2.2 Clase `Solicitud.java` - Sistema de Solicitudes de Vacaciones

#### Líneas 1-15: Enumeraciones internas
```java
public enum TipoSolicitud {
    VACACIONES("Vacaciones"),
    PERMISO_PERSONAL("Permiso Personal"),
    CITA_IGSS("Cita IGSS"),
    DIA_CUMPLEANOS("Día de Cumpleaños"),
    LICENCIA_MEDICA("Licencia Médica"),
    OTRO("Otro");

    private final String descripcion;

    TipoSolicitud(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
```

**Análisis detallado**:
- **Líneas 1-7**: Define tipos de solicitud disponibles con patrón enum
- **Línea 9**: Atributo inmutable para descripción legible
- **Líneas 11-13**: Constructor privado del enum
- **Líneas 15-17**: Getter para obtener descripción legible

```java
public enum EstadoSolicitud {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada");
    // ... similar implementation
}
```
**Análisis**: Implementa estados del flujo de trabajo de solicitudes.

#### Líneas 40-55: Atributos principales de la solicitud
```java
private static int contadorId = 1;           // Contador estático para IDs únicos

private int id;                              // ID único de la solicitud
private String empleadoDpi;                  // DPI del empleado solicitante
private String empleadoNombre;               // Nombre del empleado (desnormalizado)
private TipoSolicitud tipo;                  // Tipo de solicitud (enum)
private String descripcion;                  // Descripción detallada
private LocalDateTime fechaInicio;           // Fecha inicio del período solicitado
private LocalDateTime fechaFin;              // Fecha fin del período solicitado
private LocalDateTime fechaSolicitud;        // Fecha/hora de creación de solicitud
private EstadoSolicitud estado;              // Estado actual (enum)
private String aprobadoPor;                  // Usuario que procesó la solicitud
private String motivoRechazo;                // Motivo en caso de rechazo
private LocalDateTime fechaProcesamiento;    // Fecha/hora de procesamiento
```

**Análisis de diseño**:
- **Línea 1**: Contador estático implementa generación automática de IDs
- **Línea 4**: `empleadoDpi` actúa como clave foránea hacia `Empleado`
- **Línea 5**: `empleadoNombre` está desnormalizado para optimizar consultas
- **Líneas 8-9**: Definen el período de tiempo solicitado

#### Líneas 56-70: Constructor principal
```java
public Solicitud(String empleadoDpi, String empleadoNombre, TipoSolicitud tipo,
                String descripcion, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
    this.id = contadorId++;                          // Asigna ID único y incrementa contador
    this.empleadoDpi = empleadoDpi;
    this.empleadoNombre = empleadoNombre;
    this.tipo = tipo;
    this.descripcion = descripcion;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.fechaSolicitud = LocalDateTime.now();       // Timestamp automático
    this.estado = EstadoSolicitud.PENDIENTE;         // Estado inicial
}
```

**Análisis línea por línea**:
- **Línea 3**: Operador post-incremento garantiza ID único secuencial
- **Línea 10**: Establece timestamp de creación automáticamente
- **Línea 11**: Inicializa en estado PENDIENTE por defecto

#### Líneas 140-155: Métodos de procesamiento de solicitudes
```java
public void aprobar(String aprobadoPor) {
    this.estado = EstadoSolicitud.APROBADA;
    this.aprobadoPor = aprobadoPor;
    this.fechaProcesamiento = LocalDateTime.now();
}

public void rechazar(String rechazadoPor, String motivo) {
    this.estado = EstadoSolicitud.RECHAZADA;
    this.aprobadoPor = rechazadoPor; // Usuario que procesó la solicitud
    this.motivoRechazo = motivo;
    this.fechaProcesamiento = LocalDateTime.now();
}
```

**Análisis**:
- Ambos métodos implementan el patrón de transición de estado
- `fechaProcesamiento` se establece automáticamente
- En `rechazar()`, `aprobadoPor` almacena quien procesó (no necesariamente aprobó)

### 2.3 Clase `DatabaseManager.java` - Gestor de Persistencia

#### Líneas 1-15: Declaración y constantes
```java
package archivo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String EMPLEADOS_FILE = "Empleadosguardados.txt";
    private static final String SOLICITUDES_FILE = "solicitudes.txt";
    private List<Empleado> empleados;
    private List<Solicitud> solicitudes;
```

**Análisis**:
- **Líneas 8-9**: Constantes para nombres de archivos (principio DRY)
- **Líneas 10-11**: Listas en memoria que actúan como caché de datos

#### Líneas 16-22: Constructor con inicialización automática
```java
public DatabaseManager() {
    empleados = new ArrayList<>();
    solicitudes = new ArrayList<>();
    cargarEmpleados();
    cargarSolicitudes();
}
```

**Análisis**:
- **Líneas 17-18**: Inicializa estructuras de datos vacías
- **Líneas 19-20**: Carga automática de datos al instanciar

#### Líneas 25-45: Método de carga de empleados con manejo de errores
```java
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
```

**Análisis línea por línea**:
- **Línea 26**: Try-with-resources garantiza cierre automático del BufferedReader
- **Línea 27-31**: Loop de lectura línea por línea con parsing y validación
- **Línea 32-34**: Manejo específico para archivo inexistente (primera ejecución)
- **Línea 35-37**: Manejo genérico de errores de I/O

### 2.4 Clase `SolicitudManager.java` - Gestor Especializado de Solicitudes

#### Líneas 1-20: Configuración y constantes
```java
package archivo;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SolicitudManager {
    
    private static final String ARCHIVO_SOLICITUDES = "solicitudes.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
```

**Análisis**:
- **Línea 11**: Ruta del archivo de persistencia
- **Línea 12**: Patrón estándar para serialización de fechas (ISO 8601 modificado)

#### Líneas 25-35: Método de guardado con manejo de archivos
```java
public void guardarSolicitud(Solicitud solicitud) throws IOException {
    try (FileWriter fw = new FileWriter(ARCHIVO_SOLICITUDES, true);
         PrintWriter pw = new PrintWriter(fw)) {
        
        String linea = formatearSolicitudParaArchivo(solicitud);
        pw.println(linea);
    }
}
```

**Análisis línea por línea**:
- **Línea 26**: Método que puede lanzar IOException (diseño fail-fast)
- **Línea 27**: FileWriter en modo append (true) para agregar al final
- **Línea 28**: PrintWriter para escritura de líneas completas
- **Línea 30**: Delegación a método helper para formateo
- **Línea 31**: Escritura con salto de línea automático

#### Líneas 80-105: Método de actualización de estado
```java
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
```

**Análisis del algoritmo**:
- **Líneas 83-84**: Carga todos los registros en memoria
- **Líneas 86-95**: Búsqueda lineal con early break para eficiencia
- **Líneas 88-92**: Pattern matching para diferentes tipos de transición
- **Líneas 97-101**: Patrón de reescritura completa del archivo
- **Línea 100**: Lanza excepción si no encuentra la solicitud (fail-fast)

#### Líneas 110-130: Formateo para persistencia
```java
private String formatearSolicitudParaArchivo(Solicitud solicitud) {
    StringBuilder sb = new StringBuilder();
    sb.append(solicitud.getId()).append("|");
    sb.append(solicitud.getEmpleadoDpi()).append("|");
    sb.append(solicitud.getEmpleadoNombre()).append("|");
    sb.append(solicitud.getTipo().name()).append("|");
    sb.append(solicitud.getDescripcion().replace("|", "~")).append("|"); // Escape de delimitador
    sb.append(solicitud.getFechaInicio().format(FORMATTER)).append("|");
    sb.append(solicitud.getFechaFin().format(FORMATTER)).append("|");
    sb.append(solicitud.getFechaSolicitud().format(FORMATTER)).append("|");
    sb.append(solicitud.getEstado().name()).append("|");
    sb.append(solicitud.getAprobadoPor() != null ? solicitud.getAprobadoPor() : "").append("|");
    sb.append(solicitud.getMotivoRechazo() != null ? solicitud.getMotivoRechazo().replace("|", "~") : "").append("|");
    sb.append(solicitud.getFechaProcesamiento() != null ? solicitud.getFechaProcesamiento().format(FORMATTER) : "");
    
    return sb.toString();
}
```

**Análisis del protocolo de serialización**:
- **Línea 112**: StringBuilder para eficiencia en concatenación múltiple
- **Línea 117**: Escape de caracteres delimitadores en texto libre
- **Líneas 118-120**: Formateo consistente de fechas usando patrón definido
- **Líneas 122-124**: Manejo de campos opcionales con operador ternario
- **Línea 124**: Campo final sin delimitador (fin de registro)

### 2.5 Clase `Login.java` - Sistema de Autenticación

#### Líneas 15-25: Declaración de dependencias
```java
public class Login extends javax.swing.JFrame {

    private DatabaseManager dbManager;
    private EmailService emailService;
    private BitacoraManager bitacoraManager;
```

**Análisis**:
- Patrón de inyección de dependencias manual
- Cada manager se encarga de un aspecto específico del sistema

#### Líneas 30-35: Constructor con inicialización de servicios
```java
public Login() {
    initComponents();
    dbManager = new DatabaseManager();
    emailService = new EmailService();
    bitacoraManager = new BitacoraManager();
}
```

**Análisis**:
- **Línea 31**: Inicialización de componentes GUI (generado por NetBeans)
- **Líneas 32-34**: Instanciación de managers (patrón eager initialization)

#### Líneas 100-140: Lógica de autenticación principal
```java
private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {
    String usuario = txtusuario.getText().trim();
    String password = new String(Password.getPassword());

    // Validación de campos vacíos
    if (usuario.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Por favor ingrese usuario y contraseña",
                                    "Campos requeridos", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        // Intentar autenticación con base de datos
        Empleado empleadoAutenticado = dbManager.autenticarUsuario(usuario, password);

        if (empleadoAutenticado != null) {
            // Verificar rol del usuario
            String role = empleadoAutenticado.getRole();
            
            if ("AdminRRHH".equals(role)) {
                // Registrar login exitoso en bitácora
                bitacoraManager.registrarOperacion(usuario, "LOGIN",
                                                 "Acceso exitoso al sistema (AdminRRHH)", "");

                // Abrir menú principal de administración
                Menu menu = new Menu(empleadoAutenticado);
                menu.setVisible(true);
                dispose();
            } else if ("Empleado".equals(role)) {
                // Registrar login exitoso en bitácora
                bitacoraManager.registrarOperacion(usuario, "LOGIN",
                                                 "Acceso exitoso al sistema (Empleado)", "");

                // Abrir menú de empleado
                MenuEmpleado menuEmpleado = new MenuEmpleado(empleadoAutenticado);
                menuEmpleado.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Rol no reconocido: " + role,
                                            "Acceso denegado", JOptionPane.ERROR_MESSAGE);

                // Registrar intento de acceso con rol no válido
                bitacoraManager.registrarOperacion(usuario, "LOGIN_FAILED",
                                                 "Intento de acceso con rol no válido: " + role, usuario);
            }
        }
    }
}
```

**Análisis línea por línea del flujo de autenticación**:

- **Líneas 101-102**: Extracción y sanitización de credenciales
  - `trim()` elimina espacios en blanco accidentales
  - `getPassword()` retorna char[] por seguridad (no String)

- **Líneas 104-109**: Validación de entrada temprana (fail-fast)
  - Evita procesamiento innecesario si faltan datos
  - `return` evita anidamiento profundo de código

- **Línea 114**: Delegación de autenticación al manager especializado
  - Separación de responsabilidades (UI vs lógica de negocio)

- **Líneas 116-140**: Enrutamiento basado en roles
  - **Línea 118**: Extracción del rol del usuario autenticado
  - **Líneas 120-127**: Flujo para AdminRRHH
    - Registro de auditoría con contexto específico
    - Instanciación del menú administrativo con estado de usuario
    - `dispose()` libera recursos de la ventana de login
  
- **Líneas 128-135**: Flujo para Empleado común
  - Lógica similar pero con menú restringido
  
- **Líneas 136-142**: Manejo de roles no reconocidos
  - Registro de intento de acceso no autorizado
  - Mensaje de error específico para debugging

### 2.6 Clase `MenuEmpleado.java` - Interfaz Específica para Empleados

#### Líneas 20-35: Atributos y dependencias
```java
private Empleado empleadoActual;
private DatabaseManager dbManager;
private BitacoraManager bitacoraManager;

// Componentes de la interfaz
private JLabel lblBienvenida;
private JButton btnCrearSolicitud;
private JButton btnVerSolicitudes;
private JButton btnCerrarSesion;
private JLabel lblInfoEmpleado;
```

**Análisis**:
- **Líneas 20-22**: Referencias a managers para operaciones de datos
- **Líneas 25-29**: Componentes UI específicos del rol empleado

#### Líneas 40-50: Constructor con inyección de dependencias
```java
public MenuEmpleado(Empleado empleado) {
    this.empleadoActual = empleado;
    this.dbManager = new DatabaseManager();
    this.bitacoraManager = new BitacoraManager();
    
    initComponents();
    configurarVentana();
    mostrarInformacionEmpleado();
}
```

**Análisis del patrón de inicialización**:
- **Línea 41**: Recibe estado del empleado logueado
- **Líneas 42-43**: Inicializa managers necesarios
- **Líneas 45-47**: Secuencia de configuración (UI → Window → Data)

#### Líneas 55-120: Configuración de interfaz con listeners
```java
private void initComponents() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    // Panel principal
    JPanel panelPrincipal = new JPanel();
    panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
    panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    // Título de bienvenida
    lblBienvenida = new JLabel("Portal del Empleado");
    lblBienvenida.setFont(new Font("Tahoma", Font.BOLD, 28));
    lblBienvenida.setAlignmentX(Component.CENTER_ALIGNMENT);
    
    // Información del empleado
    lblInfoEmpleado = new JLabel();
    lblInfoEmpleado.setFont(new Font("Tahoma", Font.PLAIN, 16));
    lblInfoEmpleado.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Botones del menú
    btnCrearSolicitud = new JButton("Crear Solicitud de Vacaciones");
    btnCrearSolicitud.setFont(new Font("Tahoma", Font.BOLD, 18));
    btnCrearSolicitud.setAlignmentX(Component.CENTER_ALIGNMENT);
    btnCrearSolicitud.setPreferredSize(new Dimension(350, 50));
    btnCrearSolicitud.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            abrirCrearSolicitud();
        }
    });
```

**Análisis del diseño de UI**:
- **Líneas 56-58**: Configuración básica de ventana con BorderLayout
- **Líneas 61-63**: BoxLayout vertical para disposición secuencial
- **Línea 64**: Padding uniforme de 20px en todos los lados
- **Líneas 66-68**: Título principal con fuente específica y alineación centrada
- **Líneas 75-83**: Botón principal con:
  - Dimensiones fijas para consistencia visual
  - ActionListener con clase anónima
  - Delegación a método específico para separar lógica

#### Líneas 140-155: Método de creación de solicitudes
```java
private void abrirCrearSolicitud() {
    try {
        bitacoraManager.registrarOperacion(empleadoActual.getUsername(), "MENU_NAVEGACION",
                "Acceso a creación de solicitudes", "");
        
        CrearSolicitudVacaciones ventanaCrear = new CrearSolicitudVacaciones(empleadoActual);
        ventanaCrear.setVisible(true);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, 
                "Error al abrir la ventana de creación de solicitudes: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
```

**Análisis del patrón de navegación**:
- **Líneas 142-143**: Registro de auditoría para navegación
- **Línea 145**: Instanciación de ventana hija con contexto de empleado
- **Línea 146**: Mostrar ventana (no modal)
- **Líneas 147-151**: Manejo defensivo de errores con UI feedback

### 2.7 Clase `CrearSolicitudVacaciones.java` - Interfaz de Creación con Calendarios

#### Líneas 1-15: Imports específicos para calendarios
```java
package archivo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import com.toedter.calendar.JDateChooser;
```

**Análisis de dependencias**:
- **Líneas 3-6**: APIs estándar de Swing y AWT
- **Líneas 7-9**: APIs de tiempo modernas de Java 8+
- **Línea 10**: Biblioteca externa JCalendar para selectores de fecha

#### Líneas 25-35: Componentes especializados
```java
// Componentes de la interfaz
private JComboBox<Solicitud.TipoSolicitud> cmbTipoSolicitud;
private JDateChooser dateChooserInicio;
private JDateChooser dateChooserFin;
private JTextArea txtDescripcion;
private JButton btnCrear;
private JButton btnCancelar;
```

**Análisis de componentes**:
- **Línea 26**: ComboBox tipado con enum para type safety
- **Líneas 27-28**: Selectores de fecha especializados (no estándar de Swing)
- **Línea 29**: TextArea para descripción multilinea

#### Líneas 85-110: Configuración de calendarios con restricciones
```java
// Fecha de inicio con calendario
JLabel lblFechaInicio = new JLabel("Fecha de Inicio:");
lblFechaInicio.setFont(new Font("Tahoma", Font.BOLD, 14));
gbc.gridx = 0; gbc.gridy = 3;
gbc.fill = GridBagConstraints.NONE;
panelPrincipal.add(lblFechaInicio, gbc);

dateChooserInicio = new JDateChooser();
dateChooserInicio.setFont(new Font("Tahoma", Font.PLAIN, 12));
dateChooserInicio.setDateFormatString("dd/MM/yyyy");
dateChooserInicio.setMinSelectableDate(new Date()); // No permitir fechas pasadas
dateChooserInicio.setPreferredSize(new Dimension(150, 25));
gbc.gridx = 1; gbc.gridy = 3;
gbc.fill = GridBagConstraints.HORIZONTAL;
panelPrincipal.add(dateChooserInicio, gbc);
```

**Análisis de la configuración de calendario**:
- **Líneas 88-95**: Configuración estándar de label con GridBagLayout
- **Línea 97**: Instanciación del selector de fecha
- **Línea 99**: Formato de fecha localizado (DD/MM/YYYY)
- **Línea 100**: **Restricción crítica**: Solo fechas futuras seleccionables
- **Línea 101**: Dimensiones específicas para consistencia visual

#### Líneas 200-250: Lógica de validación y creación
```java
private void crearSolicitud() {
    try {
        // Validar campos
        if (!validarCampos()) {
            return;
        }

        // Obtener datos del formulario
        Solicitud.TipoSolicitud tipo = (Solicitud.TipoSolicitud) cmbTipoSolicitud.getSelectedItem();
        String descripcion = txtDescripcion.getText().trim();
        
        // Convertir fechas de Date a LocalDateTime
        LocalDateTime fechaInicio = convertirDateALocalDateTime(dateChooserInicio.getDate());
        LocalDateTime fechaFin = convertirDateALocalDateTime(dateChooserFin.getDate());

        // Validar fechas
        if (fechaInicio.isAfter(fechaFin)) {
            JOptionPane.showMessageDialog(this,
                    "La fecha de inicio no puede ser posterior a la fecha de fin",
                    "Error de fechas", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (fechaInicio.isBefore(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))) {
            JOptionPane.showMessageDialog(this,
                    "La fecha de inicio no puede ser anterior a la fecha actual",
                    "Error de fechas", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear la solicitud
        Solicitud nuevaSolicitud = new Solicitud(
                empleadoActual.getDpi(),
                empleadoActual.getNombre(),
                tipo,
                descripcion,
                fechaInicio,
                fechaFin
        );

        // Guardar la solicitud
        solicitudManager.guardarSolicitud(nuevaSolicitud);

        // Registrar en bitácora
        bitacoraManager.registrarOperacion(empleadoActual.getUsername(), "CREAR_SOLICITUD",
                "Solicitud creada: " + tipo.getDescripcion(), "ID: " + nuevaSolicitud.getId());

        // Mostrar confirmación
        JOptionPane.showMessageDialog(this,
                "Solicitud creada exitosamente.\nID de solicitud: " + nuevaSolicitud.getId(),
                "Solicitud creada", JOptionPane.INFORMATION_MESSAGE);

        dispose();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
                "Error al crear la solicitud: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
```

**Análisis detallado del flujo de creación**:

- **Líneas 202-206**: Validación temprana con early return
- **Líneas 208-210**: Extracción de datos del formulario con cast seguro
- **Líneas 212-213**: Conversión de API legacy (Date) a moderna (LocalDateTime)
- **Líneas 215-220**: Validación de lógica de negocio (fecha inicio < fecha fin)
- **Líneas 222-227**: Validación temporal (no fechas pasadas)
- **Líneas 229-236**: Construcción del objeto de dominio
- **Línea 238**: Persistencia delegada al manager especializado
- **Líneas 240-241**: Auditoría de operación con contexto
- **Líneas 243-246**: Feedback positivo al usuario con ID de confirmación
- **Línea 248**: Cierre limpio de la ventana

#### Líneas 280-290: Método de conversión de tipos de fecha
```java
private LocalDateTime convertirDateALocalDateTime(Date date) {
    return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .withHour(0)
            .withMinute(0)
            .withSecond(0);
}
```

**Análisis de la conversión de API legacy**:
- **Línea 281**: Convierte Date a Instant (UTC)
- **Línea 282**: Aplica zona horaria del sistema
- **Línea 283**: Convierte a LocalDateTime
- **Líneas 284-286**: Normaliza tiempo a medianoche (00:00:00)

---

## 3. Flujo de Datos y Procesos {#flujo-de-datos}

### 3.1 Flujo de Autenticación

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Login.java│───▶│DatabaseManager   │───▶│Empleadosguardados│
│             │    │.autenticarUsuario│    │.txt             │
└─────────────┘    └──────────────────┘    └─────────────────┘
       │                      │
       ▼                      ▼
┌─────────────┐    ┌──────────────────┐
│BitacoraManager│  │     Menu.java    │
│.registrar   │    │  MenuEmpleado.java│
│Operacion    │    │                  │
└─────────────┘    └──────────────────┘
```

### 3.2 Flujo de Creación de Solicitudes

```
┌──────────────────┐    ┌─────────────────┐    ┌──────────────┐
│CrearSolicitud    │───▶│  SolicitudManager│───▶│solicitudes.txt│
│Vacaciones.java   │    │ .guardarSolicitud│    │              │
└──────────────────┘    └─────────────────┘    └──────────────┘
         │                        │
         ▼                        ▼
┌──────────────────┐    ┌─────────────────┐
│BitacoraManager   │    │   EmailService   │
│.registrarOperacion│    │ .enviarNotificacion│
└──────────────────┘    └─────────────────┘
```

### 3.3 Flujo de Procesamiento de Solicitudes (Admin)

```
┌─────────────┐    ┌─────────────────┐    ┌──────────────┐
│Solicitudes  │───▶│ SolicitudManager│───▶│solicitudes.txt│
│.java        │    │.obtenerPendientes│    │              │
└─────────────┘    └─────────────────┘    └──────────────┘
       │                     │
       ▼                     ▼
┌─────────────┐    ┌─────────────────┐
│Solicitudes  │    │ SolicitudManager│
│.aprobar/    │───▶│.actualizarEstado│
│rechazar     │    │                 │
└─────────────┘    └─────────────────┘
```

---

## 4. Gestión de Archivos y Persistencia {#gestión-de-archivos}

### 4.1 Formato de Archivo `Empleadosguardados.txt`

```
username|password|nombre|dpi|area|turno|estado|email|role
admin|admin123|Administrador Sistema|1234567890123|Recursos Humanos|Diurno|Activo|admin@empresa.com|AdminRRHH
```

**Análisis del formato**:
- **Delimitador**: Pipe (`|`) para evitar conflictos con espacios en nombres
- **Orden fijo**: Permite parsing posicional
- **Sin cabeceras**: Formato directo para eficiencia

### 4.2 Formato de Archivo `solicitudes.txt`

```
ID|DPI|Nombre|Tipo|Descripción|FechaInicio|FechaFin|FechaSolicitud|Estado|AprobadoPor|MotivoRechazo|FechaProcesamiento
1|23430010101|Alfredo|VACACIONES|Vacaciones familiares|2025-09-10 00:00:00|2025-09-15 00:00:00|2025-09-05 14:30:00|PENDIENTE|||
```

**Análisis del protocolo**:
- **12 campos fijos**: Estructura completa de la solicitud
- **Escape de delimitadores**: `|` → `~` en texto libre
- **Campos opcionales**: Strings vacíos para datos no disponibles
- **Formato de fecha**: ISO 8601 modificado (`yyyy-MM-dd HH:mm:ss`)

### 4.3 Formato de Archivo `bitacora.txt`

```
timestamp|usuario|operacion|detalles|empleadoAfectado
2025-09-05 21:30:15|admin|LOGIN|Acceso exitoso al sistema (AdminRRHH)|
2025-09-05 21:31:20|evicente|CREAR_SOLICITUD|Solicitud creada: Vacaciones|ID: 1
```

**Análisis del sistema de auditoría**:
- **Timestamp automático**: Registro cronológico preciso
- **Usuario**: Quien realizó la operación
- **Operación**: Código de operación estandarizado
- **Detalles**: Información contextual variable
- **EmpleadoAfectado**: Para operaciones que afectan otros usuarios

---

## 5. Sistema de Autenticación y Roles {#sistema-de-autenticación}

### 5.1 Jerarquía de Roles

```
┌─────────────┐
│    Jefe     │ (Máximos privilegios)
└─────────────┘
       │
┌─────────────┐
│  AdminRRHH  │ (Gestión completa de usuarios y solicitudes)
└─────────────┘
       │
┌─────────────┐
│ Supervisor  │ (Gestión de equipo)
└─────────────┘
       │
┌─────────────┐
│  Empleado   │ (Operaciones básicas)
└─────────────┘
```

### 5.2 Matriz de Permisos

| Operación | Empleado | Supervisor | AdminRRHH | Jefe |
|-----------|----------|------------|-----------|------|
| Login | ✓ | ✓ | ✓ | ✓ |
| Crear solicitud vacaciones | ✓ | ✓ | ✓ | ✓ |
| Ver propias solicitudes | ✓ | ✓ | ✓ | ✓ |
| Aprobar/Rechazar solicitudes | ✗ | ✓ | ✓ | ✓ |
| Gestionar empleados | ✗ | ✗ | ✓ | ✓ |
| Gestionar roles | ✗ | ✗ | ✓ | ✓ |
| Ver bitácora completa | ✗ | ✗ | ✓ | ✓ |

### 5.3 Implementación de Control de Acceso

```java
// En Login.java - líneas 120-140
String role = empleadoAutenticado.getRole();

if ("AdminRRHH".equals(role)) {
    Menu menu = new Menu(empleadoAutenticado);    // Menú completo
    menu.setVisible(true);
} else if ("Empleado".equals(role)) {
    MenuEmpleado menuEmpleado = new MenuEmpleado(empleadoAutenticado);  // Menú restringido
    menuEmpleado.setVisible(true);
}
```

**Análisis**:
- Control de acceso basado en roles (RBAC)
- Interfaces diferentes según privilegios
- Validación en punto de entrada (login)

---

## 6. Interfaz Gráfica y Componentes {#interfaz-gráfica}

### 6.1 Arquitectura de UI

```
Application Layer
├── Login.java (Punto de entrada)
├── Menu.java (Admin dashboard)
├── MenuEmpleado.java (Employee dashboard)
└── Specialized Forms
    ├── CrearSolicitudVacaciones.java
    ├── VerSolicitudesEmpleado.java
    ├── Solicitudes.java (Admin)
    ├── MantenimientoUsuario.java
    ├── RegistrodeEmpleados.java
    ├── EmpleadosRegistrados.java
    └── GestionRoles.java
```

### 6.2 Patrones de Diseño UI

#### 6.2.1 Layout Management
```java
// GridBagLayout para formularios complejos - CrearSolicitudVacaciones.java
GridBagConstraints gbc = new GridBagConstraints();
gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
gbc.insets = new Insets(0, 0, 20, 0);
gbc.anchor = GridBagConstraints.CENTER;
```

#### 6.2.2 Event Handling Pattern
```java
// ActionListener con clase anónima - patrón estándar
btnCrear.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        crearSolicitud();  // Delegación a método específico
    }
});
```

#### 6.2.3 Form Validation Pattern
```java
private boolean validarCampos() {
    if (dateChooserInicio.getDate() == null) {
        JOptionPane.showMessageDialog(this, "La fecha de inicio es requerida",
                "Campo requerido", JOptionPane.WARNING_MESSAGE);
        return false;  // Early return para eficiencia
    }
    // ... más validaciones
    return true;
}
```

### 6.3 Componentes Especializados

#### 6.3.1 JDateChooser Integration
```java
import com.toedter.calendar.JDateChooser;

dateChooserInicio = new JDateChooser();
dateChooserInicio.setDateFormatString("dd/MM/yyyy");
dateChooserInicio.setMinSelectableDate(new Date()); // Restricción de fechas pasadas
```

**Análisis**:
- Biblioteca externa para mejor UX en selección de fechas
- Validación automática de rangos
- Formato localizado

#### 6.3.2 Table Rendering with Custom Colors
```java
// En VerSolicitudesEmpleado.java
tablaSolicitudes.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
                                                 boolean isSelected, boolean hasFocus, 
                                                 int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (!isSelected) {
            String estado = (String) table.getValueAt(row, 6);
            switch (estado) {
                case "Pendiente":
                    c.setBackground(new Color(255, 243, 205)); // Amarillo claro
                    break;
                case "Aprobada":
                    c.setBackground(new Color(212, 237, 218)); // Verde claro
                    break;
                case "Rechazada":
                    c.setBackground(new Color(248, 215, 218)); // Rojo claro
                    break;
            }
        }
        return c;
    }
});
```

**Análisis del renderer personalizado**:
- **Líneas 3-5**: Override del método de rendering estándar
- **Línea 7**: Condición para no sobrescribir selección
- **Línea 8**: Extracción del estado desde el modelo de datos
- **Líneas 9-17**: Color coding basado en estado de solicitud
- **Línea 18**: Return del componente modificado

---

## 7. Sistema de Solicitudes de Vacaciones {#sistema-de-solicitudes}

### 7.1 Estados y Transiciones

```
┌─────────────┐    aprobar()    ┌─────────────┐
│  PENDIENTE  │ ──────────────▶ │  APROBADA   │
└─────────────┘                 └─────────────┘
       │
       │ rechazar()
       ▼
┌─────────────┐
│  RECHAZADA  │
└─────────────┘
```

### 7.2 Workflow de Procesamiento

#### 7.2.1 Creación (Empleado)
```java
// 1. Validación de entrada
if (!validarCampos()) return;

// 2. Conversión de tipos
LocalDateTime fechaInicio = convertirDateALocalDateTime(dateChooserInicio.getDate());

// 3. Validación de lógica de negocio
if (fechaInicio.isAfter(fechaFin)) {
    // Error: fechas inconsistentes
}

// 4. Creación del objeto
Solicitud nuevaSolicitud = new Solicitud(empleadoActual.getDpi(), ...);

// 5. Persistencia
solicitudManager.guardarSolicitud(nuevaSolicitud);

// 6. Auditoría
bitacoraManager.registrarOperacion(...);
```

#### 7.2.2 Procesamiento (Admin)
```java
// 1. Carga de solicitudes pendientes
List<Solicitud> solicitudes = solicitudManager.obtenerSolicitudesPendientes();

// 2. Actualización de estado
solicitudManager.actualizarEstadoSolicitud(solicitudId, EstadoSolicitud.APROBADA, 
                                         usuarioActual.getUsername(), null);

// 3. Notificación
emailService.enviarAprobacionSolicitud(empleado.getEmail(), ...);

// 4. Auditoría
bitacoraManager.registrarOperacion(usuarioActual.getUsername(), "APPROVE_REQUEST", ...);
```

### 7.3 Validaciones del Sistema

#### 7.3.1 Validaciones de Entrada
- **Campos requeridos**: Fechas y descripción no pueden estar vacíos
- **Formato de fecha**: Debe seguir patrón dd/MM/yyyy
- **Rangos temporales**: Fecha inicio ≤ fecha fin
- **Fechas pasadas**: No se permiten fechas anteriores a hoy

#### 7.3.2 Validaciones de Negocio
- **Solapamiento**: (No implementado) Verificar conflictos con solicitudes existentes
- **Límites anuales**: (No implementado) Verificar días de vacaciones disponibles
- **Períodos críticos**: (No implementado) Restricciones por fechas corporativas

---

## 8. Configuración y Despliegue {#configuración}

### 8.1 Estructura de Configuración

```
Shift-Control-System/
├── email-config.properties          # Configuración SMTP
├── email-config.properties.example  # Plantilla de configuración
├── build.xml                       # Script de construcción Ant
├── manifest.mf                     # Manifiesto JAR
└── lib/                            # Dependencias externas
    ├── javax.mail-1.6.2.jar
    └── jcalendar-1.4.jar
```

### 8.2 Configuración de Email

```properties
# email-config.properties
email.enabled=true
email.smtp.host=smtp.gmail.com
email.smtp.port=587
email.smtp.username=tu-email@gmail.com
email.smtp.password=tu-app-password
email.smtp.starttls.enable=true
email.smtp.auth=true
```

### 8.3 Build Process

```xml
<!-- build.xml - Target de compilación -->
<target name="compile" depends="init">
    <javac srcdir="${src.dir}" destdir="${build.classes.dir}" 
           classpathref="classpath" includeantruntime="false"/>
</target>
```

### 8.4 Deployment Checklist

1. **Configuración**:
   - [ ] Copiar `email-config.properties.example` a `email-config.properties`
   - [ ] Configurar credenciales SMTP
   - [ ] Verificar permisos de archivos de datos

2. **Compilación**:
   ```bash
   ant clean compile
   ant jar
   ```

3. **Ejecución**:
   ```bash
   java -cp "lib/*:dist/Shift-Control-System.jar" archivo.Login
   ```

4. **Verificación**:
   - [ ] Login con credenciales por defecto
   - [ ] Creación de empleado de prueba
   - [ ] Envío de email de prueba
   - [ ] Creación de solicitud de vacaciones

---

## 9. Consideraciones de Seguridad

### 9.1 Vulnerabilidades Identificadas

1. **Almacenamiento de contraseñas**: 
   - Contraseñas en texto plano en archivos
   - **Recomendación**: Implementar hashing (BCrypt, PBKDF2)

2. **Validación de entrada**:
   - Sin sanitización de caracteres especiales
   - **Recomendación**: Validación y escape de entrada

3. **Control de acceso**:
   - Validación solo en UI, no en lógica de negocio
   - **Recomendación**: Validación en múltiples capas

### 9.2 Mejoras Recomendadas

1. **Autenticación**:
   ```java
   // Implementar hashing de contraseñas
   String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
   ```

2. **Autorización**:
   ```java
   // Validación en managers
   public void aprobarSolicitud(Empleado usuario, int solicitudId) {
       if (!usuario.tienePermiso("APROBAR_SOLICITUDES")) {
           throw new SecurityException("Acceso denegado");
       }
       // ... lógica de aprobación
   }
   ```

3. **Auditoría**:
   - Registrar intentos de acceso no autorizado
   - Logging detallado de operaciones críticas
   - Rotación de logs

---

## 10. Conclusiones y Arquitectura

### 10.1 Fortalezas del Sistema

1. **Separación de responsabilidades**: Cada clase tiene una responsabilidad específica
2. **Extensibilidad**: Fácil agregar nuevos tipos de solicitud o roles
3. **Trazabilidad**: Sistema completo de auditoría
4. **Usabilidad**: Interfaces específicas por rol

### 10.2 Áreas de Mejora

1. **Persistencia**: Migrar de archivos a base de datos relacional
2. **Seguridad**: Implementar autenticación robusta
3. **Concurrencia**: Manejo de acceso concurrente a archivos
4. **Testing**: Implementar suite de pruebas unitarias

### 10.3 Patrones Arquitectónicos Implementados

- **MVC**: Separación modelo-vista-controlador
- **Manager Pattern**: Centralización de lógica de negocio
- **Observer Pattern**: Listeners en componentes UI
- **Strategy Pattern**: Diferentes interfaces según rol
- **Factory Pattern**: Creación de ventanas según contexto

Este sistema representa una solución funcional para gestión de turnos y solicitudes, con una base sólida para evolución futura hacia una arquitectura más robusta y escalable.
