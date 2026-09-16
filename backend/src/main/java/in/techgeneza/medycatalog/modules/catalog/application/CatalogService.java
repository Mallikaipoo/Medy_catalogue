package in.techgeneza.medycatalog.modules.catalog.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.ChapterCard;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.HomeResponse;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentQuestion;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.SubjectCard;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.TopicCard;
import in.techgeneza.medycatalog.modules.catalog.persistence.ChapterEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.ChapterRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamSubjectEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamSubjectRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionOptionRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.SubjectEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.SubjectRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.TopicRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.UserExamEnrollmentEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.UserExamEnrollmentRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.UserEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogService {

    public static final String PUBLISHED = "PUBLISHED";

    private final UserRepository users;
    private final ExamRepository exams;
    private final UserExamEnrollmentRepository enrollments;
    private final ExamSubjectRepository examSubjects;
    private final SubjectRepository subjects;
    private final ChapterRepository chapters;
    private final TopicRepository topics;
    private final QuestionRepository questions;
    private final QuestionOptionRepository options;

    public CatalogService(
            UserRepository users,
            ExamRepository exams,
            UserExamEnrollmentRepository enrollments,
            ExamSubjectRepository examSubjects,
            SubjectRepository subjects,
            ChapterRepository chapters,
            TopicRepository topics,
            QuestionRepository questions,
            QuestionOptionRepository options
    ) {
        this.users = users;
        this.exams = exams;
        this.enrollments = enrollments;
        this.examSubjects = examSubjects;
        this.subjects = subjects;
        this.chapters = chapters;
        this.topics = topics;
        this.questions = questions;
        this.options = options;
    }

    @Transactional(readOnly = true)
    public HomeResponse home(UUID userId) {
        UserEntity user = users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please sign in again to continue."));
        UserExamEnrollmentEntity primary = enrollments.findByUserId(userId).stream()
                .filter(UserExamEnrollmentEntity::isPrimary)
                .findFirst()
                .or(() -> enrollments.findByUserId(userId).stream().findFirst())
                .orElseThrow(() -> ApiException.badRequest("NO_EXAM", "Please choose an examination first."));
        ExamEntity exam = exams.findById(primary.getExamId())
                .orElseThrow(() -> ApiException.notFound("That examination is not available."));
        return new HomeResponse(user.getName(), exam.getId(), exam.getCode(), exam.getName(), subjects(exam.getId()));
    }

    @Transactional(readOnly = true)
    public List<SubjectCard> subjects(UUID examId) {
        requireExam(examId);
        return examSubjects.findByExamIdOrderBySortOrderAsc(examId).stream()
                .map(link -> {
                    SubjectEntity subject = subjects.findById(link.getSubjectId())
                            .orElseThrow(() -> ApiException.notFound("Subject not found."));
                    long count = questions.countByExamIdAndSubjectIdAndStatus(examId, subject.getId(), PUBLISHED);
                    return new SubjectCard(subject.getId(), subject.getCode(), subject.getName(), count, link.getSortOrder());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChapterCard> chapters(UUID examId, UUID subjectId) {
        requireExam(examId);
        return chapters.findByExamIdAndSubjectIdOrderBySortOrderAsc(examId, subjectId).stream()
                .map(chapter -> new ChapterCard(
                        chapter.getId(),
                        chapter.getName(),
                        questions.countByChapterIdAndStatus(chapter.getId(), PUBLISHED),
                        chapter.getSortOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopicCard> topics(UUID chapterId) {
        ChapterEntity chapter = chapters.findById(chapterId)
                .orElseThrow(() -> ApiException.notFound("That chapter is not available."));
        return topics.findByChapterIdOrderBySortOrderAsc(chapter.getId()).stream()
                .map(topic -> new TopicCard(
                        topic.getId(),
                        topic.getName(),
                        questions.countByTopicIdAndStatus(topic.getId(), PUBLISHED),
                        topic.getSortOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentQuestion publishedQuestion(UUID questionId) {
        QuestionEntity question = questions.findByIdAndStatus(questionId, PUBLISHED)
                .orElseThrow(() -> ApiException.notFound("That question is not available."));
        return StudentQuestionMapper.toStudent(
                question, options.findByQuestionIdOrderByOptionOrderAsc(question.getId()));
    }

    private void requireExam(UUID examId) {
        exams.findById(examId)
                .filter(exam -> "ACTIVE".equals(exam.getStatus()))
                .orElseThrow(() -> ApiException.notFound("That examination is not available."));
    }
}
