package com.crydera.payment.service;

import com.crydera.payment.domain.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookDispatcher {

    private final RestClient restClient = RestClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    private final ObjectMapper objectMapper;

    @Async("webhookExecutor")
    public void dispatch(Payment p) {
        if (p.getCallbackUrl() == null || p.getCallbackUrl().isBlank()) return;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("paymentId", p.getId().toString());
        body.put("kind", p.getKind().name());
        body.put("status", p.getStatus().name());
        body.put("network", p.getNetwork().name());
        body.put("address", p.getAddress());
        body.put("amount", p.getAmount() == null ? null : p.getAmount().toPlainString());
        body.put("currency", p.getCurrency());
        body.put("txHash", p.getDetectedTxHash());
        body.put("externalOrderId", p.getExternalOrderId());

        try {
            String payload = objectMapper.writeValueAsString(body);
            restClient.post()
                    .uri(p.getCallbackUrl())
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (JsonProcessingException e) {
            log.error("webhook payload serialization failed for {}", p.getId(), e);
        } catch (RestClientException e) {
            log.warn("webhook delivery failed for {} -> {}: {}", p.getId(), p.getCallbackUrl(), e.getMessage());
        }
    }

}
