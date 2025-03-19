package com.kardio.config.database;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
	// Configuration to enable JPA Auditing
	// (@CreatedDate and @LastModifiedDate will be automatically updated)
}
