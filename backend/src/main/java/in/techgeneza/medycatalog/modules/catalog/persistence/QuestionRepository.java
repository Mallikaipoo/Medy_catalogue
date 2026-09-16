package in.techgeneza.medycatalog.modules.catalog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {

    @Query("""
            select q from QuestionEntity q
            join fetch q.questionType
            where q.id = :id and q.status = :status
            """)
    Optional<QuestionEntity> findByIdAndStatus(@Param("id") UUID id, @Param("status") String status);

    long countByExamIdAndSubjectIdAndStatus(UUID examId, UUID subjectId, String status);

    long countByChapterIdAndStatus(UUID chapterId, String status);

    long countByTopicIdAndStatus(UUID topicId, String status);

    @Query("""
            select q.id from QuestionEntity q
            where q.status = 'PUBLISHED'
              and q.examId = :examId
              and (:subjectId is null or q.subjectId = :subjectId)
              and (:chapterId is null or q.chapterId = :chapterId)
              and (:topicId is null or q.topicId = :topicId)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:freePreviewOnly = false or q.freePreview = true)
            """)
    List<UUID> findPublishedIds(
            @Param("examId") UUID examId,
            @Param("subjectId") UUID subjectId,
            @Param("chapterId") UUID chapterId,
            @Param("topicId") UUID topicId,
            @Param("difficulty") String difficulty,
            @Param("freePreviewOnly") boolean freePreviewOnly
    );

    Optional<QuestionEntity> findFirstByExamIdAndFreePreviewTrueAndStatus(UUID examId, String status);
}
