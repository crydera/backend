package com.crydera.merchant.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class CurrentMerchant {
    private CurrentMerchant() {}

    public static UUID id() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthPrincipal ap) return ap.merchantId();
        throw new IllegalStateException("No authenticated merchant in context");
    }
}
