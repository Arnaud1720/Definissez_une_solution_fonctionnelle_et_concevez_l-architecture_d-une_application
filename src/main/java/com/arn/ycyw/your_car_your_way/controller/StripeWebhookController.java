package com.arn.ycyw.your_car_your_way.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;

@RestController
@RequestMapping("/wcwy")
public class StripeWebhookController {
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;
    @PostMapping("/checkout")
    public String handleStripeWebhook(HttpServletRequest request,
                                      @RequestHeader("Stripe-Signature") String sigHeader) {

        String payload = getBody(request);

        Event event;
        try {
            event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    webhookSecret
            );
        } catch (Exception e) {
            System.out.println("⚠️ Signature Stripe invalide : " + e.getMessage());
            return "";
        }

        System.out.println("➡️ Event Stripe reçu : " + event.getType());

        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isPresent()) {
                Session session = (Session) deserializer.getObject().get();
                System.out.println(" Paiement réussi, session id = " + session.getId());

            }
        }

        return "";
    }

    private String getBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            System.out.println("Erreur lecture body webhook : " + e.getMessage());
        }
        return sb.toString();
    }

}
