package in.techgeneza.medycatalog.modules.learning.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.billing.application.EntitlementService;
import in.techgeneza.medycatalog.modules.billing.domain.EntitlementSnapshot;
import in.techgeneza.medycatalog.modules.catalog.persistence.ChapterEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.ChapterRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionExplanationEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionExplanationRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionOptionRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.TopicEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.TopicRepository;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.DoubtRequest;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.DoubtResponse;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.FreePreview;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.LibraryResponse;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.LocaleCard;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.PreviewOption;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.ResumeRequest;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.ResumeState;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.SavedAnswer;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.VariantCard;
import in.techgeneza.medycatalog.modules.learning.domain.AppLocale;
import in.techgeneza.medycatalog.modules.learning.persistence.LearningQueryEntity;
import in.techgeneza.medycatalog.modules.learning.persistence.LearningQueryRepository;
import in.techgeneza.medycatalog.modules.learning.persistence.QuestionVariantRepository;
import in.techgeneza.medycatalog.modules.learning.persistence.UserResumeEntity;
import in.techgeneza.medycatalog.modules.learning.persistence.UserResumeRepository;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeAnswerEntity;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeAnswerRepository;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeSessionEntity;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LearningService {

    private static final Pattern TOKEN = Pattern.compile("[^a-z0-9]+");
    private static final Set<String> STOP = Set.of(
            "the", "a", "an", "is", "of", "in", "to", "and", "or", "for", "why", "what", "how",
            "please", "explain", "doubt", "question", "this", "that", "from"
    );

    private final ExamRepository exams;
    private final ChapterRepository chapters;
    private final TopicRepository topics;
    private final QuestionRepository questions;
    private final QuestionOptionRepository options;
    private final QuestionExplanationRepository explanations;
    private final QuestionVariantRepository variants;
    private final UserResumeRepository resumes;
    private final LearningQueryRepository queries;
    private final PracticeSessionRepository sessions;
    private final PracticeAnswerRepository answers;
    private final EntitlementService entitlements;
    private final SpokenScriptService spoken;
    private final Clock clock;

    public LearningService(
            ExamRepository exams,
            ChapterRepository chapters,
            TopicRepository topics,
            QuestionRepository questions,
            QuestionOptionRepository options,
            QuestionExplanationRepository explanations,
            QuestionVariantRepository variants,
            UserResumeRepository resumes,
            LearningQueryRepository queries,
            PracticeSessionRepository sessions,
            PracticeAnswerRepository answers,
            EntitlementService entitlements,
            SpokenScriptService spoken,
            Clock clock
    ) {
        this.exams = exams;
        this.chapters = chapters;
        this.topics = topics;
        this.questions = questions;
        this.options = options;
        this.explanations = explanations;
        this.variants = variants;
        this.resumes = resumes;
        this.queries = queries;
        this.sessions = sessions;
        this.answers = answers;
        this.entitlements = entitlements;
        this.spoken = spoken;
        this.clock = clock;
    }

    public List<LocaleCard> locales() {
        return AppLocale.supported().stream()
                .map(item -> new LocaleCard(
                        item.code(), item.englishName(), item.nativeName(), item.ttsCode(), item.classical()))
                .toList();
    }

    @Transactional(readOnly = true)
    public FreePreview freePreview(UUID examId, String localeCode) {
        ExamEntity exam = exams.findById(examId)
                .filter(row -> "ACTIVE".equals(row.getStatus()))
                .orElseThrow(() -> ApiException.notFound("That examination is not available."));
        QuestionEntity question = questions.findFirstByExamIdAndFreePreviewTrueAndStatus(examId, "PUBLISHED")
                .orElseThrow(() -> ApiException.notFound("A free last-year question is not ready for this exam yet."));
        return toPreview(exam, question, AppLocale.fromCode(localeCode));
    }

    @Transactional
    public ResumeState saveResume(UUID userId, ResumeRequest request) {
        if (request == null || request.route() == null || request.route().isBlank()) {
            throw ApiException.badRequest("RESUME", "A screen to continue from is required.");
        }
        Instant now = clock.instant();
        UserResumeEntity row = resumes.findById(userId).orElseGet(UserResumeEntity::new);
        row.setUserId(userId);
        row.setExamId(request.examId());
        row.setRoute(request.route().trim());
        row.setTitle(request.title() == null || request.title().isBlank() ? "Continue" : request.title().trim());
        row.setPayload(request.payload() == null || request.payload().isBlank() ? "{}" : request.payload());
        row.setUpdatedAt(now);
        resumes.save(row);
        return toResume(row);
    }

    @Transactional(readOnly = true)
    public ResumeState resume(UUID userId) {
        return resumes.findById(userId).map(this::toResume).orElse(null);
    }

    @Transactional
    public DoubtResponse ask(UUID userId, DoubtRequest request) {
        EntitlementSnapshot snap = entitlements.snapshot(userId);
        if (!snap.hasDoubtTutor()) {
            throw new ApiException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "PREMIUM_REQUIRED",
                    "Ask doubts in your language after Premium. The free last-year question still includes audio."
            );
        }
        if (request == null || request.text() == null || request.text().isBlank()) {
            throw ApiException.badRequest("DOUBT", "Type or speak a doubt first.");
        }
        AppLocale locale = AppLocale.fromCode(request.locale());
        UUID examId = request.examId();
        TopicEntity topic = matchTopic(examId, request.topicId(), request.text());
        QuestionEntity question = matchQuestion(examId, request.questionId(), request.text());
        String english = buildAnswer(topic, question, request.text());
        String shortText = spoken.shortAnswer(locale, english);
        String script = spoken.wrap(locale, methodAndAnswer(question, topic, english));
        LearningQueryEntity row = new LearningQueryEntity();
        row.setId(UUID.randomUUID());
        row.setUserId(userId);
        row.setExamId(examId);
        row.setQuestionId(question == null ? null : question.getId());
        row.setTopicId(topic == null ? null : topic.getId());
        row.setSource(source(request.source()));
        row.setLocale(locale.code());
        row.setQueryText(request.text().trim());
        row.setAnswerShort(shortText);
        row.setSpokenScript(script);
        row.setCreatedAt(clock.instant());
        queries.save(row);
        return toDoubt(row, topic);
    }

    @Transactional(readOnly = true)
    public LibraryResponse library(UUID userId, String localeCode) {
        EntitlementSnapshot snap = entitlements.snapshot(userId);
        if (!snap.hasResumeLibrary()) {
            throw new ApiException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "PREMIUM_REQUIRED",
                    "Your answers and spoken doubts stay saved on Premium so you can replay them any time."
            );
        }
        AppLocale locale = AppLocale.fromCode(localeCode);
        List<DoubtResponse> savedQueries = queries.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(row -> toDoubt(row, row.getTopicId() == null ? null : topics.findById(row.getTopicId()).orElse(null)))
                .toList();
        List<SavedAnswer> savedAnswers = new ArrayList<>();
        for (PracticeSessionEntity session : sessions.findTop20ByUserIdAndStatusOrderBySubmittedAtDesc(userId, "COMPLETED")) {
            List<PracticeAnswerEntity> sessionAnswers = answers.findBySessionId(session.getId());
            for (PracticeAnswerEntity answer : sessionAnswers) {
                QuestionEntity question = questions.findById(answer.getQuestionId()).orElse(null);
                if (question == null) {
                    continue;
                }
                QuestionExplanationEntity explanation = explanations.findByQuestionId(question.getId()).orElse(null);
                String body = explanation == null ? question.getQuestionText() : firstNonBlank(
                        explanation.getSimpleText(), explanation.getDetailedText(), question.getQuestionText());
                savedAnswers.add(new SavedAnswer(
                        session.getId(),
                        question.getId(),
                        question.getQuestionText(),
                        Boolean.TRUE.equals(answer.getCorrect()),
                        body,
                        spoken.wrap(locale, firstNonBlank(question.getMethodScript(), body)),
                        answer.getSubmittedAt()
                ));
            }
        }
        savedAnswers.sort(Comparator.comparing(SavedAnswer::at, Comparator.nullsLast(Comparator.reverseOrder())));
        if (savedAnswers.size() > 50) {
            savedAnswers = savedAnswers.subList(0, 50);
        }
        return new LibraryResponse(savedAnswers, savedQueries);
    }

    public static String focusLabel(int hits) {
        int n = Math.max(1, Math.min(2, hits));
        if (n == 1) {
            return "This topic area has come 1 time — focus here.";
        }
        return "This topic area has come 1–2 times — focus here.";
    }

    private FreePreview toPreview(ExamEntity exam, QuestionEntity question, AppLocale locale) {
        TopicEntity topic = topics.findById(question.getTopicId()).orElse(null);
        QuestionExplanationEntity explanation = explanations.findByQuestionId(question.getId()).orElse(null);
        List<PreviewOption> optionCards = options.findByQuestionIdOrderByOptionOrderAsc(question.getId()).stream()
                .map(option -> new PreviewOption(option.getId(), option.getOptionText(), option.getOptionOrder(), option.isCorrect()))
                .toList();
        List<VariantCard> variantCards = variants.findByQuestionIdOrderBySortOrderAsc(question.getId()).stream()
                .map(row -> new VariantCard(row.getVariantText(), row.getTrapNote()))
                .toList();
        String detailed = explanation == null ? null : explanation.getDetailedText();
        String simple = explanation == null ? null : explanation.getSimpleText();
        String method = firstNonBlank(question.getMethodScript(), detailed, simple);
        return new FreePreview(
                question.getId(),
                exam.getId(),
                exam.getCode(),
                question.getExamYear() == null ? 2025 : question.getExamYear(),
                question.getQuestionText(),
                optionCards,
                simple,
                detailed,
                method,
                question.getTrapWording(),
                spoken.wrap(locale, method),
                locale.ttsCode(),
                variantCards,
                topic == null ? null : topic.getName(),
                topic == null ? 1 : topic.getExamHitCount(),
                topic == null ? focusLabel(1) : focusLabel(topic.getExamHitCount())
        );
    }

    private TopicEntity matchTopic(UUID examId, UUID topicId, String text) {
        if (topicId != null) {
            return topics.findById(topicId).orElse(null);
        }
        List<TopicEntity> pool = topicsForExam(examId);
        List<String> tokens = tokens(text);
        TopicEntity best = null;
        int bestScore = 0;
        for (TopicEntity topic : pool) {
            int score = score(tokens, topic.getName() + " " + nullToEmpty(topic.getKeyPoints()));
            if (score > bestScore) {
                bestScore = score;
                best = topic;
            }
        }
        return bestScore >= 1 ? best : null;
    }

    private QuestionEntity matchQuestion(UUID examId, UUID questionId, String text) {
        if (questionId != null) {
            return questions.findById(questionId).orElse(null);
        }
        if (examId == null) {
            return null;
        }
        List<String> tokens = tokens(text);
        QuestionEntity best = null;
        int bestScore = 0;
        for (UUID id : questions.findPublishedIds(examId, null, null, null, null, false)) {
            QuestionEntity question = questions.findById(id).orElse(null);
            if (question == null) {
                continue;
            }
            int score = score(tokens, question.getQuestionText());
            if (score > bestScore) {
                bestScore = score;
                best = question;
            }
        }
        return bestScore >= 2 ? best : null;
    }

    private List<TopicEntity> topicsForExam(UUID examId) {
        if (examId == null) {
            return topics.findAll();
        }
        List<TopicEntity> out = new ArrayList<>();
        for (ChapterEntity chapter : chapters.findByExamIdOrderBySortOrderAsc(examId)) {
            out.addAll(topics.findByChapterIdOrderBySortOrderAsc(chapter.getId()));
        }
        return out;
    }

    private String buildAnswer(TopicEntity topic, QuestionEntity question, String query) {
        if (question != null) {
            Optional<QuestionExplanationEntity> explanation = explanations.findByQuestionId(question.getId());
            String body = explanation.map(row -> firstNonBlank(row.getSimpleText(), row.getDetailedText())).orElse(null);
            String trap = firstNonBlank(question.getTrapWording(), topic == null ? null : topic.getPatternNote());
            return firstNonBlank(body, "Hold the definition, then eliminate look-alike options.")
                    + (trap == null ? "" : " Pattern: " + trap);
        }
        if (topic != null) {
            return firstNonBlank(topic.getKeyPoints(), topic.getDetailedExplanation(), topic.getName())
                    + " " + focusLabel(topic.getExamHitCount())
                    + " " + nullToEmpty(topic.getPatternNote());
        }
        return "I heard: \"" + query.trim() + "\". Name the exam topic in a few words — for example “like charges” or “fertilisation site” — and I will answer in a short line plus audio.";
    }

    private String methodAndAnswer(QuestionEntity question, TopicEntity topic, String english) {
        String method = question == null ? (topic == null ? null : topic.getSpokenScript()) : question.getMethodScript();
        return firstNonBlank(method, english);
    }

    private DoubtResponse toDoubt(LearningQueryEntity row, TopicEntity topic) {
        AppLocale locale = AppLocale.fromCode(row.getLocale());
        return new DoubtResponse(
                row.getId(),
                locale.code(),
                locale.ttsCode(),
                row.getQueryText(),
                row.getAnswerShort(),
                row.getSpokenScript(),
                topic == null ? null : topic.getName(),
                topic == null ? null : focusLabel(topic.getExamHitCount())
        );
    }

    private ResumeState toResume(UserResumeEntity row) {
        return new ResumeState(row.getRoute(), row.getTitle(), row.getExamId(), row.getPayload(), row.getUpdatedAt());
    }

    private static String source(String raw) {
        return "VOICE".equalsIgnoreCase(raw) ? "VOICE" : "TEXT";
    }

    private static List<String> tokens(String text) {
        return TOKEN.splitAsStream(text.toLowerCase(Locale.ROOT))
                .filter(token -> token.length() > 2 && !STOP.contains(token))
                .collect(Collectors.toList());
    }

    private static int score(List<String> tokens, String haystack) {
        String hay = haystack == null ? "" : haystack.toLowerCase(Locale.ROOT);
        int n = 0;
        for (String token : tokens) {
            if (hay.contains(token)) {
                n++;
            }
        }
        return n;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
