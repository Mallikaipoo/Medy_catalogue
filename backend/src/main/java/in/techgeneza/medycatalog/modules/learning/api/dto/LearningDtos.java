package in.techgeneza.medycatalog.modules.learning.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class LearningDtos {

    private LearningDtos() {
    }

    public record LocaleCard(String code, String englishName, String nativeName, String ttsCode, boolean classical) {
    }

    public record VariantCard(String variantText, String trapNote) {
    }

    public record PreviewOption(UUID id, String text, int order, boolean correct) {
    }

    public record FreePreview(
            UUID questionId,
            UUID examId,
            String examCode,
            int examYear,
            String questionText,
            List<PreviewOption> options,
            String simpleExplanation,
            String detailedExplanation,
            String methodScript,
            String trapWording,
            String spokenScript,
            String ttsCode,
            List<VariantCard> variants,
            String topicName,
            int topicHitCount,
            String focusLabel
    ) {
    }

    public record ResumeState(String route, String title, UUID examId, String payload, Instant updatedAt) {
    }

    public record ResumeRequest(String route, String title, UUID examId, String payload) {
    }

    public record DoubtRequest(String text, String locale, String source, UUID examId, UUID topicId, UUID questionId) {
    }

    public record DoubtResponse(
            UUID id,
            String locale,
            String ttsCode,
            String queryText,
            String answerShort,
            String spokenScript,
            String topicName,
            String focusLabel
    ) {
    }

    public record SavedAnswer(
            UUID sessionId,
            UUID questionId,
            String questionText,
            boolean correct,
            String explanation,
            String spokenScript,
            Instant at
    ) {
    }

    public record LibraryResponse(List<SavedAnswer> answers, List<DoubtResponse> queries) {
    }
}
