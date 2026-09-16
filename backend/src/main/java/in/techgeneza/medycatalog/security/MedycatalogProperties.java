package in.techgeneza.medycatalog.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "medycatalog")
public record MedycatalogProperties(
        Jwt jwt,
        Auth auth,
        Cors cors,
        Redis redis,
        Billing billing
) {
    public Billing billing() {
        return billing == null ? Billing.defaults() : billing;
    }

    public record Jwt(String secret, Duration accessTtl, Duration refreshTtl) {
    }

    public record Auth(boolean exposeOtp, String googleClientId, String appleAudience) {
    }

    public record Cors(String adminOrigin) {
    }

    public record Redis(boolean enabled) {
    }

    public record Billing(boolean sandbox, Duration grace, String googleRtdnSecret, String appleNotificationSecret) {
        public static Billing defaults() {
            return new Billing(true, Duration.ofDays(3), "", "");
        }

        public Duration grace() {
            return grace == null ? Duration.ofDays(3) : grace;
        }

        public String googleRtdnSecret() {
            return googleRtdnSecret == null ? "" : googleRtdnSecret;
        }

        public String appleNotificationSecret() {
            return appleNotificationSecret == null ? "" : appleNotificationSecret;
        }
    }
}
