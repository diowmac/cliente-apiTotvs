/**
 * Autor: Marco Ezequiel Cedro Barros Borges
 * Data: 9 de jun. de 2025
 */

package com.clienteapi.backend.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Minha API Cliente")
                .version("1.0")
                .description("Documentação da API Cliente"));
    }
}
