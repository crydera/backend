package com.crydera.payment.api.invoice;

import com.crydera.payment.api.dto.PaymentDtos;
import com.crydera.payment.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
@Tag(name = "Payment: Invoice")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping("/{id}")
    @Operation(summary = "Информация о платеже-invoice (адрес, сеть, сумма, статус)")
    public PaymentDtos.PaymentDetailsResponse details(@PathVariable UUID id) {
        return invoiceService.details(id);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Текущий статус платежа", description = "Используется фронтендом для поллинга каждые 5 секунд.")
    public PaymentDtos.PaymentStatusResponse status(@PathVariable UUID id) {
        return invoiceService.status(id);
    }
}
