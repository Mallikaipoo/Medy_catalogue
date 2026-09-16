package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionOptionRepository extends JpaRepository<QuestionOptionEntity, UUID> {
    List<QuestionOptionEntity> findByQuestionIdOrderByOptionOrderAsc(UUID questionId);

    List<QuestionOptionEntity> findByQuestionIdInOrderByOptionOrderAsc(List<UUID> questionIds);
}
