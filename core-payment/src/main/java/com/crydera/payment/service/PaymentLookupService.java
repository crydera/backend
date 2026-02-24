package com.crydera.payment.service;

import com.crydera.payment.api.dto.PaymentDtos;
import com.crydera.payment.domain.Payment;
import com.crydera.payment.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentLookupService {

    private final PaymentRepository paymentRepository;
    private final StatusCacheService statusCache;

    public PaymentDtos.PaymentDetailsResponse details(UUID id, Payment.Kind kind) {
        Payment p = load(id, kind);
        return new PaymentDtos.PaymentDetailsResponse(
                p.getId(), p.getNetwork(), p.getAddress(), p.getAmount(), p.getCurrency(),
                p.getStatus(), p.getExpiresAt(), p.getCreatedAt(), p.getPaidAt()
        );
    }

    public PaymentDtos.PaymentStatusResponse status(UUID id, Payment.Kind kind) {
        return statusCache.get(kind, id)
                .map(s -> new PaymentDtos.PaymentStatusResponse(id, s))
                .orElseGet(() -> {
                    Payment p = load(id, kind);
                    statusCache.put(kind, p.getId(), p.getStatus());
                    return new PaymentDtos.PaymentStatusResponse(p.getId(), p.getStatus());
                });
    }

    private Payment load(UUID id, Payment.Kind kind) {
        return paymentRepository.findByIdAndKind(id, kind)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        kind.name().toLowerCase() + " " + id + " not found"));
    }
}
