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
@Table(name = "question_explanations")
public class QuestionExplanationEntity {

    @Id
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "simple_text")
    private String simpleText;

    @Column(name = "detailed_text")
    private String detailedText;

    @Column(name = "why_others_wrong")
    private String whyOthersWrong;

    @Column(name = "related_concept_topic_id")
    private UUID relatedConceptTopicId;

    @Column(name = "exam_tip")
    private String examTip;
}
