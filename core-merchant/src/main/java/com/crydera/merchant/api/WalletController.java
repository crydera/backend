package com.crydera.merchant.api;

import com.crydera.merchant.api.dto.WalletDtos;
import com.crydera.merchant.security.CurrentMerchant;
import com.crydera.merchant.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchant/wallet")
@RequiredArgsConstructor
@Tag(name = "Merchant: Wallets")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @Operation(summary = "Создать кошелёк в выбранной блокчейн-сети")
    public WalletDtos.WalletResponse create(@Valid @RequestBody WalletDtos.CreateWalletRequest req) {
        return walletService.create(CurrentMerchant.id(), req);
    }

    @PutMapping
    @Operation(summary = "Переименовать кошелёк")
    public WalletDtos.WalletResponse rename(@Valid @RequestBody WalletDtos.RenameWalletRequest req) {
        return walletService.rename(CurrentMerchant.id(), req);
    }

    @GetMapping
    @Operation(summary = "Список кошельков мерчанта (включая балансы)")
    public WalletDtos.WalletListResponse list() {
        return walletService.list(CurrentMerchant.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Подробная информация о кошельке")
    public WalletDtos.WalletDetailsResponse details(@PathVariable UUID id) {
        return walletService.details(CurrentMerchant.id(), id);
    }

    @PostMapping("/withdrawal")
    @Operation(summary = "Инициировать вывод средств с кошелька")
    public WalletDtos.WithdrawalResponse withdraw(@Valid @RequestBody WalletDtos.WithdrawalRequest req) {
        return walletService.withdraw(CurrentMerchant.id(), req);
    }
}
