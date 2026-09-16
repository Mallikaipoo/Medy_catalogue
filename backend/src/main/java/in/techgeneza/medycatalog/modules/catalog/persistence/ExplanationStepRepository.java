package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExplanationStepRepository extends JpaRepository<ExplanationStepEntity, UUID> {
    List<ExplanationStepEntity> findByQuestionIdOrderByStepOrderAsc(UUID questionId);
}
