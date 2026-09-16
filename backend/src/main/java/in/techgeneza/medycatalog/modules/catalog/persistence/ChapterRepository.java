package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChapterRepository extends JpaRepository<ChapterEntity, UUID> {
    List<ChapterEntity> findByExamIdAndSubjectIdOrderBySortOrderAsc(UUID examId, UUID subjectId);

    List<ChapterEntity> findByExamIdOrderBySortOrderAsc(UUID examId);
}
