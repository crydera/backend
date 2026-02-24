package com.crydera.payment.api.common;

import com.crydera.merchant.security.CurrentMerchant;
import com.crydera.payment.api.dto.PaymentDtos;
import com.crydera.payment.service.PaymentService;
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
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(name = "Payment: Common")
@SecurityRequirement(name = "apiKeyAuth")
public class CommonPaymentController {

    private final PaymentService paymentService;

    @PostMapping("/invoice")
    @Operation(summary = "Создать invoice-платёж",
            description = "Возвращает ссылку на хостовую страницу оплаты Crydera.")
    public PaymentDtos.InvoiceCreatedResponse createInvoice(@Valid @RequestBody PaymentDtos.CreatePaymentRequest req) {
        return paymentService.createInvoice(CurrentMerchant.id(), req);
    }

    @PostMapping("/whitelabel")
    @Operation(summary = "Создать whitelabel-платёж",
            description = "Возвращает реквизиты платежа (адрес, сумма, сеть) для отображения на стороне мерчанта.")
    public PaymentDtos.WhitelabelCreatedResponse createWhitelabel(@Valid @RequestBody PaymentDtos.CreatePaymentRequest req) {
        return paymentService.createWhitelabel(CurrentMerchant.id(), req);
    }
}
