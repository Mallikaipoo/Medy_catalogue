package in.techgeneza.medycatalog.modules.billing.application;

import java.time.Instant;

public interface StoreVerifier {

    VerifiedPurchase verify(String platform, String productId, String purchaseToken);

    record VerifiedPurchase(
            String platform,
            String productId,
            String transactionId,
            Instant expiresAt,
            boolean autoRenew
    ) {
    }
}
