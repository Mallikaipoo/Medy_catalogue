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
@Table(name = "practice_sessions")
public class PracticeSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "chapter_id")
    private UUID chapterId;

    @Column(name = "topic_id")
    private UUID topicId;

    private String difficulty;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "must_submit_by", nullable = false)
    private Instant mustSubmitBy;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(nullable = false)
    private String status;

    private BigDecimal score;

    @Column(name = "total_marks")
    private BigDecimal totalMarks;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "wrong_count")
    private Integer wrongCount;

    @Column(name = "skipped_count")
    private Integer skippedCount;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
