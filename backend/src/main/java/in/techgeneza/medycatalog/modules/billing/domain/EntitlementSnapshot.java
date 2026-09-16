package in.techgeneza.medycatalog.modules.billing.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EntitlementSnapshot(
        String planCode,
        String planName,
        String status,
        boolean adsOff,
        boolean unlimitedPractice,
        boolean detailedExplanations,
        boolean rewardedExtraAttempts,
        int dailyPracticeSessions,
        int extraStartsRemaining,
        int practiceStartedToday,
        int practiceRemainingToday
) {
    public static final String FREE = "FREE";
    public static final String PREMIUM = "PREMIUM";
    public static final String PLAYER_PLACEMENT = "PLAYER";

    public static EntitlementSnapshot freeDefaults() {
        return new EntitlementSnapshot(FREE, "Free", "NONE", false, false, false, true, 3, 0, 0, 3);
    }

    public boolean canStartWithoutCredit() {
        return unlimitedPractice || practiceRemainingToday > 0;
    }
}
