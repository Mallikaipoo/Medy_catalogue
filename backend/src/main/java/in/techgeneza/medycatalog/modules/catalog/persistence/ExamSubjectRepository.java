package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExamSubjectRepository extends JpaRepository<ExamSubjectEntity, ExamSubjectEntity.Pk> {
    List<ExamSubjectEntity> findByExamIdOrderBySortOrderAsc(UUID examId);
}
