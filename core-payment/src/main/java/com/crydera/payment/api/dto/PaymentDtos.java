package com.crydera.payment.api.dto;

import com.crydera.merchant.domain.Network;
import com.crydera.payment.domain.Payment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class PaymentDtos {
    private PaymentDtos() {}

    public record CreatePaymentRequest(
            @NotNull Network network,
            @NotNull @Positive BigDecimal amount,
            @NotBlank String currency,
            String externalOrderId,
            String callbackUrl
    ) {}

    public record InvoiceCreatedResponse(
            UUID id,
            String invoiceUrl,    
            Network network,
            String address,       
            BigDecimal payAmount, 
            String currency,
            Instant expiresAt
    ) {}

    public record WhitelabelCreatedResponse(
            UUID id,
            Network network,
            String address,
            BigDecimal amount,
            String currency,
            Instant expiresAt
    ) {}

    public record PaymentDetailsResponse(
            UUID id,
            Network network,
            String address,
            BigDecimal amount,
            String currency,
            Payment.Status status,
            Instant expiresAt,
            Instant createdAt,
            Instant paidAt
    ) {}

    public record PaymentStatusResponse(UUID id, Payment.Status status) {}
}
