package com.crydera.merchant.security;

import com.crydera.merchant.repo.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyHasher hasher;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String raw = req.getHeader(HEADER);
        if (raw != null && !raw.isBlank()) {
            apiKeyRepository.findByKeyHash(hasher.hash(raw))
                    .filter(k -> k.getRevokedAt() == null)
                    .ifPresent(k -> {
                        var principal = new AuthPrincipal(k.getMerchantId(), AuthPrincipal.AuthSource.API_KEY);
                        var auth = new UsernamePasswordAuthenticationToken(principal, raw, List.of());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }
        chain.doFilter(req, res);
    }
}
