package in.techgeneza.medycatalog.modules.billing.domain;

import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentQuestion;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.ReviewItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewEntitlementFilterTest {

    @Test
    void sessionSnapshotKeepsDetailedReviewAfterPlanLapses() {
        ReviewItem item = sample("Keep this", "Why others");
        EntitlementSnapshot startedPremium = new EntitlementSnapshot(
                EntitlementSnapshot.PREMIUM, "Premium", "ACTIVE", true, true, true, false, 0, 0, 0, 99,
                true, true, true, true, true);
        ReviewItem filtered = ReviewEntitlementFilter.apply(item, startedPremium);
        assertThat(filtered.detailedExplanation()).isEqualTo("Keep this");
        assertThat(filtered.whyOthersWrong()).isEqualTo("Why others");
    }

    @Test
    void freeSnapshotHidesDetailedExplanationOnly() {
        ReviewItem item = sample("Keep this", "Why others");
        ReviewItem filtered = ReviewEntitlementFilter.apply(item, EntitlementSnapshot.freeDefaults());
        assertThat(filtered.simpleExplanation()).isEqualTo("Simple");
        assertThat(filtered.detailedExplanation()).isNull();
        assertThat(filtered.whyOthersWrong()).isNull();
        assertThat(filtered.examTip()).isEqualTo("Tip");
    }

    @Test
    void freePreviewKeepsFullExplanation() {
        ReviewItem item = new ReviewItem(
                1,
                new StudentQuestion(
                        UUID.randomUUID(), "Q", "SINGLE_MCQ", false, false, "EASY",
                        BigDecimal.ONE, BigDecimal.ZERO, 30, UUID.randomUUID(), List.of()),
                List.of(), List.of(), null, null, true, BigDecimal.ONE,
                "Simple", "Keep this", "Why others", "Tip", List.of(), "EASY",
                true, "Trap", "Method", 2025);
        ReviewItem filtered = ReviewEntitlementFilter.apply(item, EntitlementSnapshot.freeDefaults());
        assertThat(filtered.detailedExplanation()).isEqualTo("Keep this");
        assertThat(filtered.methodScript()).isEqualTo("Method");
    }

    @Test
    void adsNeverAttachToPlayerOrSubmit() {
        assertThat(AdsPlacementPolicy.allowedOnStudentUi("HOME_BANNER")).isTrue();
        assertThat(AdsPlacementPolicy.allowedOnStudentUi("PLAYER")).isFalse();
        assertThat(AdsPlacementPolicy.allowedOnStudentUi("EXAM_PLAYER")).isFalse();
        assertThat(AdsPlacementPolicy.allowedOnStudentUi("SUBMIT")).isFalse();
    }

    private static ReviewItem sample(String detailed, String whyWrong) {
        StudentQuestion question = new StudentQuestion(
                UUID.randomUUID(), "Q", "SINGLE_MCQ", false, false, "EASY",
                BigDecimal.ONE, BigDecimal.ZERO, 30, UUID.randomUUID(), List.of());
        return new ReviewItem(
                1, question, List.of(), List.of(), null, null, true, BigDecimal.ONE,
                "Simple", detailed, whyWrong, "Tip", List.of(), "EASY", false, null, null, 2025);
    }
}
