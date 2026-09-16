package in.techgeneza.medycatalog.modules.learning.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppLocaleTest {

    @Test
    void shipsFourteenIndianLocalesIncludingElevenClassical() {
        assertThat(AppLocale.supported()).hasSize(14);
        long classical = AppLocale.supported().stream().filter(AppLocale::classical).count();
        assertThat(classical).isEqualTo(11);
        assertThat(AppLocale.fromCode("hi")).isEqualTo(AppLocale.HI);
        assertThat(AppLocale.fromCode("pra").ttsCode()).isEqualTo("sa-IN");
    }
}
