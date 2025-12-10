package com.arn.ycyw.your_car_your_way.controller;

import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Status;
import com.arn.ycyw.your_car_your_way.entity.Users;
import com.arn.ycyw.your_car_your_way.reposiory.AgencyRepository;
import com.arn.ycyw.your_car_your_way.reposiory.RentalRepository;
import com.arn.ycyw.your_car_your_way.reposiory.UserRepository;
import com.arn.ycyw.your_car_your_way.services.EmailService;
import com.arn.ycyw.your_car_your_way.services.InvoiceService;
import com.stripe.param.checkout.SessionRetrieveParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/wcwy")
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private AgencyRepository agencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private InvoiceService invoiceService;

    @PostMapping("/checkout")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        System.out.println("=== WEBHOOK REÇU ===");
        System.out.println("Payload length = " + payload.length());

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            System.out.println("✅ Signature validée ! Event type = " + event.getType());
        } catch (Exception e) {
            System.out.println("❌ Erreur signature : " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            System.out.println("🎯 C'est un checkout.session.completed !");

            try {
                // Extraire l'ID de la session depuis le payload JSON
                String sessionId = extractSessionIdFromPayload(payload);
                System.out.println("📋 Session ID extrait : " + sessionId);

                if (sessionId != null) {
                    // Récupérer la session complète via l'API Stripe
                    SessionRetrieveParams params = SessionRetrieveParams.builder()
                            .addExpand("line_items")
                            .build();
                    Session session = Session.retrieve(sessionId, params, null);
                    System.out.println("✅ Session récupérée via API !");
                    System.out.println("📧 Customer email : " + session.getCustomerEmail());

                    handleSuccessfulPayment(session);
                } else {
                    System.out.println("❌ Impossible d'extraire l'ID de session");
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur lors de la récupération de la session : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⏭️ Event ignoré : " + event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }

    /**
     * Extrait l'ID de la session depuis le payload JSON brut
     */
    private String extractSessionIdFromPayload(String payload) {
        try {
            // Chercher "id": "cs_test_..." ou "id": "cs_live_..."
            int idIndex = payload.indexOf("\"id\":");
            if (idIndex == -1) return null;

            // Trouver le début de la valeur
            int valueStart = payload.indexOf("\"", idIndex + 5) + 1;
            int valueEnd = payload.indexOf("\"", valueStart);

            String id = payload.substring(valueStart, valueEnd);

            // Vérifier que c'est bien un ID de session checkout
            if (id.startsWith("cs_")) {
                return id;
            }

            // Sinon chercher dans l'objet data
            int csIndex = payload.indexOf("\"cs_");
            if (csIndex != -1) {
                int start = csIndex + 1;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }

            return null;
        } catch (Exception e) {
            System.out.println("Erreur extraction session ID : " + e.getMessage());
            return null;
        }
    }

    private void handleSuccessfulPayment(Session session) {
        try {
            Map<String, String> metadata = session.getMetadata();
            System.out.println("📦 Métadonnées reçues : " + metadata);

            if (metadata == null || metadata.isEmpty()) {
                System.out.println("❌ Pas de métadonnées dans la session !");
                return;
            }

            // Récupérer les données des métadonnées
            String catCar = metadata.get("catCar");
            String startDateStr = metadata.get("startDate");
            String endDateStr = metadata.get("endDate");
            String priceHTStr = metadata.get("priceHT");
            String tvaAmountStr = metadata.get("tvaAmount");
            String priceTTCStr = metadata.get("priceTTC");
            String departureAgencyIdStr = metadata.get("departureAgencyId");
            String returnAgencyIdStr = metadata.get("returnAgencyId");
            String userEmail = metadata.get("userEmail");

            System.out.println("📋 Données extraites :");
            System.out.println("   - catCar: " + catCar);
            System.out.println("   - startDate: " + startDateStr);
            System.out.println("   - endDate: " + endDateStr);
            System.out.println("   - priceHT: " + priceHTStr);
            System.out.println("   - priceTTC: " + priceTTCStr);
            System.out.println("   - departureAgencyId: " + departureAgencyIdStr);
            System.out.println("   - returnAgencyId: " + returnAgencyIdStr);
            System.out.println("   - userEmail: " + userEmail);

            // Vérifier que toutes les données sont présentes
            if (catCar == null || startDateStr == null || endDateStr == null ||
                    priceHTStr == null || departureAgencyIdStr == null ||
                    returnAgencyIdStr == null || userEmail == null) {
                System.out.println("❌ Données manquantes dans les métadonnées !");
                return;
            }

            long priceHT = Long.parseLong(priceHTStr);
            long tvaAmount = Long.parseLong(tvaAmountStr);
            long priceTTC = Long.parseLong(priceTTCStr);
            Integer departureAgencyId = Integer.parseInt(departureAgencyIdStr);
            Integer returnAgencyId = Integer.parseInt(returnAgencyIdStr);

            // Parser les dates
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime startDate = LocalDateTime.parse(startDateStr, formatter);
            LocalDateTime endDate = LocalDateTime.parse(endDateStr, formatter);

            // Récupérer les entités
            Optional<Users> userOpt = userRepository.findByEmail(userEmail);
            Optional<Agency> departureAgencyOpt = agencyRepository.findById(departureAgencyId);
            Optional<Agency> returnAgencyOpt = agencyRepository.findById(returnAgencyId);

            if (userOpt.isEmpty() || departureAgencyOpt.isEmpty() || returnAgencyOpt.isEmpty()) {
                System.out.println("❌ Données manquantes pour créer le rental");
                System.out.println("   User found: " + userOpt.isPresent());
                System.out.println("   Departure Agency found: " + departureAgencyOpt.isPresent());
                System.out.println("   Return Agency found: " + returnAgencyOpt.isPresent());
                return;
            }

            Users user = userOpt.get();
            Agency departureAgency = departureAgencyOpt.get();
            Agency returnAgency = returnAgencyOpt.get();

            // Créer le Rental avec le prix TTC
            Rentals rental = new Rentals();
            rental.setCatCar(catCar);
            rental.setStartDate(startDate);
            rental.setEndDate(endDate);
            rental.setPrice((int) priceTTC);
            rental.setStatus(Status.BOOKED);
            rental.setDepartureAgency(departureAgency);
            rental.setReturnAgency(returnAgency);
            rental.setUser(user);

            Rentals savedRental = rentalRepository.save(rental);

            System.out.println("✅ Rental créé avec succès : ID = " + savedRental.getId());
            System.out.println("   Prix HT: " + priceHT + " centimes");
            System.out.println("   TVA: " + tvaAmount + " centimes");
            System.out.println("   Prix TTC: " + priceTTC + " centimes");

            // Générer la facture PDF
            byte[] invoicePdf = invoiceService.generateInvoicePdf(
                    user,
                    savedRental,
                    departureAgency,
                    returnAgency,
                    priceHT,
                    tvaAmount,
                    priceTTC
            );

            System.out.println("📄 Facture PDF générée : " + invoicePdf.length + " bytes");

            // Envoyer l'email de confirmation avec la facture
            emailService.sendBookingConfirmationWithInvoice(
                    user,
                    savedRental,
                    departureAgency,
                    returnAgency,
                    priceHT,
                    tvaAmount,
                    priceTTC,
                    invoicePdf
            );

            System.out.println("📧 Email de confirmation avec facture envoyé à " + user.getEmail());

        } catch (Exception e) {
            System.out.println("❌ Erreur lors du traitement du paiement : " + e.getMessage());
            e.printStackTrace();
        }
    }


}
