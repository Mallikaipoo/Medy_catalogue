package in.techgeneza.medycatalog.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "medycatalog")
public record MedycatalogProperties(
        Jwt jwt,
        Auth auth,
        Cors cors,
        Redis redis
) {
    public record Jwt(String secret, Duration accessTtl, Duration refreshTtl) {
    }

    public record Auth(boolean exposeOtp, String googleClientId, String appleAudience) {
    }

    public record Cors(String adminOrigin) {
    }

    public record Redis(boolean enabled) {
    }
}
