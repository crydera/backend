package com.crydera.merchant.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${crydera.openapi.public-url:http://localhost:8080}")
    private String publicUrl;

    @Bean
    public OpenAPI cryderaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Crydera Core API")
                        .version("v1")
                        .description("")
                        .contact(new Contact().name("Crydera Backend").email("backend@crydera.io")))
                .servers(List.of(new Server().url(publicUrl).description("local dev")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT, выдаваемый POST /api/v1/merchant/login"))
                        .addSecuritySchemes("apiKeyAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API-ключ мерчанта, выдаётся POST /api/v1/merchant/api-key")))
                .tags(List.of(
                        new Tag().name("Merchant: Auth").description("Регистрация, вход, выход, смена пароля"),
                        new Tag().name("Merchant: Account").description("Информация об аккаунте мерчанта"),
                        new Tag().name("Merchant: API keys").description("Выпуск API-ключей для Payment API"),
                        new Tag().name("Merchant: Wallets").description("Управление кошельками"),
                        new Tag().name("Merchant: Transactions").description("История транзакций"),
                        new Tag().name("Payment: Common").description("Создание invoice / whitelabel платежей"),
                        new Tag().name("Payment: Invoice").description("Информация и поллинг статуса invoice"),
                        new Tag().name("Payment: Whitelabel").description("Информация и поллинг статуса whitelabel")
                ));
    }
}
