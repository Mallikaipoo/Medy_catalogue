package in.techgeneza.medycatalog.modules.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "store_transactions")
public class StoreTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(nullable = false, length = 32)
    private String platform;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "store_transaction_id", nullable = false, length = 191)
    private String storeTransactionId;

    @Column(name = "purchase_token_hash", nullable = false, length = 64)
    private String purchaseTokenHash;

    private String payload;

    @Column(name = "verified_at", nullable = false)
    private Instant verifiedAt = Instant.now();
}
