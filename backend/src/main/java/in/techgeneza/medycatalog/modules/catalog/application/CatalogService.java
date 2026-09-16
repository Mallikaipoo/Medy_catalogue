package in.techgeneza.medycatalog.modules.catalog.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.billing.application.EntitlementService;
import in.techgeneza.medycatalog.modules.learning.application.LearningService;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.ChapterCard;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.ExamSyllabus;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.HomeResponse;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentQuestion;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.SubjectCard;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.SyllabusChapter;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.SyllabusTopic;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.TopicCard;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.TopicNotes;
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
    private final EntitlementService entitlements;

    public CatalogService(
            UserRepository users,
            ExamRepository exams,
            UserExamEnrollmentRepository enrollments,
            ExamSubjectRepository examSubjects,
            SubjectRepository subjects,
            ChapterRepository chapters,
            TopicRepository topics,
            QuestionRepository questions,
            QuestionOptionRepository options,
            EntitlementService entitlements
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
        this.entitlements = entitlements;
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
                        topic.getSortOrder(),
                        topic.getKeyPoints(),
                        topic.getExamHitCount(),
                        LearningService.focusLabel(topic.getExamHitCount()),
                        topic.getPatternNote()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TopicNotes topicNotes(UUID userId, UUID topicId) {
        var topic = topics.findById(topicId)
                .orElseThrow(() -> ApiException.notFound("That topic is not available."));
        int year = topic.getSyllabusYear() == null ? 2025 : topic.getSyllabusYear();
        boolean locked = userId == null || !entitlements.snapshot(userId).hasStudyMaterial();
        return new TopicNotes(
                topic.getId(),
                topic.getName(),
                year,
                topic.getKeyPoints(),
                locked ? null : topic.getDetailedExplanation(),
                questions.countByTopicIdAndStatus(topic.getId(), PUBLISHED),
                topic.getExamHitCount(),
                LearningService.focusLabel(topic.getExamHitCount()),
                topic.getPatternNote(),
                locked ? null : topic.getSpokenScript(),
                locked
        );
    }

    @Transactional(readOnly = true)
    public ExamSyllabus syllabus(UUID examId) {
        ExamEntity exam = exams.findById(examId)
                .filter(row -> "ACTIVE".equals(row.getStatus()))
                .orElseThrow(() -> ApiException.notFound("That examination is not available."));
        List<SyllabusChapter> chapterDtos = chapters.findByExamIdOrderBySortOrderAsc(examId).stream()
                .map(chapter -> {
                    SubjectEntity subject = subjects.findById(chapter.getSubjectId())
                            .orElseThrow(() -> ApiException.notFound("Subject not found."));
                    List<SyllabusTopic> topicDtos = topics.findByChapterIdOrderBySortOrderAsc(chapter.getId()).stream()
                            .map(topic -> new SyllabusTopic(
                                    topic.getId(),
                                    topic.getName(),
                                    topic.getKeyPoints(),
                                    questions.countByTopicIdAndStatus(topic.getId(), PUBLISHED),
                                    topic.getExamHitCount(),
                                    LearningService.focusLabel(topic.getExamHitCount())))
                            .toList();
                    return new SyllabusChapter(
                            chapter.getId(),
                            chapter.getName(),
                            subject.getCode(),
                            subject.getName(),
                            topicDtos);
                })
                .toList();
        return new ExamSyllabus(
                exam.getId(),
                exam.getCode(),
                exam.getName(),
                2025,
                "Latest public syllabus outline (2025). Practice items are original MedyCatalog questions covering typical 2016–2025 themes — not copied official papers.",
                chapterDtos
        );
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
