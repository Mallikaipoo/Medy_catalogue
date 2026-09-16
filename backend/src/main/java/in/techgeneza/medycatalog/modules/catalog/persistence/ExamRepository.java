package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<ExamEntity, UUID> {
    List<ExamEntity> findByStatusOrderByNameAsc(String status);
}
