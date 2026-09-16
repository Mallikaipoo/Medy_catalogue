package in.techgeneza.medycatalog.modules.practice.api.dto;

import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentQuestion;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class PracticeDtos {

    private PracticeDtos() {
    }

    public record StartRequest(
            @NotNull UUID examId,
            UUID subjectId,
            UUID chapterId,
            UUID topicId,
            String difficulty,
            @Min(1) @Max(100) int questionCount,
            Integer durationSeconds
    ) {
    }

    public record PreviewResponse(long available, int requested) {
    }

    public record AnswerRequest(
            @NotNull UUID questionId,
            List<UUID> optionIds,
            String numericalAnswer,
            Boolean markedForReview,
            Boolean clear
    ) {
    }

    public record SessionItem(
            int order,
            StudentQuestion question,
            List<UUID> selectedOptionIds,
            String numericalAnswer,
            boolean markedForReview
    ) {
    }

    public record SessionResponse(
            UUID id,
            String status,
            long remainingSeconds,
            int questionCount,
            List<SessionItem> items
    ) {
    }

    public record ResultResponse(
            UUID sessionId,
            BigDecimal score,
            BigDecimal totalMarks,
            int correct,
            int wrong,
            int skipped,
            int timeTakenSeconds,
            double accuracyPercent,
            double averageSecondsPerQuestion
    ) {
    }

    public record ReviewItem(
            int order,
            StudentQuestion question,
            List<UUID> selectedOptionIds,
            List<UUID> correctOptionIds,
            String numericalAnswer,
            String numericalCorrect,
            boolean correct,
            BigDecimal awardedMarks,
            String simpleExplanation,
            String detailedExplanation,
            String whyOthersWrong,
            String examTip,
            List<Step> steps,
            String difficulty,
            boolean freePreview,
            String trapWording,
            String methodScript,
            Integer examYear
    ) {
    }

    public record Step(int order, String title, String body) {
    }

    public record ReviewResponse(UUID sessionId, ResultResponse result, List<ReviewItem> items) {
    }
}
