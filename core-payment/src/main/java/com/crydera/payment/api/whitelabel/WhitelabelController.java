package com.crydera.payment.api.whitelabel;

import com.crydera.payment.api.dto.PaymentDtos;
import com.crydera.payment.service.WhitelabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/whitelabel")
@RequiredArgsConstructor
@Tag(name = "Payment: Whitelabel")
public class WhitelabelController {

    private final WhitelabelService whitelabelService;

    @GetMapping("/{id}")
    @Operation(summary = "Информация о whitelabel-платеже")
    public PaymentDtos.PaymentDetailsResponse details(@PathVariable UUID id) {
        return whitelabelService.details(id);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Текущий статус whitelabel-платежа", description = "Используется фронтендом для поллинга каждые 5 секунд.")
    public PaymentDtos.PaymentStatusResponse status(@PathVariable UUID id) {
        return whitelabelService.status(id);
    }
}
