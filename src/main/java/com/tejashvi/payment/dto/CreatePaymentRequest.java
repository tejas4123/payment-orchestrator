package com.tejashvi.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(

        @NotBlank
        String merchantId,

        @NotBlank
        String customerId,

        @Positive
        long amount,

        @NotBlank
        String currency

) {
}