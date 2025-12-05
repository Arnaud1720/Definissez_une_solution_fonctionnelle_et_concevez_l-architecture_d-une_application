package com.arn.ycyw.your_car_your_way.config;

import com.stripe.exception.StripeException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:4200")
public class PaymentController {


    @Value("${stripe.frontend-url}")
    private String frontendUrl;

    @PostMapping("/create-checkout-session")
    public Map<String, String> createCheckoutSession() throws StripeException {

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("eur")
                                                        .setUnitAmount(1500L) // 15,00€ en centimes
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Test paiement backend")
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .setSuccessUrl(frontendUrl + "/paiement/success?session_id={CHECKOUT_SESSION_ID}")
                        .setCancelUrl(frontendUrl + "/paiement/cancel")
                        .build();

        Session session = Session.create(params);

        Map<String, String> response = new HashMap<>();
        response.put("url", session.getUrl());
        return response;
    }

}
