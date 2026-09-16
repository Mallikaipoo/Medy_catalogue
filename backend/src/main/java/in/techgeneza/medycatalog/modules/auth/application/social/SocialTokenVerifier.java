package in.techgeneza.medycatalog.modules.auth.application.social;

public interface SocialTokenVerifier {
    SocialProfile verifyGoogle(String idToken);

    SocialProfile verifyApple(String identityToken);
}
