package in.techgeneza.medycatalog.modules.catalog.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "questions")
public class QuestionEntity {

    @Id
    private UUID id;

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_type_id", nullable = false)
    private QuestionTypeEntity questionType;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private BigDecimal marks;

    @Column(name = "negative_marks", nullable = false)
    private BigDecimal negativeMarks;

    @Column(name = "estimated_time_seconds", nullable = false)
    private int estimatedTimeSeconds;

    @Column(name = "exam_year")
    private Integer examYear;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_reference")
    private String sourceReference;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String status;

    @Column(name = "numerical_answer")
    private String numericalAnswer;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
