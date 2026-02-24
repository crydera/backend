package com.crydera.merchant.security;

import java.util.UUID;

public record AuthPrincipal(UUID merchantId, AuthSource source) {
    public enum AuthSource { JWT, API_KEY }
}
