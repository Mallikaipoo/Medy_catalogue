package in.techgeneza.medycatalog.modules.learning.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserResumeRepository extends JpaRepository<UserResumeEntity, UUID> {
}
