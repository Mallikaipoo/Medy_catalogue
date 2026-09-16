package in.techgeneza.medycatalog.modules.catalog.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CatalogDtos {

    private CatalogDtos() {
    }

    public record SubjectCard(
            UUID id,
            String code,
            String name,
            long publishedQuestions,
            int sortOrder
    ) {
    }

    public record ChapterCard(
            UUID id,
            String name,
            long publishedQuestions,
            int sortOrder
    ) {
    }

    public record TopicCard(
            UUID id,
            String name,
            long publishedQuestions,
            int sortOrder
    ) {
    }

    public record StudentOption(UUID id, String text, int order) {
    }

    public record StudentQuestion(
            UUID id,
            String questionText,
            String questionType,
            boolean allowsMultipleOptions,
            boolean requiresNumerical,
            String difficulty,
            BigDecimal marks,
            BigDecimal negativeMarks,
            int estimatedTimeSeconds,
            UUID topicId,
            List<StudentOption> options
    ) {
    }

    public record HomeResponse(
            String studentName,
            UUID examId,
            String examCode,
            String examName,
            List<SubjectCard> subjects
    ) {
    }
}
