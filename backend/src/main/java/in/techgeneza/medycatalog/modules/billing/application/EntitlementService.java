package in.techgeneza.medycatalog.modules.billing.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.billing.domain.EntitlementSnapshot;
import in.techgeneza.medycatalog.modules.billing.domain.SubscriptionAccess;
import in.techgeneza.medycatalog.modules.billing.persistence.PlanEntitlementEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.PlanEntitlementRepository;
import in.techgeneza.medycatalog.modules.billing.persistence.SubscriptionPlanEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.SubscriptionPlanRepository;
import in.techgeneza.medycatalog.modules.billing.persistence.UserPracticeCreditEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.UserPracticeCreditRepository;
import in.techgeneza.medycatalog.modules.billing.persistence.UserSubscriptionEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.UserSubscriptionRepository;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EntitlementService {

    public static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    public static final int MAX_REWARDED_PER_DAY = 5;

    private final SubscriptionPlanRepository plans;
    private final PlanEntitlementRepository entitlements;
    private final UserSubscriptionRepository subscriptions;
    private final UserPracticeCreditRepository credits;
    private final PracticeSessionRepository sessions;
    private final Clock clock;

    public EntitlementService(
            SubscriptionPlanRepository plans,
            PlanEntitlementRepository entitlements,
            UserSubscriptionRepository subscriptions,
            UserPracticeCreditRepository credits,
            PracticeSessionRepository sessions,
            Clock clock
    ) {
        this.plans = plans;
        this.entitlements = entitlements;
        this.subscriptions = subscriptions;
        this.credits = credits;
        this.sessions = sessions;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EntitlementSnapshot snapshot(UUID userId) {
        Instant now = clock.instant();
        UserSubscriptionEntity current = currentEntitled(userId, now);
        SubscriptionPlanEntity plan = current == null
                ? plans.findByCode(EntitlementSnapshot.FREE).orElseThrow()
                : plans.findById(current.getPlanId()).orElseThrow();
        Map<String, PlanEntitlementEntity> byCode = entitlements.findByPlanId(plan.getId()).stream()
                .collect(Collectors.toMap(PlanEntitlementEntity::getCode, row -> row));
        boolean unlimited = bool(byCode, "unlimited_practice");
        int daily = intVal(byCode, "daily_practice_sessions", unlimited ? Integer.MAX_VALUE : 3);
        int started = (int) sessions.countStartedSince(userId, startOfIstDay(now));
        int extra = extraStarts(userId, now);
        int remaining = unlimited ? Integer.MAX_VALUE : Math.max(0, daily - started) + extra;
        String status = current == null ? "NONE" : current.getStatus();
        return new EntitlementSnapshot(
                plan.getCode(),
                plan.getName(),
                status,
                bool(byCode, "ads_off"),
                unlimited,
                bool(byCode, "detailed_explanations"),
                bool(byCode, "rewarded_extra_attempts"),
                unlimited ? 0 : daily,
                extra,
                started,
                remaining
        );
    }

    @Transactional
    public StartGrant authorizeStart(UUID userId) {
        EntitlementSnapshot snap = snapshot(userId);
        if (snap.unlimitedPractice() || snap.practiceRemainingToday() - snap.extraStartsRemaining() > 0) {
            return new StartGrant(snap, false);
        }
        if (snap.extraStartsRemaining() > 0) {
            consumeExtraStart(userId);
            return new StartGrant(snapshot(userId), true);
        }
        throw new ApiException(
                HttpStatus.FORBIDDEN,
                "DAILY_LIMIT",
                "You have used today's free practice. Watch a short ad for one more attempt, or continue with Premium — a test already in progress is never stopped."
        );
    }

    @Transactional
    public EntitlementSnapshot grantRewardedAttempt(UUID userId) {
        EntitlementSnapshot snap = snapshot(userId);
        if (snap.adsOff() || !snap.rewardedExtraAttempts()) {
            throw ApiException.badRequest("REWARD_NOT_NEEDED", "Premium already includes unlimited practice.");
        }
        Instant now = clock.instant();
        LocalDate today = LocalDate.ofInstant(now, IST);
        UserPracticeCreditEntity row = credits.findById(userId).orElseGet(() -> {
            UserPracticeCreditEntity created = new UserPracticeCreditEntity();
            created.setUserId(userId);
            return created;
        });
        if (row.getRewardedOn() == null || !today.equals(row.getRewardedOn())) {
            row.setRewardedOn(today);
            row.setRewardedToday(0);
        }
        if (row.getRewardedToday() >= MAX_REWARDED_PER_DAY) {
            throw ApiException.tooManyRequests("Extra attempts from ads reset tomorrow. Premium removes this limit.");
        }
        row.setRewardedToday(row.getRewardedToday() + 1);
        row.setExtraStarts(row.getExtraStarts() + 1);
        row.setUpdatedAt(now);
        credits.save(row);
        return snapshot(userId);
    }

    public UserSubscriptionEntity currentEntitled(UUID userId, Instant now) {
        List<UserSubscriptionEntity> rows = subscriptions.findByUserIdOrderByPeriodEndDesc(userId);
        for (UserSubscriptionEntity row : rows) {
            if (SubscriptionAccess.isEntitled(row.getStatus(), row.getPeriodEnd(), row.getGraceUntil(), now)) {
                return row;
            }
        }
        return null;
    }

    private void consumeExtraStart(UUID userId) {
        UserPracticeCreditEntity row = credits.findById(userId)
                .orElseThrow(() -> ApiException.forbidden("You have used today's free practice."));
        if (row.getExtraStarts() <= 0) {
            throw ApiException.forbidden("You have used today's free practice.");
        }
        row.setExtraStarts(row.getExtraStarts() - 1);
        row.setUpdatedAt(clock.instant());
        credits.save(row);
    }

    private int extraStarts(UUID userId, Instant now) {
        return credits.findById(userId)
                .map(row -> {
                    LocalDate today = LocalDate.ofInstant(now, IST);
                    if (row.getRewardedOn() != null && !today.equals(row.getRewardedOn()) && row.getExtraStarts() > 0) {
                        return row.getExtraStarts();
                    }
                    return row.getExtraStarts();
                })
                .orElse(0);
    }

    private Instant startOfIstDay(Instant now) {
        return LocalDate.ofInstant(now, IST).atStartOfDay(IST).toInstant();
    }

    private static boolean bool(Map<String, PlanEntitlementEntity> byCode, String code) {
        PlanEntitlementEntity row = byCode.get(code);
        return row != null && Boolean.TRUE.equals(row.getValueBool());
    }

    private static int intVal(Map<String, PlanEntitlementEntity> byCode, String code, int fallback) {
        PlanEntitlementEntity row = byCode.get(code);
        if (row == null || row.getValueInt() == null) {
            return fallback;
        }
        return row.getValueInt();
    }

    public record StartGrant(EntitlementSnapshot snapshot, boolean usedRewardedCredit) {
    }
}
