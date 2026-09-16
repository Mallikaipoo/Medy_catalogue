package in.techgeneza.medycatalog.modules.auth.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHasherTest {

    @Test
    void sha256IsStableAndNotPlaintext() {
        String hash = TokenHasher.sha256("123456");
        assertThat(hash).hasSize(64);
        assertThat(hash).isNotEqualTo("123456");
        assertThat(TokenHasher.sha256("123456")).isEqualTo(hash);
    }

    @Test
    void sixDigitCodeHasLeadingZerosWhenNeeded() {
        String code = TokenHasher.sixDigitCode();
        assertThat(code).hasSize(6);
        assertThat(code).matches("\\d{6}");
    }

    @Test
    void randomTokensDiffer() {
        assertThat(TokenHasher.randomToken()).isNotEqualTo(TokenHasher.randomToken());
        assertThat(TokenHasher.randomToken()).hasSize(64);
    }
}
