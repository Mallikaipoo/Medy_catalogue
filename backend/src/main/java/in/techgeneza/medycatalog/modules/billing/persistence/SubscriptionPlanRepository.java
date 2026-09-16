package in.techgeneza.medycatalog.modules.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlanEntity, UUID> {
    Optional<SubscriptionPlanEntity> findByCode(String code);

    List<SubscriptionPlanEntity> findByActiveTrueOrderBySortOrderAsc();
}
