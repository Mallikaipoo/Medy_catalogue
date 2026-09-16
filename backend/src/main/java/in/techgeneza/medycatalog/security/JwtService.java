package in.techgeneza.medycatalog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final MedycatalogProperties properties;
    private final Clock clock;

    public JwtService(MedycatalogProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        byte[] bytes = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("MEDYCATALOG_JWT_SECRET must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String issueAccessToken(UUID userId, List<String> roles) {
        Instant now = clock.instant();
        Instant exp = now.plus(properties.jwt().accessTtl());
        return Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long accessExpiresInSeconds() {
        return properties.jwt().accessTtl().toSeconds();
    }
}
