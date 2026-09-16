package in.techgeneza.medycatalog.modules.auth.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrivacySettingsRepository extends JpaRepository<PrivacySettingsEntity, UUID> {
}
