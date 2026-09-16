package in.techgeneza.medycatalog.modules.auth.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OtpChallengeRepository extends JpaRepository<OtpChallengeEntity, UUID> {
    Optional<OtpChallengeEntity> findFirstByDestinationAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            String destination, String purpose);
}
