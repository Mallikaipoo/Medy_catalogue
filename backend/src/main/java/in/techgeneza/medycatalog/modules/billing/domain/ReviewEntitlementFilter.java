package in.techgeneza.medycatalog.modules.billing.domain;

import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.ReviewItem;

public final class ReviewEntitlementFilter {

    private ReviewEntitlementFilter() {
    }

    public static ReviewItem apply(ReviewItem item, EntitlementSnapshot snapshot) {
        if (snapshot == null || snapshot.detailedExplanations() || item.freePreview()) {
            return item;
        }
        return new ReviewItem(
                item.order(),
                item.question(),
                item.selectedOptionIds(),
                item.correctOptionIds(),
                item.numericalAnswer(),
                item.numericalCorrect(),
                item.correct(),
                item.awardedMarks(),
                item.simpleExplanation(),
                null,
                null,
                item.examTip(),
                item.steps(),
                item.difficulty(),
                item.freePreview(),
                item.trapWording(),
                null,
                item.examYear()
        );
    }
}
