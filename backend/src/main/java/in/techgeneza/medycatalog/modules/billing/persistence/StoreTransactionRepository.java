package in.techgeneza.medycatalog.modules.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreTransactionRepository extends JpaRepository<StoreTransactionEntity, UUID> {
    Optional<StoreTransactionEntity> findByPlatformAndStoreTransactionId(String platform, String storeTransactionId);
}
