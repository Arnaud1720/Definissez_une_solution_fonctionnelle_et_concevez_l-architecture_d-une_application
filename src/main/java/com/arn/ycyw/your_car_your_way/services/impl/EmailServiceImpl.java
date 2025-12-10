package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.services.EmailService;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;


    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendBookingConfirmationWithInvoice(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            long priceHT,
            long tvaAmount,
            long priceTTC,
            byte[] invoicePdf
    ) {
        try {
            System.out.println("➡️ Préparation de l'email de confirmation pour " + user.getEmail());

            MimeMessage message = mailSender.createMimeMessage();

            // Créer le contenu multipart
            MimeMultipart multipart = new MimeMultipart("mixed");

            // Partie HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent = buildConfirmationEmailHtml(user, rental, departureAgency, returnAgency, priceHT, tvaAmount, priceTTC);
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // Partie PDF (pièce jointe)
            MimeBodyPart pdfPart = new MimeBodyPart();
            DataSource dataSource = new ByteArrayDataSource(invoicePdf, "application/pdf");
            pdfPart.setDataHandler(new DataHandler(dataSource));
            pdfPart.setFileName("facture_YCYW_" + rental.getId() + ".pdf");
            multipart.addBodyPart(pdfPart);

            // Configurer le message
            message.setFrom(fromEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, user.getEmail());
            message.setSubject("✅ Confirmation de votre réservation #" + rental.getId() + " - Your Car Your Way");
            message.setContent(multipart);

            // Envoyer
            mailSender.send(message);
            System.out.println("✅ Email de confirmation envoyé à " + user.getEmail());

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'envoi de l'email de confirmation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendCancellationConfirmation(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            int refundPercentage
    ) {
        try {
            System.out.println("➡️ Préparation de l'email d'annulation pour " + user.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("❌ Annulation de votre réservation #" + rental.getId() + " - Your Car Your Way");

            String htmlContent = buildCancellationEmailHtml(user, rental, departureAgency, returnAgency, refundPercentage);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ Email d'annulation envoyé à " + user.getEmail());

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'envoi de l'email d'annulation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===== TEMPLATES HTML =====

    private String buildConfirmationEmailHtml(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            long priceHT,
            long tvaAmount,
            long priceTTC
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #2563eb 0%%, #1d4ed8 100%%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; }
                    .content { padding: 30px; }
                    .badge { display: inline-block; background: #22c55e; color: white; padding: 8px 16px; border-radius: 20px; font-weight: bold; margin-bottom: 20px; }
                    .info-card { background: #f8fafc; border-radius: 8px; padding: 20px; margin: 15px 0; }
                    .info-card h3 { margin: 0 0 10px 0; color: #1e40af; font-size: 14px; text-transform: uppercase; }
                    .info-card p { margin: 5px 0; color: #334155; }
                    .price-table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                    .price-table td { padding: 10px; border-bottom: 1px solid #e2e8f0; }
                    .price-table .label { color: #64748b; }
                    .price-table .total { font-weight: bold; font-size: 18px; color: #1e40af; border-top: 2px solid #1e40af; }
                    .footer { background: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 12px; }
                    .attachment-note { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0; border-radius: 0 8px 8px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚗 Your Car Your Way</h1>
                        <p>Confirmation de réservation</p>
                    </div>
                    <div class="content">
                        <span class="badge">✓ Confirmée</span>
                        <h2>Bonjour %s,</h2>
                        <p>Votre réservation a été confirmée avec succès !</p>
                        
                        <div class="info-card">
                            <h3>📍 Départ</h3>
                            <p><strong>%s</strong></p>
                            <p>%s, %s</p>
                            <p>📅 %s</p>
                        </div>
                        
                        <div class="info-card">
                            <h3>📍 Retour</h3>
                            <p><strong>%s</strong></p>
                            <p>%s, %s</p>
                            <p>📅 %s</p>
                        </div>
                        
                        <div class="info-card">
                            <h3>🚙 Véhicule</h3>
                            <p><strong>Catégorie %s</strong></p>
                        </div>
                        
                        <table class="price-table">
                            <tr><td class="label">Prix HT</td><td align="right">%.2f €</td></tr>
                            <tr><td class="label">TVA (20%%)</td><td align="right">%.2f €</td></tr>
                            <tr class="total"><td>Total TTC</td><td align="right">%.2f €</td></tr>
                        </table>
                        
                        <div class="attachment-note">
                            📎 <strong>Facture jointe</strong><br>
                            Vous trouverez votre facture en pièce jointe de cet email.
                        </div>
                        
                        <p><strong>Rappels importants :</strong></p>
                        <ul>
                            <li>Présentez votre confirmation (ID #%d)</li>
                            <li>Munissez-vous de votre permis de conduire</li>
                            <li>Annulation gratuite jusqu'à 7 jours avant</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>Your Car Your Way - Location de véhicules en Europe</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFirstName(),
                departureAgency.getName(),
                departureAgency.getCity(),
                departureAgency.getCountry(),
                rental.getStartDate().format(dateFormatter),
                returnAgency.getName(),
                returnAgency.getCity(),
                returnAgency.getCountry(),
                rental.getEndDate().format(dateFormatter),
                rental.getCatCar(),
                priceHT / 100.0,
                tvaAmount / 100.0,
                priceTTC / 100.0,
                rental.getId()
        );
    }

    private String buildCancellationEmailHtml(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            int refundPercentage
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

        double originalPrice = rental.getPrice() / 100.0;
        double refundAmount = originalPrice * refundPercentage / 100.0;

        String refundMessage;
        if (refundPercentage == 100) {
            refundMessage = "Vous serez remboursé intégralement.";
        } else {
            refundMessage = "Conformément à nos conditions, vous serez remboursé à hauteur de " + refundPercentage + "%.";
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #dc2626 0%%, #b91c1c 100%%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; }
                    .content { padding: 30px; }
                    .badge { display: inline-block; background: #ef4444; color: white; padding: 8px 16px; border-radius: 20px; font-weight: bold; margin-bottom: 20px; }
                    .info-card { background: #f8fafc; border-radius: 8px; padding: 20px; margin: 15px 0; }
                    .info-card h3 { margin: 0 0 10px 0; color: #991b1b; font-size: 14px; text-transform: uppercase; }
                    .info-card p { margin: 5px 0; color: #334155; }
                    .refund-box { background: #dcfce7; border: 2px solid #22c55e; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center; }
                    .refund-box .amount { font-size: 28px; font-weight: bold; color: #16a34a; }
                    .refund-box .label { color: #166534; }
                    .footer { background: #f8fafc; padding: 20px; text-align: center; color: #64748b; font-size: 12px; }
                    .warning { background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; margin: 20px 0; border-radius: 0 8px 8px 0; }
                    .btn { display: inline-block; background: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 8px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚗 Your Car Your Way</h1>
                        <p>Confirmation d'annulation</p>
                    </div>
                    <div class="content">
                        <span class="badge">✕ Annulée</span>
                        <h2>Bonjour %s,</h2>
                        <p>Votre réservation <strong>#%d</strong> a bien été annulée.</p>
                        
                        <div class="info-card">
                            <h3>📍 Réservation annulée</h3>
                            <p><strong>Départ :</strong> %s (%s)</p>
                            <p>📅 %s</p>
                            <p><strong>Retour :</strong> %s (%s)</p>
                            <p>📅 %s</p>
                            <p><strong>Catégorie :</strong> %s</p>
                        </div>
                        
                        <div class="refund-box">
                            <p class="label">Montant remboursé</p>
                            <p class="amount">%.2f €</p>
                            <p class="label">(%d%% du montant initial de %.2f €)</p>
                        </div>
                        
                        <div class="warning">
                            💡 <strong>Délai de remboursement</strong><br>
                            %s<br>
                            Le remboursement sera effectué sous 5 à 10 jours ouvrés sur le moyen de paiement utilisé lors de la réservation.
                        </div>
                        
                        <p>Nous espérons vous revoir bientôt sur Your Car Your Way !</p>
                        
                        <a href="http://localhost:4200/search" class="btn">Nouvelle réservation</a>
                    </div>
                    <div class="footer">
                        <p>Your Car Your Way - Location de véhicules en Europe</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFirstName(),
                rental.getId(),
                departureAgency.getName(),
                departureAgency.getCity(),
                rental.getStartDate().format(dateFormatter),
                returnAgency.getName(),
                returnAgency.getCity(),
                rental.getEndDate().format(dateFormatter),
                rental.getCatCar(),
                refundAmount,
                refundPercentage,
                originalPrice,
                refundMessage
        );
    }
}
