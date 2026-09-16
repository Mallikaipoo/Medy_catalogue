package in.techgeneza.medycatalog.modules.auth.persistence;

import in.techgeneza.medycatalog.modules.auth.domain.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserIdentityRepository extends JpaRepository<UserIdentityEntity, UUID> {
    Optional<UserIdentityEntity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    List<UserIdentityEntity> findByUserId(UUID userId);

    boolean existsByUserIdAndProvider(UUID userId, AuthProvider provider);
}
