package in.techgeneza.medycatalog.modules.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdConfigRepository extends JpaRepository<AdConfigEntity, UUID> {
    List<AdConfigEntity> findByEnabledTrue();
}
