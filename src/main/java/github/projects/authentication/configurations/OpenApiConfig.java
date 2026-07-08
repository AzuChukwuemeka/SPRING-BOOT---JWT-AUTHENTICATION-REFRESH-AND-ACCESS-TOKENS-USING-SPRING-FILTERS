package github.projects.authentication.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI authenticationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JWT Authentication API")
                        .description(
                                "Spring Boot service demonstrating stateless JWT authentication with access "
                                        + "and refresh tokens. Register a user, log in to receive an access "
                                        + "token (Authorization header) and a refresh token (HttpOnly cookie), "
                                        + "then click Authorize above and paste the access token to call the "
                                        + "protected endpoint.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
