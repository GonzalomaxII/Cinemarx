package CINEMARX.M4;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    public static void sendReceiptEmail(String recipientEmail, String subject, String htmlBody) {
        // Replace with your email credentials and SMTP server
        final String username = "YOUR_EMAIL@example.com";
        final String password = "YOUR_PASSWORD";
        final String smtpHost = "smtp.example.com";
        final int smtpPort = 587; // Or your SMTP port

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Create the HTML body part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");

            // Create the image part
            MimeBodyPart imagePart = new MimeBodyPart();
            String imagePath = java.nio.file.Paths.get(System.getProperty("user.dir"), "RECURSOS", "IMAGENES", "CINEMARX logotipo.png").toString();
            imagePart.attachFile(imagePath);
            imagePart.setContentID("<logo>");
            imagePart.setDisposition(MimeBodyPart.INLINE);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            multipart.addBodyPart(imagePart);

            // Set the complete message parts
            message.setContent(multipart);

            // Send message
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException | java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String recipient = "RECIPIENT_EMAIL@example.com"; // Replace with the recipient's email address
        String subject = "Tu recibo de CINEMARX";

        // Construct the HTML body
        String htmlBody = """
            <html>
            <body style=\"font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;\">
                <div style=\"max-width: 600px; margin: auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">
                    <div style=\"text-align: center; padding-bottom: 20px; border-bottom: 1px solid #dddddd;\">
                        <img src=\"cid:logo\" alt=\"CINEMARX Logo\" style=\"max-width: 200px;\">
                    </div>
                    <div style=\"padding: 20px 0;\">
                        <h1 style=\"color: #333333;\">¡Gracias por tu compra!</h1>
                        <p style=\"color: #555555;\">Hola,</p>
                        <p style=\"color: #555555;\">Aquí tienes el resumen de tu compra en CINEMARX:</p>
                        <table style=\"width: 100%; border-collapse: collapse; margin-top: 20px;\">
                            <tr style=\"background-color: #f9f9f9;\">
                                <th style=\"padding: 10px; border: 1px solid #dddddd; text-align: left;\">Producto</th>
                                <th style=\"padding: 10px; border: 1px solid #dddddd; text-align: right;\">Precio</th>
                            </tr>
                            <tr>
                                <td style=\"padding: 10px; border: 1px solid #dddddd;\">2 x Entradas - Película de Ejemplo</td>
                                <td style=\"padding: 10px; border: 1px solid #dddddd; text-align: right;\">$20.00</td>
                            </tr>
                            <tr>
                                <td style=\"padding: 10px; border: 1px solid #dddddd;\">1 x Combo Mediano (Palomitas + Refresco)</td>
                                <td style=\"padding: 10px; border: 1px solid #dddddd; text-align: right;\">$10.00</td>
                            </tr>
                            <tr style=\"font-weight: bold;\">
                                <td style=\"padding: 10px; border: 1px solid #dddddd; text-align: right;\">Total:</td>
                                <td style=\"padding: 10px; border: 1px solid #dddddd; text-align: right;\">$30.00</td>
                            </tr>
                        </table>
                        <p style=\"color: #555555; margin-top: 20px;\">¡Disfruta de la función!</p>
                    </div>
                    <div style=\"text-align: center; padding-top: 20px; border-top: 1px solid #dddddd; font-size: 12px; color: #999999;\">
                        <p>CINEMARX &copy; 2025</p>
                    </div>
                </div>
            </body>
            </html>
            """;

        sendReceiptEmail(recipient, subject, htmlBody);
    }
}
