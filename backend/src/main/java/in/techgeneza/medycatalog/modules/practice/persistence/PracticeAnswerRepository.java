package in.techgeneza.medycatalog.modules.practice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PracticeAnswerRepository extends JpaRepository<PracticeAnswerEntity, UUID> {
    Optional<PracticeAnswerEntity> findBySessionIdAndQuestionId(UUID sessionId, UUID questionId);

    List<PracticeAnswerEntity> findBySessionId(UUID sessionId);
}
