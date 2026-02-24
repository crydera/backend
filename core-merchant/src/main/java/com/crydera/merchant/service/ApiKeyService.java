package com.crydera.merchant.service;

import com.crydera.merchant.api.dto.ApiKeyDtos;
import com.crydera.merchant.domain.ApiKey;
import com.crydera.merchant.repo.ApiKeyRepository;
import com.crydera.merchant.security.ApiKeyHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private static final SecureRandom RNG = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyHasher hasher;

    @Transactional
    public ApiKeyDtos.IssueResponse issue(UUID merchantId, ApiKeyDtos.IssueRequest req) {
        byte[] raw = new byte[24];
        RNG.nextBytes(raw);
        String plaintext = "crydera_" + Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        ApiKey row = ApiKey.builder()
                .merchantId(merchantId)
                .keyHash(hasher.hash(plaintext))
                .label(req.label())
                .build();
        row = apiKeyRepository.save(row);

        return new ApiKeyDtos.IssueResponse(row.getId(), plaintext, row.getLabel(), row.getCreatedAt());
    }
}
