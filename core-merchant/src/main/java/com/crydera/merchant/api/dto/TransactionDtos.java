package com.crydera.merchant.api.dto;

import com.crydera.merchant.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TransactionDtos {
    private TransactionDtos() {}

    public record TransactionResponse(
            UUID id,
            UUID walletId,
            Transaction.Type type,
            Transaction.Status status,
            BigDecimal amount,
            String currency,
            String txHash,
            String counterparty,
            Instant createdAt,
            Instant confirmedAt
    ) {}

    public record TransactionListResponse(List<TransactionResponse> items) {}
}
