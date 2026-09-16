package in.techgeneza.medycatalog.modules.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlagEntity, String> {
}
