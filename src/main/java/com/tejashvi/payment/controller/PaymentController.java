package com.tejashvi.payment.controller;

import com.tejashvi.payment.dto.CreatePaymentRequest;
import com.tejashvi.payment.dto.PaymentResponse;
import com.tejashvi.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(

            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreatePaymentRequest request
    ) {

        var payment = paymentService.createPayment(
                request.merchantId(),
                request.customerId(),
                request.amount(),
                request.currency(),
                idempotencyKey
        );

        return ResponseEntity.ok(
                PaymentResponse.from(payment)
        );
    }
}