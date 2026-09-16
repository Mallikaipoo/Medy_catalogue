package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubjectRepository extends JpaRepository<SubjectEntity, UUID> {
}
