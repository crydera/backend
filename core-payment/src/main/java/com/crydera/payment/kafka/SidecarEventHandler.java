package com.crydera.payment.kafka;

import com.crydera.merchant.domain.Transaction;
import com.crydera.merchant.domain.Wallet;
import com.crydera.merchant.repo.TransactionRepository;
import com.crydera.merchant.repo.WalletRepository;
import com.crydera.payment.domain.Payment;
import com.crydera.payment.domain.ProcessedSidecarEvent;
import com.crydera.payment.repo.PaymentRepository;
import com.crydera.payment.repo.ProcessedSidecarEventRepository;
import com.crydera.payment.service.StatusCacheService;
import com.crydera.payment.service.WebhookDispatcher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SidecarEventHandler {

    private final ObjectMapper objectMapper;
    private final ProcessedSidecarEventRepository processedRepo;
    private final PaymentRepository paymentRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final StatusCacheService statusCache;
    private final WebhookDispatcher webhookDispatcher;

    @KafkaListener(topics = "sidecar.events.tron", groupId = "crydera-core")
    public void onTronEvent(String raw) {
        handle(raw);
    }

    @Transactional
    public void handle(String raw) {
        JsonNode env;
        try {
            env = objectMapper.readTree(raw);
        } catch (Exception e) {
            log.warn("malformed sidecar event payload: {}", e.getMessage());
            return;
        }
        UUID eventId = parseUuid(env.path("eventId").asText(null));
        if (eventId == null) {
            log.warn("event missing eventId, skipping: {}", raw);
            return;
        }
        if (processedRepo.existsByEventId(eventId)) {
            return;
        }

        String type = env.path("type").asText("");
        UUID paymentId = parseUuid(env.path("paymentId").asText(null));
        JsonNode payload = env.path("payload");

        switch (type) {
            case "PAYMENT_CREATED" -> applyAddressUpdate(paymentId, payload);
            case "PAYMENT_DETECTED" -> applyDetected(paymentId, payload);
            case "PAYMENT_CONFIRMED" -> applyConfirmed(paymentId, payload);
            case "PAYMENT_EXPIRED" -> applyExpired(paymentId);
            case "WITHDRAWAL_BROADCAST" -> applyWithdrawalBroadcast(paymentId, payload);
            case "WITHDRAWAL_CONFIRMED" -> applyWithdrawalConfirmed(paymentId, payload);
            default -> log.info("ignoring unknown event type: {}", type);
        }

        processedRepo.save(ProcessedSidecarEvent.builder()
                .eventId(eventId)
                .eventType(type)
                .paymentId(paymentId)
                .build());
    }

    private void applyAddressUpdate(UUID paymentId, JsonNode payload) {
        if (paymentId == null) return;
        paymentRepository.findById(paymentId).ifPresent(p -> {
            String addr = payload.path("address").asText(null);
            if (addr != null && !addr.isBlank() && !addr.equals(p.getAddress())) {
                p.setAddress(addr);
                paymentRepository.save(p);
            }
        });
    }

    private void applyDetected(UUID paymentId, JsonNode payload) {
        if (paymentId == null) return;
        paymentRepository.findById(paymentId).ifPresent(p -> {
            p.setStatus(Payment.Status.WAITING);
            p.setDetectedTxHash(payload.path("txHash").asText(null));
            Integer confs = payload.has("confirmations") ? payload.get("confirmations").asInt() : 0;
            p.setConfirmations(confs);
            paymentRepository.save(p);
            statusCache.put(p.getKind(), p.getId(), p.getStatus());
        });
    }

    private void applyConfirmed(UUID paymentId, JsonNode payload) {
        if (paymentId == null) return;
        paymentRepository.findById(paymentId).ifPresent(p -> {
            p.setStatus(Payment.Status.PAID);
            p.setPaidAt(Instant.now());
            String txHash = payload.path("txHash").asText(p.getDetectedTxHash());
            p.setDetectedTxHash(txHash);
            p.setConfirmations(payload.has("confirmations") ? payload.get("confirmations").asInt() : null);
            paymentRepository.save(p);
            statusCache.put(p.getKind(), p.getId(), p.getStatus());
            recordIncomeTransaction(p, payload);
            webhookDispatcher.dispatch(p);
        });
    }

    private void applyExpired(UUID paymentId) {
        if (paymentId == null) return;
        paymentRepository.findById(paymentId).ifPresent(p -> {
            if (p.getStatus() != Payment.Status.PAID) {
                p.setStatus(Payment.Status.EXPIRED);
                paymentRepository.save(p);
                statusCache.put(p.getKind(), p.getId(), p.getStatus());
                webhookDispatcher.dispatch(p);
            }
        });
    }

    private void applyWithdrawalBroadcast(UUID txOrPaymentId, JsonNode payload) {
        if (txOrPaymentId == null) return;
        transactionRepository.findById(txOrPaymentId).ifPresent(t -> {
            t.setStatus(Transaction.Status.PENDING);
            t.setTxHash(payload.path("txHash").asText(t.getTxHash()));
            transactionRepository.save(t);
        });
    }

    private void applyWithdrawalConfirmed(UUID txOrPaymentId, JsonNode payload) {
        if (txOrPaymentId == null) return;
        transactionRepository.findById(txOrPaymentId).ifPresent(t -> {
            t.setStatus(Transaction.Status.CONFIRMED);
            t.setConfirmedAt(Instant.now());
            t.setTxHash(payload.path("txHash").asText(t.getTxHash()));
            transactionRepository.save(t);
        });
    }

    private void recordIncomeTransaction(Payment p, JsonNode payload) {
        Optional<Wallet> walletOpt = walletRepository.findFirstByMerchantIdAndAddress(p.getMerchantId(), p.getAddress());
        if (walletOpt.isEmpty()) {
            log.debug("no wallet found for payment {} address {} — skipping tx record", p.getId(), p.getAddress());
            return;
        }
        Wallet w = walletOpt.get();
        BigDecimal amount = parseAmount(payload.path("amount").asText(null), p.getAmount());
        Transaction tx = Transaction.builder()
                .walletId(w.getId())
                .type(Transaction.Type.INCOME)
                .status(Transaction.Status.CONFIRMED)
                .amount(amount)
                .currency(p.getCurrency())
                .txHash(p.getDetectedTxHash())
                .paymentId(p.getId())
                .confirmedAt(Instant.now())
                .build();
        transactionRepository.save(tx);
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) return null;
        try { return UUID.fromString(value); } catch (IllegalArgumentException e) { return null; }
    }

    private static BigDecimal parseAmount(String raw, BigDecimal fallback) {
        if (raw == null || raw.isBlank()) return fallback;
        try { return new BigDecimal(raw); } catch (NumberFormatException e) { return fallback; }
    }

}
