package in.techgeneza.medycatalog.modules.billing.api.dto;

import in.techgeneza.medycatalog.modules.billing.domain.EntitlementSnapshot;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class BillingDtos {

    private BillingDtos() {
    }

    public record PriceCard(
            String platform,
            String period,
            String currency,
            BigDecimal amount,
            String storeProductId,
            boolean highlighted,
            String operationalNote
    ) {
    }

    public record PlanCard(
            String code,
            String name,
            String description,
            boolean highlight,
            List<PriceCard> prices
    ) {
    }

    public record MeResponse(EntitlementSnapshot entitlement, List<PlanCard> plans) {
    }

    public record VerifyRequest(
            @NotBlank String platform,
            @NotBlank String productId,
            @NotBlank String purchaseToken
    ) {
    }

    public record WebhookRequest(
            String platform,
            String eventType,
            String productId,
            String purchaseToken,
            Instant expiryTime
    ) {
    }

    public record AdPlacement(String placement, String unitId) {
    }

    public record AdsResponse(boolean showAds, List<AdPlacement> placements) {
    }
}
