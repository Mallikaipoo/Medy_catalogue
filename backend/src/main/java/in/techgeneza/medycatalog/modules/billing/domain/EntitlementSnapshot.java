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
        int practiceRemainingToday,
        Boolean fullQuestionBank,
        Boolean studyMaterial,
        Boolean audioExplain,
        Boolean doubtTutor,
        Boolean resumeLibrary
) {
    public static final String FREE = "FREE";
    public static final String PREMIUM = "PREMIUM";
    public static final String PLAYER_PLACEMENT = "PLAYER";

    public EntitlementSnapshot {
        if (fullQuestionBank == null) {
            fullQuestionBank = unlimitedPractice;
        }
        if (studyMaterial == null) {
            studyMaterial = detailedExplanations;
        }
        if (audioExplain == null) {
            audioExplain = true;
        }
        if (doubtTutor == null) {
            doubtTutor = unlimitedPractice;
        }
        if (resumeLibrary == null) {
            resumeLibrary = unlimitedPractice;
        }
    }

    public static EntitlementSnapshot freeDefaults() {
        return new EntitlementSnapshot(FREE, "Free", "NONE", false, false, false, true, 3, 0, 0, 3,
                false, false, true, false, false);
    }

    public boolean canStartWithoutCredit() {
        return unlimitedPractice || practiceRemainingToday > 0;
    }

    public boolean hasFullQuestionBank() {
        return Boolean.TRUE.equals(fullQuestionBank);
    }

    public boolean hasStudyMaterial() {
        return Boolean.TRUE.equals(studyMaterial);
    }

    public boolean hasAudioExplain() {
        return Boolean.TRUE.equals(audioExplain);
    }

    public boolean hasDoubtTutor() {
        return Boolean.TRUE.equals(doubtTutor);
    }

    public boolean hasResumeLibrary() {
        return Boolean.TRUE.equals(resumeLibrary);
    }

    public boolean premium() {
        return PREMIUM.equals(planCode);
    }
}
