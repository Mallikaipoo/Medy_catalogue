package in.techgeneza.medycatalog.modules.billing.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.auth.application.TokenHasher;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.AdPlacement;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.AdsResponse;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.MeResponse;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.PlanCard;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.PriceCard;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.VerifyRequest;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.WebhookRequest;
import in.techgeneza.medycatalog.modules.billing.domain.AdsPlacementPolicy;
import in.techgeneza.medycatalog.modules.billing.domain.EntitlementSnapshot;
import in.techgeneza.medycatalog.modules.billing.domain.SubscriptionAccess;
import in.techgeneza.medycatalog.modules.billing.persistence.AdConfigEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.AdConfigRepository;
import in.techgeneza.medycatalog.modules.billing.persistence.FeatureFlagEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.FeatureFlagRepository;
import in.techgeneza.medycatalog.modules.billing.persistence.PlanPriceEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.PlanPriceRepository;
import in.techgeneza.medycatalog.modules.billing.persistence.StoreTransactionEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.StoreTransactionRepository;
import in.techgeneza.medycatalog.modules.billing.persistence.SubscriptionPlanEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.SubscriptionPlanRepository;
import in.techgeneza.medycatalog.modules.billing.persistence.UserSubscriptionEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.UserSubscriptionRepository;
import in.techgeneza.medycatalog.security.MedycatalogProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class SubscriptionService {

    private final SubscriptionPlanRepository plans;
    private final PlanPriceRepository prices;
    private final UserSubscriptionRepository subscriptions;
    private final StoreTransactionRepository transactions;
    private final AdConfigRepository ads;
    private final FeatureFlagRepository flags;
    private final EntitlementService entitlements;
    private final StoreVerifier storeVerifier;
    private final MedycatalogProperties properties;
    private final Clock clock;

    public SubscriptionService(
            SubscriptionPlanRepository plans,
            PlanPriceRepository prices,
            UserSubscriptionRepository subscriptions,
            StoreTransactionRepository transactions,
            AdConfigRepository ads,
            FeatureFlagRepository flags,
            EntitlementService entitlements,
            StoreVerifier storeVerifier,
            MedycatalogProperties properties,
            Clock clock
    ) {
        this.plans = plans;
        this.prices = prices;
        this.subscriptions = subscriptions;
        this.transactions = transactions;
        this.ads = ads;
        this.flags = flags;
        this.entitlements = entitlements;
        this.storeVerifier = storeVerifier;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PlanCard> plans() {
        List<PlanPriceEntity> activePrices = prices.findByActiveTrueOrderByHighlightedDescAmountAsc();
        return plans.findByActiveTrueOrderBySortOrderAsc().stream()
                .filter(plan -> EntitlementSnapshot.PREMIUM.equals(plan.getCode()))
                .map(plan -> toCard(plan, activePrices))
                .toList();
    }

    @Transactional(readOnly = true)
    public MeResponse me(UUID userId) {
        return new MeResponse(entitlements.snapshot(userId), plans());
    }

    @Transactional
    public MeResponse verify(UUID userId, VerifyRequest request) {
        String platform = normalizePlatform(request.platform());
        StoreVerifier.VerifiedPurchase verified = storeVerifier.verify(platform, request.productId(), request.purchaseToken());
        applyPurchase(userId, verified, request.purchaseToken(), "PURCHASE");
        return me(userId);
    }

    @Transactional
    public MeResponse restore(UUID userId) {
        return me(userId);
    }

    @Transactional
    public MeResponse grantRewarded(UUID userId) {
        entitlements.grantRewardedAttempt(userId);
        return me(userId);
    }

    @Transactional(readOnly = true)
    public AdsResponse ads(UUID userId, String platform) {
        EntitlementSnapshot snap = entitlements.snapshot(userId);
        boolean adsMaster = flags.findById("ads_enabled").map(FeatureFlagEntity::isEnabled).orElse(true);
        if (snap.adsOff() || !adsMaster) {
            return new AdsResponse(false, List.of());
        }
        String normalized = normalizePlatform(platform);
        List<AdPlacement> placements = ads.findByEnabledTrue().stream()
                .filter(row -> row.getPlatform().equals(normalized))
                .filter(row -> AdsPlacementPolicy.allowedOnStudentUi(row.getPlacement()))
                .sorted(Comparator.comparing(AdConfigEntity::getPlacement))
                .map(row -> new AdPlacement(row.getPlacement(), row.getUnitId()))
                .toList();
        return new AdsResponse(true, placements);
    }

    @Transactional
    public void handleWebhook(String platform, String signature, String rawBody, WebhookRequest request) {
        verifyWebhookSignature(platform, signature, rawBody);
        String normalized = normalizePlatform(platform == null ? request.platform() : platform);
        String tokenHash = TokenHasher.sha256(request.purchaseToken());
        UserSubscriptionEntity sub = subscriptions.findFirstByLatestPurchaseTokenHashOrderByUpdatedAtDesc(tokenHash)
                .orElse(null);
        if (sub == null) {
            return;
        }
        Instant now = clock.instant();
        String event = request.eventType() == null ? "" : request.eventType().toUpperCase(Locale.ROOT);
        switch (event) {
            case "RENEWED", "RECOVERED", "PURCHASED" -> {
                sub.setStatus(SubscriptionAccess.ACTIVE);
                if (request.expiryTime() != null) {
                    sub.setPeriodEnd(request.expiryTime());
                }
                sub.setGraceUntil(request.expiryTime() == null
                        ? sub.getPeriodEnd().plus(properties.billing().grace())
                        : request.expiryTime().plus(properties.billing().grace()));
            }
            case "IN_GRACE", "BILLING_RETRY" -> {
                sub.setStatus(SubscriptionAccess.GRACE.equals(event) || "IN_GRACE".equals(event)
                        ? SubscriptionAccess.GRACE
                        : SubscriptionAccess.BILLING_RETRY);
                Instant grace = request.expiryTime() == null
                        ? now.plus(properties.billing().grace())
                        : request.expiryTime();
                sub.setGraceUntil(grace);
            }
            case "CANCELED" -> {
                sub.setAutoRenew(false);
                sub.setStatus(SubscriptionAccess.CANCELED);
            }
            case "EXPIRED" -> {
                sub.setStatus(SubscriptionAccess.EXPIRED);
                sub.setGraceUntil(null);
            }
            case "REFUNDED", "REVOKED" -> {
                sub.setStatus(SubscriptionAccess.REVOKED);
                sub.setPeriodEnd(now);
                sub.setGraceUntil(null);
            }
            default -> {
                return;
            }
        }
        sub.setUpdatedAt(now);
        subscriptions.save(sub);
        recordTransaction(sub.getUserId(), sub.getId(), normalized, event, tokenHash, rawBody);
    }

    private void applyPurchase(UUID userId, StoreVerifier.VerifiedPurchase verified, String purchaseToken, String eventType) {
        PlanPriceEntity price = prices.findByStoreProductIdAndPlatformAndActiveTrue(verified.productId(), verified.platform())
                .orElseThrow(() -> ApiException.badRequest("UNKNOWN_PRODUCT", "That subscription product is not configured."));
        Instant now = clock.instant();
        String tokenHash = TokenHasher.sha256(purchaseToken);
        if (transactions.findByPlatformAndStoreTransactionId(verified.platform(), verified.transactionId()).isPresent()) {
            return;
        }
        UserSubscriptionEntity sub = subscriptions.findByUserIdOrderByPeriodEndDesc(userId).stream()
                .filter(row -> verified.platform().equals(row.getPlatform()))
                .findFirst()
                .orElseGet(UserSubscriptionEntity::new);
        sub.setUserId(userId);
        sub.setPlanId(price.getPlanId());
        sub.setStatus(SubscriptionAccess.ACTIVE);
        sub.setPlatform(verified.platform());
        sub.setStoreProductId(verified.productId());
        sub.setPeriodStart(now);
        sub.setPeriodEnd(verified.expiresAt());
        sub.setGraceUntil(verified.expiresAt().plus(properties.billing().grace()));
        sub.setAutoRenew(verified.autoRenew());
        sub.setLatestPurchaseTokenHash(tokenHash);
        subscriptions.save(sub);
        recordTransaction(userId, sub.getId(), verified.platform(), eventType, verified.transactionId(), tokenHash);
    }

    private void recordTransaction(
            UUID userId,
            UUID subscriptionId,
            String platform,
            String eventType,
            String storeTransactionId,
            String payload
    ) {
        if (transactions.findByPlatformAndStoreTransactionId(platform, storeTransactionId).isPresent()) {
            return;
        }
        StoreTransactionEntity row = new StoreTransactionEntity();
        row.setUserId(userId);
        row.setSubscriptionId(subscriptionId);
        row.setPlatform(platform);
        row.setEventType(eventType);
        row.setStoreTransactionId(storeTransactionId);
        row.setPurchaseTokenHash(storeTransactionId.length() == 64 ? storeTransactionId : TokenHasher.sha256(storeTransactionId));
        row.setPayload(payload);
        row.setVerifiedAt(clock.instant());
        transactions.save(row);
    }

    private PlanCard toCard(SubscriptionPlanEntity plan, List<PlanPriceEntity> activePrices) {
        List<PriceCard> priceCards = activePrices.stream()
                .filter(price -> price.getPlanId().equals(plan.getId()))
                .map(price -> new PriceCard(
                        price.getPlatform(),
                        price.getPeriod(),
                        price.getCurrency(),
                        price.getAmount(),
                        price.getStoreProductId(),
                        price.isHighlighted(),
                        "YEAR".equals(price.getPeriod())
                                ? "Best value — billed once a year, fewer interruptions"
                                : "Flexible month to month"
                ))
                .toList();
        return new PlanCard(plan.getCode(), plan.getName(), plan.getDescription(), plan.isHighlight(), priceCards);
    }

    private void verifyWebhookSignature(String platform, String signature, String rawBody) {
        String secret = "APP_STORE".equals(normalizePlatform(platform))
                ? properties.billing().appleNotificationSecret()
                : properties.billing().googleRtdnSecret();
        if (secret.isBlank()) {
            if (properties.billing().sandbox()) {
                return;
            }
            throw ApiException.unauthorized("Webhook signature is required.");
        }
        if (signature == null || signature.isBlank() || rawBody == null) {
            throw ApiException.unauthorized("Webhook signature is required.");
        }
        String expected = hmacSha256(secret, rawBody);
        if (!expected.equalsIgnoreCase(signature.replace("sha256=", ""))) {
            throw ApiException.unauthorized("Webhook signature is invalid.");
        }
    }

    private static String hmacSha256(String secret, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String normalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) {
            return "GOOGLE_PLAY";
        }
        String value = platform.trim().toUpperCase(Locale.ROOT);
        if (value.contains("IOS") || value.contains("APPLE")) {
            return "APP_STORE";
        }
        if (value.contains("ANDROID") || value.contains("GOOGLE")) {
            return "GOOGLE_PLAY";
        }
        return value;
    }
}
