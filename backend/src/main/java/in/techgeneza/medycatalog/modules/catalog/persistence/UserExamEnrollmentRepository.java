package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserExamEnrollmentRepository extends JpaRepository<UserExamEnrollmentEntity, UserExamEnrollmentEntity.Pk> {
    List<UserExamEnrollmentEntity> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
