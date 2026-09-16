package in.techgeneza.medycatalog.modules.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanPriceRepository extends JpaRepository<PlanPriceEntity, UUID> {
    List<PlanPriceEntity> findByActiveTrueOrderByHighlightedDescAmountAsc();

    Optional<PlanPriceEntity> findByStoreProductIdAndPlatformAndActiveTrue(String storeProductId, String platform);
}
