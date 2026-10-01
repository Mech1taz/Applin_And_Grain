package com.pedidos360.catalog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ms-pedidos360-catalog - Product Catalog API")
                        .description("Microservicio de Catálogo de Pedidos360 responsable del CRUD de productos y control de stock.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Arquitectura Pedidos360")
                                .email("soporte@pedidos360.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8088").description("Entorno de desarrollo local")
                ));
    }
}
