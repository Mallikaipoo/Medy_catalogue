package in.techgeneza.medycatalog.modules.billing.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.billing.persistence.PlanPriceEntity;
import in.techgeneza.medycatalog.modules.billing.persistence.PlanPriceRepository;
import in.techgeneza.medycatalog.modules.auth.application.TokenHasher;
import in.techgeneza.medycatalog.security.MedycatalogProperties;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class SandboxStoreVerifier implements StoreVerifier {

    private final PlanPriceRepository prices;
    private final MedycatalogProperties properties;
    private final Clock clock;

    public SandboxStoreVerifier(PlanPriceRepository prices, MedycatalogProperties properties, Clock clock) {
        this.prices = prices;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public VerifiedPurchase verify(String platform, String productId, String purchaseToken) {
        if (!properties.billing().sandbox()) {
            throw ApiException.unavailable(
                    "BILLING_UNAVAILABLE",
                    "Store verification is not live yet. You can keep practising on the free plan.");
        }
        if (purchaseToken == null || purchaseToken.isBlank()) {
            throw ApiException.badRequest("INVALID_PURCHASE", "That purchase could not be verified.");
        }
        PlanPriceEntity price = prices.findByStoreProductIdAndPlatformAndActiveTrue(productId, platform)
                .orElseThrow(() -> ApiException.badRequest("UNKNOWN_PRODUCT", "That subscription product is not configured."));
        Duration length = "YEAR".equals(price.getPeriod()) ? Duration.ofDays(365) : Duration.ofDays(30);
        Instant now = clock.instant();
        return new VerifiedPurchase(
                platform,
                productId,
                TokenHasher.sha256(platform + ":" + purchaseToken),
                now.plus(length),
                true
        );
    }
}
