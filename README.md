# Shift Control System (Sistema de Control de Turnos)

A comprehensive Java desktop application for employee shift management and HR administration, built with Swing GUI and file-based data storage.

## Features

### Core Functionality
- **Employee Management**: Complete CRUD operations for employee records
- **Role-Based Access Control**: Multi-level permission system (Employee, Supervisor, AdminRRHH, Jefe)
- **Shift Management**: Track and manage employee work schedules
- **Request System**: Handle employee requests (vacations, personal leave, medical appointments, etc.)
- **Audit Trail**: Comprehensive logging system with BitacoraManager
- **Email Notifications**: Automated email system for important events

### User Roles & Permissions
- **Empleado (Employee)**: Basic access, submit requests
- **Supervisor**: Manage team schedules and approve requests
- **AdminRRHH (HR Admin)**: Full user management, role assignment, employee lifecycle
- **Jefe (Manager)**: High-level oversight and management

### Advanced Features
- **Enhanced Role Management Interface**: Professional table view with sorting, filtering, and color-coded roles
- **Email Integration**: SMTP-based email system with Gmail support
- **Data Persistence**: File-based storage with backup and recovery
- **Form Validation**: Comprehensive input validation and error handling
- **Responsive GUI**: Modern Swing interface with professional styling

## Requirements

### System Requirements
- **Java**: JDK 8 or higher
- **Build Tool**: Apache Ant
- **Email**: Gmail account (for email notifications) or custom SMTP server

### Dependencies
- `javax.mail-1.6.2.jar` (included in `lib/` directory) - For email notifications
- `jcalendar-1.4.jar` (included in `lib/` directory) - For calendar date picker components

### Manual Dependency Installation (if needed)

If the dependencies are not included in the `lib/` directory, you can download them manually:

#### JavaMail API
```bash
# Download javax.mail dependency
wget https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar -P lib/

# Or using curl
curl -o lib/javax.mail-1.6.2.jar https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar
```

#### JCalendar Library
```bash
# Download JCalendar dependency
wget https://repo1.maven.org/maven2/com/toedter/jcalendar/1.4/jcalendar-1.4.jar -P lib/

# Or using curl
curl -o lib/jcalendar-1.4.jar https://repo1.maven.org/maven2/com/toedter/jcalendar/1.4/jcalendar-1.4.jar
```

#### Alternative: Maven Central Links
- **JavaMail**: https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar
- **JCalendar**: https://repo1.maven.org/maven2/com/toedter/jcalendar/1.4/jcalendar-1.4.jar

#### Verify Dependencies
```bash
# Check if dependencies are properly installed
ls -la lib/
# Should show:
# javax.mail-1.6.2.jar
# jcalendar-1.4.jar
```

## Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/eedvi/Shift-Control-System.git
cd Shift-Control-System
```

### 2. Configure Email Service (Optional)
Edit `email-config.properties`:
```properties
# Enable email notifications
email.enabled=true

# Gmail Configuration
email.username=your-email@gmail.com
email.password=your-app-password  # Use Gmail App Password
email.from=your-email@gmail.com
```

**Gmail Setup Instructions:**
1. Enable 2-Factor Authentication on your Google account
2. Generate an App Password: [Google App Passwords](https://myaccount.google.com/apppasswords)
3. Use the App Password (not your regular password) in `email.password`

### 3. Build the Project
```bash
ant clean
ant compile
```

### 4. Run the Application
```bash
ant run
```

Or manually:
```bash
java -cp "build/classes:lib/*" archivo.Login
```

## Project Structure

```
Shift-Control-System/
├── src/archivo/                    # Source code
│   ├── Login.java                 # Main entry point & authentication
│   ├── Menu.java                  # Main application menu
│   ├── Empleado.java             # Employee data model
│   ├── MantenimientoUsuario.java # Employee maintenance
│   ├── RegistrodeEmpleados.java  # Employee registration
│   ├── EmpleadosRegistrados.java # Employee listing
│   ├── GestionRoles.java         # Role management (Enhanced)
│   ├── Solicitudes.java          # Request management
│   ├── Solicitud.java            # Request data model
│   ├── DatabaseManager.java     # Data persistence
│   ├── BitacoraManager.java     # Audit logging
│   ├── EmailService.java        # Email notifications
│   └── Archivo.java             # File operations
├── lib/
│   └── javax.mail-1.6.2.jar     # Email dependency
├── build/                        # Compiled classes
├── data files/
│   ├── Empleadosguardados.txt    # Employee data
│   ├── solicitudes.txt           # Requests data
│   ├── bitacora.txt             # Audit log
│   └── AgendaContactos.txt      # Contact information
├── email-config.properties       # Email configuration
├── build.xml                     # Ant build file
└── README.md                     # This file
```

## Usage Guide

### Initial Login
- **Default Admin**: Create initial admin account through the system
- **Role Assignment**: AdminRRHH can assign roles to other users

### Employee Management
1. **Registration**: Use "Registro de Empleados" to add new employees
2. **View Records**: Access complete employee listing with enhanced table view
3. **Role Management**: Use "Gestión de Roles" for role assignments (Enhanced UI)
4. **Deactivation**: Manage employee lifecycle including deactivation

### Request Management
1. **Submit Requests**: Employees can submit various types of requests
2. **Approval Workflow**: Supervisors and managers can approve/reject requests
3. **Status Tracking**: Real-time status updates for all requests

### Audit & Monitoring
- All system operations are logged in `bitacora.txt`
- Email notifications for important events (when enabled)
- Comprehensive user activity tracking

## Recent Enhancements

### Enhanced Role Management Interface
- **Professional Table View**: Replaced basic text area with sortable JTable
- **Color-Coded Roles**: Visual distinction between different user roles
- **Real-time Statistics**: Live counts of users by role and status
- **Improved User Experience**: Better navigation and data presentation

### Email System Integration
- **SMTP Configuration**: Support for Gmail and custom SMTP servers
- **Automated Notifications**: Role changes, account status updates
- **Configurable Settings**: Enable/disable email features

## Configuration

### Email Settings
Located in `email-config.properties`:
- Configure SMTP server settings
- Set up authentication credentials
- Enable/disable email functionality

### Build Configuration
Located in `build.xml`:
- Customize build targets
- Modify classpath settings
- Configure distribution options

## Troubleshooting

### Common Issues
1. **Email not working**: Verify `email-config.properties` and Gmail App Password
2. **Build failures**: Ensure Java 8+ and Ant are properly installed
3. **Data persistence**: Check file permissions for data files
