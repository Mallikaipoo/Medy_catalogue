package in.techgeneza.medycatalog.modules.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UUID> {
    List<UserSubscriptionEntity> findByUserIdOrderByPeriodEndDesc(UUID userId);

    Optional<UserSubscriptionEntity> findFirstByLatestPurchaseTokenHashOrderByUpdatedAtDesc(String tokenHash);
}
