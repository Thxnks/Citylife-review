package com.citylife.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI cityLifeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CityLife Review API")
                        .description("本地生活点评后端服务接口文档")
                        .version("1.0")
                        .contact(new Contact().name("XiaoHao")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/**")
                .build();
    }
}
