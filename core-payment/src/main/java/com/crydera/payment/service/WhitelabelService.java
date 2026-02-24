package com.crydera.payment.service;

import com.crydera.payment.api.dto.PaymentDtos;
import com.crydera.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WhitelabelService {

    private final PaymentLookupService lookup;

    public PaymentDtos.PaymentDetailsResponse details(UUID whitelabelId) {
        return lookup.details(whitelabelId, Payment.Kind.WHITELABEL);
    }

    public PaymentDtos.PaymentStatusResponse status(UUID whitelabelId) {
        return lookup.status(whitelabelId, Payment.Kind.WHITELABEL);
    }
}
