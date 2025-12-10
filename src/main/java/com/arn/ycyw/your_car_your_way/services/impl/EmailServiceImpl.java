package com.arn.ycyw.your_car_your_way.services.impl;

import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.services.EmailService;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.FRANCE);

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
        System.out.println("➡️ Préparation de l'email de confirmation pour " + user.getEmail());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            System.out.println("fromEmail utilisé = " + fromEmail);

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("✅ Confirmation de réservation #" + rental.getId() + " - Your Car Your Way");

            String htmlContent = buildBookingConfirmationHtml(
                    user,
                    rental,
                    departureAgency,
                    returnAgency,
                    priceHT,
                    tvaAmount,
                    priceTTC
            );
            helper.setText(htmlContent, true);

            // Joindre la facture PDF
            String invoiceFileName = "Facture_YCYW_" + rental.getId() + ".pdf";
            DataSource pdfDataSource = new ByteArrayDataSource(invoicePdf, "application/pdf");
            helper.addAttachment(invoiceFileName, pdfDataSource);

            // ENVOI de l'email (une seule fois)
            mailSender.send(message);
            System.out.println("✅ Email envoyé à " + user.getEmail());

        } catch (Exception e) {
            System.out.println("❌ Erreur envoi email : " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void sendCancellationConfirmation(Users user, Rentals rental) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("❌ Annulation de réservation #" + rental.getId() + " - Your Car Your Way");

            String htmlContent = buildCancellationHtml(user, rental);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("📧 Email d'annulation envoyé à " + user.getEmail());

        } catch (MessagingException e) {
            System.out.println("❌ Erreur envoi email : " + e.getMessage());
        }
    }

    private String buildBookingConfirmationHtml(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            long priceHT,
            long tvaAmount,
            long priceTTC
    ) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background: white; }
                    .header { background: linear-gradient(135deg, #2563eb, #1e40af); color: white; padding: 40px 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .header p { margin: 10px 0 0; opacity: 0.9; }
                    .content { padding: 30px; }
                    .badge { display: inline-block; background: #10b981; color: white; padding: 6px 16px; border-radius: 20px; font-size: 14px; font-weight: 600; }
                    .card { background: #f9fafb; border-radius: 12px; padding: 20px; margin: 20px 0; border: 1px solid #e5e7eb; }
                    .card-title { font-size: 16px; font-weight: 600; color: #374151; margin-bottom: 15px; display: flex; align-items: center; gap: 8px; }
                    .row { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #e5e7eb; }
                    .row:last-child { border-bottom: none; }
                    .label { color: #6b7280; font-size: 14px; }
                    .value { font-weight: 600; color: #111827; text-align: right; }
                    .total-row { background: #2563eb; color: white; padding: 15px 20px; border-radius: 8px; display: flex; justify-content: space-between; margin-top: 20px; }
                    .total-row .label { color: rgba(255,255,255,0.9); }
                    .total-row .value { font-size: 24px; font-weight: 700; }
                    .info-box { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 8px; padding: 15px; margin: 20px 0; }
                    .info-box h4 { color: #1e40af; margin: 0 0 10px; }
                    .info-box ul { margin: 0; padding-left: 20px; color: #1e40af; }
                    .info-box li { margin: 5px 0; }
                    .footer { text-align: center; padding: 30px; color: #6b7280; font-size: 13px; background: #f9fafb; }
                    .attachment-note { background: #fef3c7; border: 1px solid #f59e0b; border-radius: 8px; padding: 15px; margin: 20px 0; text-align: center; }
                    .attachment-note strong { color: #92400e; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚗 Your Car Your Way</h1>
                        <p>Confirmation de réservation</p>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s %s</strong>,</p>
                        <p>Votre réservation a été confirmée avec succès ! <span class="badge">✓ Confirmée</span></p>

                        <div class="card">
                            <div class="card-title">📋 Réservation #%d</div>
                            <div class="row">
                                <span class="label">Catégorie de véhicule</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <div class="card">
                            <div class="card-title">🟢 Départ</div>
                            <div class="row">
                                <span class="label">Agence</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Ville</span>
                                <span class="value">%s, %s</span>
                            </div>
                            <div class="row">
                                <span class="label">Date et heure</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <div class="card">
                            <div class="card-title">🔴 Retour</div>
                            <div class="row">
                                <span class="label">Agence</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">Ville</span>
                                <span class="value">%s, %s</span>
                            </div>
                            <div class="row">
                                <span class="label">Date et heure</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <div class="card">
                            <div class="card-title">💰 Récapitulatif des montants</div>
                            <div class="row">
                                <span class="label">Montant HT</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="row">
                                <span class="label">TVA (20%%)</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <div class="total-row">
                            <span class="label">Total TTC payé</span>
                            <span class="value">%s</span>
                        </div>

                        <div class="attachment-note">
                            📎 <strong>Votre facture est jointe à cet email</strong> au format PDF.
                        </div>

                        <div class="info-box">
                            <h4>📌 Rappels importants</h4>
                            <ul>
                                <li>Présentez une pièce d'identité valide</li>
                                <li>Munissez-vous de votre permis de conduire</li>
                                <li>Annulation gratuite jusqu'à 48h avant le départ</li>
                                <li>Annulation à moins de 7 jours : remboursement à 25%%</li>
                            </ul>
                        </div>

                        <p style="text-align: center; margin-top: 30px;">Bonne route ! 🚗💨</p>
                    </div>
                    <div class="footer">
                        <p><strong>Your Car Your Way</strong> - Location de voitures en Europe</p>
                        <p>support@yourcaryourway.com | +33 1 23 45 67 89</p>
                        <p style="font-size: 11px; margin-top: 15px;">Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFirstName(),
                user.getLastName(),
                rental.getId(),
                getCategoryName(rental.getCatCar()),
                departureAgency.getName(),
                departureAgency.getCity(),
                departureAgency.getCountry(),
                rental.getStartDate().format(DATE_FORMATTER),
                returnAgency.getName(),
                returnAgency.getCity(),
                returnAgency.getCountry(),
                rental.getEndDate().format(DATE_FORMATTER),
                CURRENCY_FORMATTER.format(priceHT / 100.0),
                CURRENCY_FORMATTER.format(tvaAmount / 100.0),
                CURRENCY_FORMATTER.format(priceTTC / 100.0)
        );
    }

    private String buildCancellationHtml(Users user, Rentals rental) {
        double refundAmount = rental.getRefundPercentage() != null
                ? (rental.getPrice() / 100.0) * (rental.getRefundPercentage() / 100.0)
                : 0;

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #ef4444; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border: 1px solid #e5e7eb; }
                    .refund-box { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border: 1px solid #e5e7eb; text-align: center; }
                    .footer { text-align: center; padding: 20px; color: #6b7280; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Annulation confirmée</h1>
                        <p>Réservation #%d</p>
                    </div>
                    <div class="content">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Votre réservation a été annulée conformément à votre demande.</p>

                        <div class="refund-box">
                            <p>Taux de remboursement : <strong>%d%%</strong></p>
                            <p style="font-size: 28px; color: #10b981; font-weight: bold;">%s</p>
                        </div>

                        <p>Le remboursement sera crédité sur votre compte sous 5-10 jours ouvrés.</p>
                    </div>
                    <div class="footer">
                        <p>Your Car Your Way - Location de voitures</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                rental.getId(),
                user.getFirstName(),
                rental.getRefundPercentage() != null ? rental.getRefundPercentage() : 0,
                CURRENCY_FORMATTER.format(refundAmount)
        );
    }

    private String getCategoryName(String code) {
        return switch (code) {
            case "A" -> "Économique";
            case "B" -> "Compacte";
            case "C" -> "Berline";
            case "D" -> "SUV";
            case "E" -> "Premium";
            default -> code;
        };
    }
}
