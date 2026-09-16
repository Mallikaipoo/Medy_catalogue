package in.techgeneza.medycatalog.modules.auth.application.social;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.security.MedycatalogProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAppleTokenVerifier implements SocialTokenVerifier {

    private final MedycatalogProperties properties;

    public GoogleAppleTokenVerifier(MedycatalogProperties properties) {
        this.properties = properties;
    }

    @Override
    public SocialProfile verifyGoogle(String idToken) {
        String clientId = properties.auth().googleClientId();
        if (clientId == null || clientId.isBlank()) {
            throw ApiException.unavailable(
                    "PROVIDER_NOT_CONFIGURED",
                    "Google sign-in is not available yet.");
        }
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw ApiException.unauthorized("Google sign-in could not be verified. Please try again.");
            }
            GoogleIdToken.Payload payload = token.getPayload();
            String name = payload.get("name") != null ? String.valueOf(payload.get("name")) : payload.getEmail();
            return new SocialProfile(payload.getSubject(), payload.getEmail(), name);
        } catch (GeneralSecurityException | IOException ex) {
            throw ApiException.unauthorized("Google sign-in could not be verified. Please try again.");
        }
    }

    @Override
    public SocialProfile verifyApple(String identityToken) {
        return AppleTokenParser.parse(identityToken, properties.auth().appleAudience());
    }
}
