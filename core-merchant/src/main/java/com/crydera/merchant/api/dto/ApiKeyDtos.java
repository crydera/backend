package com.crydera.merchant.api.dto;

import java.time.Instant;
import java.util.UUID;

public final class ApiKeyDtos {
    private ApiKeyDtos() {}

    public record IssueRequest(String label) {}

    public record IssueResponse(UUID id, String key, String label, Instant createdAt) {}
}
