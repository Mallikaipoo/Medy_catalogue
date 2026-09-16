package in.techgeneza.medycatalog.modules.auth.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleEntity.Pk> {
    List<UserRoleEntity> findByUserId(UUID userId);
}
