package in.techgeneza.medycatalog.modules.learning.persistence;

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
@Table(name = "question_variants")
public class QuestionVariantEntity {

    @Id
    private UUID id;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "variant_text", nullable = false)
    private String variantText;

    @Column(name = "trap_note", nullable = false)
    private String trapNote;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
