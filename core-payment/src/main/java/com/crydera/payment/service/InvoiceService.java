package com.crydera.payment.service;

import com.crydera.payment.api.dto.PaymentDtos;
import com.crydera.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final PaymentLookupService lookup;

    public PaymentDtos.PaymentDetailsResponse details(UUID invoiceId) {
        return lookup.details(invoiceId, Payment.Kind.INVOICE);
    }

    public PaymentDtos.PaymentStatusResponse status(UUID invoiceId) {
        return lookup.status(invoiceId, Payment.Kind.INVOICE);
    }
}
