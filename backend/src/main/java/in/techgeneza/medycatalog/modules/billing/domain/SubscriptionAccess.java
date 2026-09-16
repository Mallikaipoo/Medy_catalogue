package in.techgeneza.medycatalog.modules.billing.domain;

import java.time.Instant;

public final class SubscriptionAccess {

    public static final String ACTIVE = "ACTIVE";
    public static final String GRACE = "GRACE";
    public static final String BILLING_RETRY = "BILLING_RETRY";
    public static final String EXPIRED = "EXPIRED";
    public static final String CANCELED = "CANCELED";
    public static final String REVOKED = "REVOKED";

    private SubscriptionAccess() {
    }

    public static boolean isEntitled(String status, Instant periodEnd, Instant graceUntil, Instant now) {
        if (status == null || REVOKED.equals(status)) {
            return false;
        }
        if (EXPIRED.equals(status)) {
            return false;
        }
        if (CANCELED.equals(status)) {
            return periodEnd != null && now.isBefore(periodEnd);
        }
        if (ACTIVE.equals(status) || GRACE.equals(status) || BILLING_RETRY.equals(status)) {
            if (periodEnd != null && now.isBefore(periodEnd)) {
                return true;
            }
            return graceOpen(graceUntil, now);
        }
        return false;
    }

    private static boolean graceOpen(Instant graceUntil, Instant now) {
        return graceUntil != null && now.isBefore(graceUntil);
    }
}
