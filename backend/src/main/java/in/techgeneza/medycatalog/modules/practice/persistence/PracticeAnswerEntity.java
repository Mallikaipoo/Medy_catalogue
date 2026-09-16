package in.techgeneza.medycatalog.modules.practice.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "practice_answers")
public class PracticeAnswerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "selected_payload", nullable = false)
    private String selectedPayload = "{}";

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "awarded_marks")
    private BigDecimal awardedMarks;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;
}
