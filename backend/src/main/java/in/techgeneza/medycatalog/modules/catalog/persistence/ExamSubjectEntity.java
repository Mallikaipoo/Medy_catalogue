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
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "exam_subjects")
@IdClass(ExamSubjectEntity.Pk.class)
public class ExamSubjectEntity {

    @Id
    @Column(name = "exam_id")
    private UUID examId;

    @Id
    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private UUID examId;
        private UUID subjectId;
    }
}
