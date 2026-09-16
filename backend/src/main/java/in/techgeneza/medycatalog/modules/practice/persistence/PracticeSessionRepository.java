package in.techgeneza.medycatalog.modules.practice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PracticeSessionRepository extends JpaRepository<PracticeSessionEntity, UUID> {

    @Query("""
            select count(s) from PracticeSessionEntity s
            where s.userId = :userId and s.createdAt >= :from
            """)
    long countStartedSince(@Param("userId") UUID userId, @Param("from") Instant from);

    List<PracticeSessionEntity> findTop20ByUserIdAndStatusOrderBySubmittedAtDesc(UUID userId, String status);
}
