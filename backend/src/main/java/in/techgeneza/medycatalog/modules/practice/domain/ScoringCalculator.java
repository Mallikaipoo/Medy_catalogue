package in.techgeneza.medycatalog.modules.practice.domain;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side marking. Never invoke this from Flutter for official scores.
 */
public final class ScoringCalculator {

    private ScoringCalculator() {
    }

    public record Score(boolean answered, boolean correct, BigDecimal awardedMarks) {
    }

    public static Score score(
            String typeCode,
            Set<UUID> selectedOptionIds,
            Set<UUID> correctOptionIds,
            String numericalSubmitted,
            String numericalCorrect,
            BigDecimal marks,
            BigDecimal negativeMarks
    ) {
        String type = typeCode == null ? "" : typeCode.toUpperCase(Locale.ROOT);
        boolean answered = isAnswered(type, selectedOptionIds, numericalSubmitted);
        if (!answered) {
            return new Score(false, false, BigDecimal.ZERO);
        }
        boolean correct = isCorrect(type, selectedOptionIds, correctOptionIds, numericalSubmitted, numericalCorrect);
        if (correct) {
            return new Score(true, true, marks);
        }
        BigDecimal penalty = negativeMarks == null ? BigDecimal.ZERO : negativeMarks.negate();
        return new Score(true, false, penalty);
    }

    private static boolean isAnswered(String type, Set<UUID> selected, String numerical) {
        if ("NUMERICAL".equals(type)) {
            return numerical != null && !numerical.isBlank();
        }
        return selected != null && !selected.isEmpty();
    }

    private static boolean isCorrect(
            String type,
            Set<UUID> selected,
            Set<UUID> correct,
            String numericalSubmitted,
            String numericalCorrect
    ) {
        return switch (type) {
            case "SINGLE_MCQ", "TRUE_FALSE" -> selected != null
                    && selected.size() == 1
                    && correct != null
                    && selected.equals(correct);
            case "MULTI_MCQ" -> selected != null && correct != null && !correct.isEmpty() && selected.equals(correct);
            case "NUMERICAL" -> numericalEquals(numericalSubmitted, numericalCorrect);
            default -> false;
        };
    }

    static boolean numericalEquals(String submitted, String expected) {
        if (submitted == null || expected == null) {
            return false;
        }
        try {
            BigDecimal left = new BigDecimal(submitted.trim());
            BigDecimal right = new BigDecimal(expected.trim());
            return left.compareTo(right) == 0;
        } catch (NumberFormatException ex) {
            return submitted.trim().equalsIgnoreCase(expected.trim());
        }
    }
}
