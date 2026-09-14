package com.tejashvi.payment.dto;

import com.tejashvi.payment.domain.Payment;

import java.util.UUID;

public record PaymentResponse(

        UUID paymentId,
        String merchantId,
        String customerId,
        long amount,
        String currency,
        String status

) {

    public static PaymentResponse from(Payment payment) {

        return new PaymentResponse(
                payment.getId(),
                payment.getMerchantId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name()
        );
    }
}