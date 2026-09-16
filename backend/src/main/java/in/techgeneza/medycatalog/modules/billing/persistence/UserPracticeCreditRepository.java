package in.techgeneza.medycatalog.modules.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserPracticeCreditRepository extends JpaRepository<UserPracticeCreditEntity, UUID> {
}
