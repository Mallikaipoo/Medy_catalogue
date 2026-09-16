package in.techgeneza.medycatalog.modules.learning.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LearningQueryRepository extends JpaRepository<LearningQueryEntity, UUID> {
    List<LearningQueryEntity> findTop50ByUserIdOrderByCreatedAtDesc(UUID userId);
}
