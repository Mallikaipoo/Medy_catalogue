package in.techgeneza.medycatalog.modules.catalog.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "topics")
public class TopicEntity {

    @Id
    private UUID id;

    @Column(name = "chapter_id", nullable = false)
    private UUID chapterId;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "key_points")
    private String keyPoints;

    @Column(name = "detailed_explanation")
    private String detailedExplanation;

    @Column(name = "syllabus_year")
    private Integer syllabusYear;

    @Column(name = "exam_hit_count", nullable = false)
    private int examHitCount;

    @Column(name = "pattern_note")
    private String patternNote;

    @Column(name = "spoken_script")
    private String spokenScript;
}
