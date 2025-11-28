package com.arn.ycyw.your_car_your_way.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI yourCarYourWayApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Your Car Your Way API")
                        .description("API pour la gestion des locations de voitures")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Arnaud")
                                .email("arnaud1720@gmail.com")
                        )
                );
    }
}
