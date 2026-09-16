package in.techgeneza.medycatalog.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            new MedycatalogProperties(
                    new MedycatalogProperties.Jwt(
                            "test-only-secret-key-must-be-at-least-32b",
                            Duration.ofMinutes(15),
                            Duration.ofDays(30)
                    ),
                    new MedycatalogProperties.Auth(true, "google-client", "apple-aud"),
                    new MedycatalogProperties.Cors("http://localhost:3000"),
                    new MedycatalogProperties.Redis(false),
                    MedycatalogProperties.Billing.defaults()
            ),
            Clock.systemUTC()
    );

    @Test
    void issuesSignedAccessTokenWithRoles() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.issueAccessToken(userId, List.of("STUDENT"));
        Claims claims = jwtService.parse(token);
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("roles", List.class)).contains("STUDENT");
        assertThat(jwtService.accessExpiresInSeconds()).isEqualTo(900);
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.issueAccessToken(UUID.randomUUID(), List.of("STUDENT"));
        assertThatThrownBy(() -> jwtService.parse(token + "x"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void requiresLongSecret() {
        MedycatalogProperties shortSecret = new MedycatalogProperties(
                new MedycatalogProperties.Jwt("short", Duration.ofMinutes(15), Duration.ofDays(30)),
                new MedycatalogProperties.Auth(false, "", ""),
                new MedycatalogProperties.Cors("http://localhost:3000"),
                new MedycatalogProperties.Redis(false),
                MedycatalogProperties.Billing.defaults()
        );
        assertThatThrownBy(() -> new JwtService(shortSecret, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class);
    }
}
