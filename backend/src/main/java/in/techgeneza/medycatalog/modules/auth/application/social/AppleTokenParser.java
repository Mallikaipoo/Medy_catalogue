package in.techgeneza.medycatalog.modules.auth.application.social;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import in.techgeneza.medycatalog.common.exception.ApiException;

import java.net.URI;

final class AppleTokenParser {

    private static final String APPLE_KEYS = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISS = "https://appleid.apple.com";

    private AppleTokenParser() {
    }

    static SocialProfile parse(String identityToken, String audience) {
        if (audience == null || audience.isBlank()) {
            throw ApiException.unavailable(
                    "PROVIDER_NOT_CONFIGURED",
                    "Apple sign-in is not available yet.");
        }
        try {
            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(URI.create(APPLE_KEYS).toURL());
            JWSKeySelector<SecurityContext> selector =
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
            processor.setJWSKeySelector(selector);
            JWTClaimsSet claims = processor.process(identityToken, null);
            if (!APPLE_ISS.equals(claims.getIssuer()) || !claims.getAudience().contains(audience)) {
                throw ApiException.unauthorized("Apple sign-in could not be verified. Please try again.");
            }
            String email = claims.getStringClaim("email");
            return new SocialProfile(claims.getSubject(), email, email);
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.unauthorized("Apple sign-in could not be verified. Please try again.");
        }
    }
}
