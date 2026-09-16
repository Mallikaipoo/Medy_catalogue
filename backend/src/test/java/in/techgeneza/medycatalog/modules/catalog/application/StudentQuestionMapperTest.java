package in.techgeneza.medycatalog.modules.catalog.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentOption;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentQuestion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StudentQuestionMapperTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void studentPayloadNeverIncludesAnswerKey() throws Exception {
        StudentQuestion question = new StudentQuestion(
                UUID.randomUUID(),
                "The SI unit of force is:",
                "SINGLE_MCQ",
                false,
                false,
                "EASY",
                new BigDecimal("4"),
                new BigDecimal("1"),
                45,
                UUID.randomUUID(),
                List.of(new StudentOption(UUID.randomUUID(), "Newton", 2))
        );
        String json = mapper.writeValueAsString(question);
        assertThat(json).doesNotContain("isCorrect");
        assertThat(json).doesNotContain("correct");
        assertThat(json).doesNotContain("numericalAnswer");
        assertThat(json).contains("Newton");
        assertThat(json).contains("SINGLE_MCQ");
    }
}
