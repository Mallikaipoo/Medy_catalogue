package in.techgeneza.medycatalog.modules.catalog.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_exam_enrollments")
@IdClass(UserExamEnrollmentEntity.Pk.class)
public class UserExamEnrollmentEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "exam_id")
    private UUID examId;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt = Instant.now();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private UUID userId;
        private UUID examId;
    }
}
