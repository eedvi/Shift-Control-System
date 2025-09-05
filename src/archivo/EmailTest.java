package archivo;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Gmail Email Test Utility
 * Use this to test your Gmail configuration separately
 */
public class EmailTest {
    
    public static void main(String[] args) {
        // Test Gmail configuration
        String username = "vicenterafael683@gmail.com";
        String password = "ylgglqwnfjmdldscx";  // Your App Password
        String toEmail = "vicenterafael683@gmail.com"; // Send to yourself for testing
        
        System.out.println("Testing Gmail SMTP connection...");
        System.out.println("Username: " + username);
        System.out.println("Password length: " + password.length() + " characters");
        
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.debug", "true"); // Enable debug output
        
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("Authenticating with Gmail...");
                    return new PasswordAuthentication(username, password);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Test Email from Shift Control System");
            message.setText("This is a test email to verify Gmail SMTP configuration.");
            
            System.out.println("Sending test email...");
            Transport.send(message);
            
            System.out.println("✅ SUCCESS! Email sent successfully!");
            System.out.println("Gmail configuration is working properly.");
            
        } catch (MessagingException e) {
            System.err.println("❌ FAILED! Error sending email:");
            System.err.println(e.getMessage());
            e.printStackTrace();
            
            if (e.getMessage().contains("Username and Password not accepted")) {
                System.err.println("\n🔧 TROUBLESHOOTING STEPS:");
                System.err.println("1. Verify 2-Factor Authentication is enabled on your Google account");
                System.err.println("2. Generate a NEW App Password at: https://myaccount.google.com/apppasswords");
                System.err.println("3. Select 'Mail' as the app and 'Other' as the device");
                System.err.println("4. Copy the 16-character password exactly (no spaces)");
                System.err.println("5. Update email-config.properties with the new App Password");
                System.err.println("\nCurrent password format check:");
                System.err.println("- Length: " + password.length() + " characters (should be 16)");
                System.err.println("- Contains spaces: " + password.contains(" "));
                System.err.println("- All lowercase: " + password.equals(password.toLowerCase()));
            }
        }
    }
}
