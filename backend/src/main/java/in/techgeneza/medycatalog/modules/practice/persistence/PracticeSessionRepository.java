package in.techgeneza.medycatalog.modules.practice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PracticeSessionRepository extends JpaRepository<PracticeSessionEntity, UUID> {
}
