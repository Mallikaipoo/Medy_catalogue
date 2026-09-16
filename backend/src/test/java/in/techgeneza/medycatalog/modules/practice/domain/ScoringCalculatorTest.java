package in.techgeneza.medycatalog.modules.practice.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringCalculatorTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final UUID C = UUID.fromString("00000000-0000-0000-0000-00000000000c");
    private static final BigDecimal MARKS = new BigDecimal("4");
    private static final BigDecimal NEG = new BigDecimal("1");

    @Test
    void singleMcqCorrectAwardsMarks() {
        var score = ScoringCalculator.score("SINGLE_MCQ", Set.of(B), Set.of(B), null, null, MARKS, NEG);
        assertThat(score.correct()).isTrue();
        assertThat(score.awardedMarks()).isEqualByComparingTo("4");
    }

    @Test
    void singleMcqWrongAppliesNegative() {
        var score = ScoringCalculator.score("SINGLE_MCQ", Set.of(A), Set.of(B), null, null, MARKS, NEG);
        assertThat(score.correct()).isFalse();
        assertThat(score.answered()).isTrue();
        assertThat(score.awardedMarks()).isEqualByComparingTo("-1");
    }

    @Test
    void skippedIsZero() {
        var score = ScoringCalculator.score("SINGLE_MCQ", Set.of(), Set.of(B), null, null, MARKS, NEG);
        assertThat(score.answered()).isFalse();
        assertThat(score.awardedMarks()).isEqualByComparingTo("0");
    }

    @Test
    void multiMcqRequiresExactSet() {
        var correct = ScoringCalculator.score("MULTI_MCQ", Set.of(A, C), Set.of(A, C), null, null, MARKS, NEG);
        var partial = ScoringCalculator.score("MULTI_MCQ", Set.of(A), Set.of(A, C), null, null, MARKS, NEG);
        assertThat(correct.correct()).isTrue();
        assertThat(partial.correct()).isFalse();
        assertThat(partial.awardedMarks()).isEqualByComparingTo("-1");
    }

    @Test
    void numericalMatchesCanonicalValue() {
        var score = ScoringCalculator.score("NUMERICAL", Set.of(), Set.of(), "6.0", "6", MARKS, NEG);
        assertThat(score.correct()).isTrue();
    }

    @Test
    void unknownTypeDoesNotAwardMarks() {
        var score = ScoringCalculator.score("ESSAY", Set.of(A), Set.of(A), null, null, MARKS, NEG);
        assertThat(score.correct()).isFalse();
        assertThat(score.awardedMarks()).isEqualByComparingTo("-1");
    }
}
