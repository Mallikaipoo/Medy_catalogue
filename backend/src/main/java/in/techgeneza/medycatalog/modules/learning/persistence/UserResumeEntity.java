package in.techgeneza.medycatalog.modules.learning.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_resume_states")
public class UserResumeEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "exam_id")
    private UUID examId;

    @Column(nullable = false)
    private String route;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String payload;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
