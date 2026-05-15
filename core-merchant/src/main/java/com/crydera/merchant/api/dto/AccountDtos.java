package com.crydera.merchant.api.dto;

import java.time.Instant;
import java.util.UUID;

public final class AccountDtos {
    private AccountDtos() {}

    public record AccountResponse(
            UUID id,
            String email,
            String company,
            Instant createdAt
    ) {}
}
