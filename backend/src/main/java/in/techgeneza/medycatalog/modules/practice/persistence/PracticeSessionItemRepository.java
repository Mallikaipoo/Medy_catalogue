package in.techgeneza.medycatalog.modules.practice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PracticeSessionItemRepository extends JpaRepository<PracticeSessionItemEntity, UUID> {
    List<PracticeSessionItemEntity> findBySessionIdOrderByItemOrderAsc(UUID sessionId);
}
