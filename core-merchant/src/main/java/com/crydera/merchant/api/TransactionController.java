package com.crydera.merchant.api;

import com.crydera.merchant.api.dto.TransactionDtos;
import com.crydera.merchant.domain.Transaction;
import com.crydera.merchant.security.CurrentMerchant;
import com.crydera.merchant.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant/transactions")
@RequiredArgsConstructor
@Tag(name = "Merchant: Transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "История транзакций мерчанта",
            description = "Опциональная фильтрация по типу через query-параметр `type` (INCOME / WITHDRAWAL).")
    public TransactionDtos.TransactionListResponse list(
            @Parameter(description = "Фильтр по типу: INCOME или WITHDRAWAL")
            @RequestParam(value = "type", required = false) Transaction.Type type
    ) {
        return transactionService.list(CurrentMerchant.id(), type);
    }
}
