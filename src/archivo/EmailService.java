package archivo;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Email Service for sending real email notifications
 * Now configured with JavaMail API for actual email delivery
 */
public class EmailService {

    private String smtpHost;
    private String smtpPort;
    private String username;
    private String password;
    private String fromEmail;
    private boolean useSSL;
    private boolean enabled;

    public EmailService() {
        loadEmailConfiguration();
    }

    private void loadEmailConfiguration() {
        try {
            Properties config = new Properties();
            config.load(new java.io.FileInputStream("email-config.properties"));

            this.smtpHost = config.getProperty("smtp.host", "smtp.gmail.com");
            this.smtpPort = config.getProperty("smtp.port", "587");
            this.username = config.getProperty("email.username", "your-email@gmail.com");
            this.password = config.getProperty("email.password", "your-app-password");
            this.fromEmail = config.getProperty("email.from", "noreply@empresa.com");
            this.useSSL = Boolean.parseBoolean(config.getProperty("smtp.use.ssl", "false"));
            this.enabled = Boolean.parseBoolean(config.getProperty("email.enabled", "false"));

            if (enabled) {
                System.out.println("Email service loaded from configuration file and ENABLED.");
            } else {
                System.out.println("Email service loaded from configuration file but DISABLED. Set email.enabled=true to activate.");
            }

        } catch (Exception e) {
            System.out.println("Email configuration file not found, using defaults (simulation mode).");
            this.smtpHost = "smtp.gmail.com";
            this.smtpPort = "587";
            this.username = "your-email@gmail.com";
            this.password = "your-app-password";
            this.fromEmail = "noreply@empresa.com";
            this.useSSL = false;
            this.enabled = false;
        }

        System.out.println("SMTP Host: " + smtpHost + ", Port: " + smtpPort + ", From: " + fromEmail);
    }

    public boolean enviarNotificacionInactividad(String email, String nombreEmpleado, String motivo) {
        String subject = "Notificación de Cambio de Estado Laboral";
        String content = buildInactivityEmailContent(nombreEmpleado, motivo);

        if (enabled) {
            return sendEmail(email, subject, content);
        } else {
            logEmailToConsole(email, subject, content);
            return true;
        }
    }

    public boolean enviarNotificacion(String email, String asunto, String mensaje) {
        if (enabled) {
            return sendEmail(email, asunto, mensaje);
        } else {
            logEmailToConsole(email, asunto, mensaje);
            return true;
        }
    }

    public boolean enviarBienvenida(String email, String nombreEmpleado, String username) {
        String subject = "Bienvenido al Sistema de Control de Turnos";
        String content = buildWelcomeEmailContent(nombreEmpleado, username);

        if (enabled) {
            return sendEmail(email, subject, content);
        } else {
            logEmailToConsole(email, subject, content);
            return true;
        }
    }

    public boolean enviarAprobacionSolicitud(String email, String nombreEmpleado, String tipoSolicitud) {
        String subject = "Solicitud Aprobada - " + tipoSolicitud;
        String content = buildApprovalEmailContent(nombreEmpleado, tipoSolicitud);

        if (enabled) {
            return sendEmail(email, subject, content);
        } else {
            logEmailToConsole(email, subject, content);
            return true;
        }
    }

    public boolean enviarRechazoSolicitud(String email, String nombreEmpleado, String tipoSolicitud, String motivo) {
        String subject = "Solicitud Rechazada - " + tipoSolicitud;
        String content = buildRejectionEmailContent(nombreEmpleado, tipoSolicitud, motivo);

        if (enabled) {
            return sendEmail(email, subject, content);
        } else {
            logEmailToConsole(email, subject, content);
            return true;
        }
    }

    public boolean enviarEmailSimulado(String email, String asunto, String mensaje) {
        return enviarNotificacion(email, asunto, mensaje);
    }

    private boolean sendEmail(String toEmail, String subject, String content) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");

            // Enhanced Gmail-specific settings
            if ("smtp.gmail.com".equals(smtpHost)) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
                props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            } else if (useSSL) {
                props.put("mail.smtp.ssl.enable", "true");
            } else {
                props.put("mail.smtp.starttls.enable", "true");
            }

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("Authenticating with username: " + username);
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("Error sending email to " + toEmail + ": " + e.getMessage());

            if (e.getMessage().contains("Username and Password not accepted")) {
                System.err.println("Gmail Authentication Failed. Please check:");
                System.err.println("1. Make sure 2-Factor Authentication is enabled on your Google account");
                System.err.println("2. Use an App Password instead of your regular password");
                System.err.println("3. Generate App Password at: https://myaccount.google.com/apppasswords");
                System.err.println("4. Make sure 'Less secure app access' is not required (use App Password instead)");
            }

            return false;
        }
    }

    private void logEmailToConsole(String email, String subject, String content) {
        System.out.println("=== EMAIL NOTIFICATION (SIMULATED) ===");
        System.out.println("To: " + email);
        System.out.println("Subject: " + subject);
        System.out.println("Content:");
        System.out.println(content);
        System.out.println("=== END EMAIL ===");
    }

    private String buildInactivityEmailContent(String nombreEmpleado, String motivo) {
        return "Estimado/a " + nombreEmpleado + ",\n\n" +
               "Le informamos que su estado laboral ha sido modificado a 'Inactivo'.\n" +
               "Motivo: " + motivo + "\n\n" +
               "Si tiene alguna consulta, por favor contacte al departamento de Recursos Humanos.\n\n" +
               "Saludos cordiales,\n" +
               "Sistema de Control de Turnos";
    }

    private String buildWelcomeEmailContent(String nombreEmpleado, String username) {
        return "Estimado/a " + nombreEmpleado + ",\n\n" +
               "Bienvenido/a al Sistema de Control de Turnos.\n" +
               "Su nombre de usuario es: " + username + "\n\n" +
               "Por favor, mantenga esta información confidencial.\n\n" +
               "Saludos cordiales,\n" +
               "Departamento de Recursos Humanos";
    }

    private String buildApprovalEmailContent(String nombreEmpleado, String tipoSolicitud) {
        return "Estimado/a " + nombreEmpleado + ",\n\n" +
               "Su solicitud de " + tipoSolicitud + " ha sido APROBADA.\n\n" +
               "Puede proceder con los arreglos necesarios.\n\n" +
               "Saludos cordiales,\n" +
               "Departamento de Recursos Humanos";
    }

    private String buildRejectionEmailContent(String nombreEmpleado, String tipoSolicitud, String motivo) {
        return "Estimado/a " + nombreEmpleado + ",\n\n" +
               "Lamentamos informarle que su solicitud de " + tipoSolicitud + " ha sido RECHAZADA.\n" +
               "Motivo: " + motivo + "\n\n" +
               "Si tiene alguna consulta, por favor contacte al departamento de Recursos Humanos.\n\n" +
               "Saludos cordiales,\n" +
               "Departamento de Recursos Humanos";
    }

    public void configureEmail(String smtpHost, String smtpPort, String username, String password, String fromEmail) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
        this.enabled = true;
        System.out.println("Email service configured and enabled.");
    }

    public void enableEmailService() {
        this.enabled = true;
    }

    public void disableEmailService() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
