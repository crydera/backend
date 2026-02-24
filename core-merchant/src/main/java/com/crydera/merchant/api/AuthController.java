package com.crydera.merchant.api;

import com.crydera.merchant.api.dto.AuthDtos;
import com.crydera.merchant.security.CurrentMerchant;
import com.crydera.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchant")
@RequiredArgsConstructor
@Tag(name = "Merchant: Auth")
public class AuthController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового мерчанта", description = "Возвращает JWT-токен.")
    public AuthDtos.TokenResponse register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
        return merchantService.register(req);
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в аккаунт", description = "Возвращает JWT-токен.")
    public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
        return merchantService.login(req);
    }

    @PostMapping("/pass")
    @Operation(summary = "Смена пароля", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> changePassword(@Valid @RequestBody AuthDtos.ChangePasswordRequest req) {
        merchantService.changePassword(CurrentMerchant.id(), req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход из аккаунта", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String auth) {
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
        merchantService.logout(CurrentMerchant.id(), token);
        return ResponseEntity.noContent().build();
    }
}
