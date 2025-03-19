package com.kardio.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Minimal Swagger/OpenAPI configuration for API documentation. Most
 * configuration is handled via application.properties using springdoc.*
 * properties.
 */
@Configuration
public class SwaggerConfig {

	/**
	 * Creates a minimal OpenAPI bean for Swagger documentation with JWT security.
	 * The rest of configuration (title, description, etc.) is in
	 * application.properties.
	 */
	@Bean
	public OpenAPI openAPI() {
		final String securitySchemeName = "bearerAuth";

		return new OpenAPI().addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
						.name(securitySchemeName).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
	}
}
