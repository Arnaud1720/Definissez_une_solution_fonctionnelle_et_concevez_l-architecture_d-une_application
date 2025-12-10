package com.arn.ycyw.your_car_your_way.services;

import com.arn.ycyw.your_car_your_way.entity.Agency;
import com.arn.ycyw.your_car_your_way.entity.Rentals;
import com.arn.ycyw.your_car_your_way.entity.Users;

public interface EmailService {

    /**
     * Envoie un email de confirmation de réservation avec la facture PDF en pièce jointe
     */
    void sendBookingConfirmationWithInvoice(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            long priceHT,
            long tvaAmount,
            long priceTTC,
            byte[] invoicePdf
    );

    /**
     * Envoie un email de confirmation d'annulation de réservation
     */
    void sendCancellationConfirmation(
            Users user,
            Rentals rental,
            Agency departureAgency,
            Agency returnAgency,
            int refundPercentage
    );
}
