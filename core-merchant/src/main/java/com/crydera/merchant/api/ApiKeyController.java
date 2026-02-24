package com.crydera.merchant.api;

import com.crydera.merchant.api.dto.ApiKeyDtos;
import com.crydera.merchant.security.CurrentMerchant;
import com.crydera.merchant.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant/api-key")
@RequiredArgsConstructor
@Tag(name = "Merchant: API keys")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping
    @Operation(summary = "Выпустить новый API-ключ",
            description = "Plaintext-ключ возвращается ОДИН РАЗ в поле `key`. Сохраняется в БД только хеш.")
    public ApiKeyDtos.IssueResponse issue(@Valid @RequestBody(required = false) ApiKeyDtos.IssueRequest req) {
        if (req == null) req = new ApiKeyDtos.IssueRequest("default");
        return apiKeyService.issue(CurrentMerchant.id(), req);
    }
}
