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
@Table(name = "question_options")
public class QuestionOptionEntity {

    @Id
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "option_text", nullable = false)
    private String optionText;

    @Column(name = "option_order", nullable = false)
    private int optionOrder;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;
}
