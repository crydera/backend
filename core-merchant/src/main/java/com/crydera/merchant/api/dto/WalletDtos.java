package com.crydera.merchant.api.dto;

import com.crydera.merchant.domain.Network;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WalletDtos {
    private WalletDtos() {}

    public record CreateWalletRequest(
            @NotNull Network network,
            String label
    ) {}

    public record RenameWalletRequest(
            @NotNull UUID id,
            @NotBlank String label
    ) {}

    public record WalletResponse(
            UUID id,
            Network network,
            String address,
            String label,
            BigDecimal balance,       
            String currency,
            Instant createdAt
    ) {}

    public record WalletListResponse(List<WalletResponse> wallets) {}

    public record WalletDetailsResponse(
            WalletResponse wallet,
            List<TransactionDtos.TransactionResponse> recentTransactions
    ) {}

    public record WithdrawalRequest(
            @NotNull UUID walletId,
            @NotBlank String toAddress,
            @NotNull @Positive BigDecimal amount
    ) {}

    public record WithdrawalResponse(UUID transactionId, String status) {}
}
