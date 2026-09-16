package in.techgeneza.medycatalog.modules.learning.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionVariantRepository extends JpaRepository<QuestionVariantEntity, UUID> {
    List<QuestionVariantEntity> findByQuestionIdOrderBySortOrderAsc(UUID questionId);
}
