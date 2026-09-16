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
            int sortOrder,
            String keyPoints,
            int examHitCount,
            String focusLabel,
            String patternNote
    ) {
    }

    public record TopicNotes(
            UUID id,
            String name,
            int syllabusYear,
            String keyPoints,
            String detailedExplanation,
            long publishedQuestions,
            int examHitCount,
            String focusLabel,
            String patternNote,
            String spokenScript,
            boolean studyMaterialLocked
    ) {
    }

    public record SyllabusTopic(
            UUID id,
            String name,
            String keyPoints,
            long publishedQuestions,
            int examHitCount,
            String focusLabel
    ) {
    }

    public record SyllabusChapter(
            UUID id,
            String name,
            String subjectCode,
            String subjectName,
            List<SyllabusTopic> topics
    ) {
    }

    public record ExamSyllabus(
            UUID examId,
            String examCode,
            String examName,
            int syllabusYear,
            String coverageNote,
            List<SyllabusChapter> chapters
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
