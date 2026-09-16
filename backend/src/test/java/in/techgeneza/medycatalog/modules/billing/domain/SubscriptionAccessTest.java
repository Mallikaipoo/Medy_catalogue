package in.techgeneza.medycatalog.modules.billing.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionAccessTest {

    private final Instant now = Instant.parse("2026-09-16T12:00:00Z");

    @Test
    void activeBeforePeriodEndStaysEntitled() {
        assertThat(SubscriptionAccess.isEntitled(
                SubscriptionAccess.ACTIVE, now.plusSeconds(60), now.plusSeconds(3600), now)).isTrue();
    }

    @Test
    void afterPeriodEndGraceStillEntitled() {
        assertThat(SubscriptionAccess.isEntitled(
                SubscriptionAccess.BILLING_RETRY, now.minusSeconds(10), now.plusSeconds(3600), now)).isTrue();
    }

    @Test
    void expiredWithoutGraceIsNotEntitled() {
        assertThat(SubscriptionAccess.isEntitled(
                SubscriptionAccess.EXPIRED, now.minusSeconds(10), null, now)).isFalse();
    }

    @Test
    void canceledStillEntitledUntilPaidPeriodEnds() {
        assertThat(SubscriptionAccess.isEntitled(
                SubscriptionAccess.CANCELED, now.plusSeconds(86400), null, now)).isTrue();
    }

    @Test
    void refundRevokesImmediately() {
        assertThat(SubscriptionAccess.isEntitled(
                SubscriptionAccess.REVOKED, now.plusSeconds(86400), now.plusSeconds(86400), now)).isFalse();
    }
}
