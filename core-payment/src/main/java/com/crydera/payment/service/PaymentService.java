package com.crydera.payment.service;

import com.crydera.merchant.domain.Wallet;
import com.crydera.merchant.kafka.SidecarCommandPublisher;
import com.crydera.merchant.repo.WalletRepository;
import com.crydera.payment.api.dto.PaymentDtos;
import com.crydera.payment.domain.Payment;
import com.crydera.payment.repo.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final long DEFAULT_TTL_SECONDS = 900;   

    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final SidecarCommandPublisher commandPublisher;
    private final StatusCacheService statusCache;
    private final ObjectMapper objectMapper;

    @Value("${crydera.invoice.public-base-url:https://pay.crydera.io/invoice}")
    private String invoiceBaseUrl;

    @Transactional
    public PaymentDtos.InvoiceCreatedResponse createInvoice(UUID merchantId, PaymentDtos.CreatePaymentRequest req) {
        Payment p = persist(merchantId, req, Payment.Kind.INVOICE);
        publishCreatePaymentCommand(p);
        statusCache.put(Payment.Kind.INVOICE, p.getId(), p.getStatus());
        return new PaymentDtos.InvoiceCreatedResponse(
                p.getId(),
                invoiceBaseUrl + "/?key=" + p.getId(),
                p.getNetwork(),
                p.getAddress(),
                p.getAmount(),
                p.getCurrency(),
                p.getExpiresAt()
        );
    }

    @Transactional
    public PaymentDtos.WhitelabelCreatedResponse createWhitelabel(UUID merchantId, PaymentDtos.CreatePaymentRequest req) {
        Payment p = persist(merchantId, req, Payment.Kind.WHITELABEL);
        publishCreatePaymentCommand(p);
        statusCache.put(Payment.Kind.WHITELABEL, p.getId(), p.getStatus());
        return new PaymentDtos.WhitelabelCreatedResponse(
                p.getId(),
                p.getNetwork(),
                p.getAddress(),
                p.getAmount(),
                p.getCurrency(),
                p.getExpiresAt()
        );
    }

    private Payment persist(UUID merchantId, PaymentDtos.CreatePaymentRequest req, Payment.Kind kind) {
        
        Wallet wallet = pickWallet(merchantId, req);

        UUID paymentId = UUID.randomUUID();
        BigDecimal payAmount = applyAmountNonce(req.amount(), paymentId);

        Instant expiresAt = Instant.now().plusSeconds(DEFAULT_TTL_SECONDS);
        Payment p = Payment.builder()
                .id(paymentId)
                .merchantId(merchantId)
                .kind(kind)
                .network(req.network())
                .address(wallet.getAddress())
                .amount(payAmount)
                .currency(req.currency())
                .status(Payment.Status.WAITING)
                .externalOrderId(req.externalOrderId())
                .callbackUrl(req.callbackUrl())
                .expiresAt(expiresAt)
                .build();
        return paymentRepository.save(p);
    }

    private Wallet pickWallet(UUID merchantId, PaymentDtos.CreatePaymentRequest req) {
        List<Wallet> wallets = walletRepository.findByMerchantId(merchantId).stream()
                .filter(w -> w.getNetwork() == req.network())
                .toList();
        if (wallets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "merchant has no wallet for network " + req.network() +
                            ", create one via POST /api/v1/merchant/wallet first");
        }
        return wallets.get(0);
    }

    private static BigDecimal applyAmountNonce(BigDecimal requested, UUID paymentId) {
        long tail = paymentId.getLeastSignificantBits() & 0xFFFFL;          
        BigDecimal delta = new BigDecimal(tail).movePointLeft(6);
        return requested.add(delta);
    }

    private void publishCreatePaymentCommand(Payment p) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("type", "CREATE_PAYMENT");
        envelope.put("commandId", UUID.randomUUID().toString());
        envelope.put("paymentId", p.getId().toString());
        envelope.put("network", p.getNetwork().name());
        envelope.put("payload", Map.of(
                "kind", p.getKind().name(),
                "address", p.getAddress(),
                "amount", p.getAmount().toPlainString(),
                "currency", p.getCurrency(),
                "ttlSeconds", DEFAULT_TTL_SECONDS
        ));
        try {
            commandPublisher.send(p.getNetwork(), objectMapper.writeValueAsString(envelope));
        } catch (JsonProcessingException e) {
            log.error("failed to serialize CREATE_PAYMENT command for {}", p.getId(), e);
        }
    }
}
