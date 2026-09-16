package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionExplanationRepository extends JpaRepository<QuestionExplanationEntity, UUID> {
    Optional<QuestionExplanationEntity> findByQuestionId(UUID questionId);
}
