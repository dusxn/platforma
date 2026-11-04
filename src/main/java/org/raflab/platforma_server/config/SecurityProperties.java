package org.raflab.platforma_server.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(String jwtSecret, Duration jwtDuration) {
}
