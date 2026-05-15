package com.crydera.merchant.api;

import com.crydera.merchant.api.dto.AccountDtos;
import com.crydera.merchant.security.CurrentMerchant;
import com.crydera.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant/account")
@RequiredArgsConstructor
@Tag(name = "Merchant: Account")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final MerchantService merchantService;

    @GetMapping
    @Operation(summary = "Информация об аккаунте текущего мерчанта")
    public AccountDtos.AccountResponse account() {
        return merchantService.getAccount(CurrentMerchant.id());
    }
}
