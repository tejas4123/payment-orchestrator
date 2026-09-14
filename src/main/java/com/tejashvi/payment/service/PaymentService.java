package com.tejashvi.payment.service;

import com.tejashvi.payment.domain.Payment;
import com.tejashvi.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createPayment(
            String merchantId,
            String customerId,
            long amount,
            String currency,
            String idempotencyKey
    ) {

        Payment existingPayment =
                paymentRepository.findByIdempotencyKey(idempotencyKey)
                        .orElse(null);

        if (existingPayment != null) {
            return existingPayment;
        }

        Payment payment = new Payment(
                merchantId,
                customerId,
                amount,
                currency,
                idempotencyKey
        );

        return paymentRepository.save(payment);
    }
}