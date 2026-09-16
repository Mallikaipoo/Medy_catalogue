package in.techgeneza.medycatalog.modules.learning.application;

import in.techgeneza.medycatalog.modules.learning.domain.AppLocale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpokenScriptServiceTest {

    private final SpokenScriptService spoken = new SpokenScriptService();

    @Test
    void wrapsHindiIntroBeforeTheMethod() {
        String script = spoken.wrap(AppLocale.HI, "Like charges repel.");
        assertThat(script).startsWith("धीरे सुनिए");
        assertThat(script).contains("Like charges repel.");
    }

    @Test
    void focusCopyHighlightsOneOrTwoHits() {
        assertThat(LearningService.focusLabel(1)).contains("1 time");
        assertThat(LearningService.focusLabel(2)).contains("1–2 times");
    }
}
