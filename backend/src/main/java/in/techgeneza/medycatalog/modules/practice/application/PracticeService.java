package in.techgeneza.medycatalog.modules.practice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentQuestion;
import in.techgeneza.medycatalog.modules.catalog.application.StudentQuestionMapper;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExplanationStepRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionExplanationEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionExplanationRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionOptionEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionOptionRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.QuestionRepository;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.AnswerRequest;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.PreviewResponse;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.ResultResponse;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.ReviewItem;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.ReviewResponse;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.SessionItem;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.SessionResponse;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.StartRequest;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.Step;
import in.techgeneza.medycatalog.modules.practice.domain.ScoringCalculator;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeAnswerEntity;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeAnswerRepository;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeSessionEntity;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeSessionItemEntity;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeSessionItemRepository;
import in.techgeneza.medycatalog.modules.practice.persistence.PracticeSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PracticeService {

    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";

    private final QuestionRepository questions;
    private final QuestionOptionRepository options;
    private final QuestionExplanationRepository explanations;
    private final ExplanationStepRepository steps;
    private final PracticeSessionRepository sessions;
    private final PracticeSessionItemRepository items;
    private final PracticeAnswerRepository answers;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public PracticeService(
            QuestionRepository questions,
            QuestionOptionRepository options,
            QuestionExplanationRepository explanations,
            ExplanationStepRepository steps,
            PracticeSessionRepository sessions,
            PracticeSessionItemRepository items,
            PracticeAnswerRepository answers,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.questions = questions;
        this.options = options;
        this.explanations = explanations;
        this.steps = steps;
        this.sessions = sessions;
        this.items = items;
        this.answers = answers;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PreviewResponse preview(StartRequest request) {
        List<UUID> ids = publishedIds(request);
        return new PreviewResponse(ids.size(), request.questionCount());
    }

    @Transactional
    public SessionResponse start(UUID userId, StartRequest request) {
        List<UUID> pool = new ArrayList<>(publishedIds(request));
        if (pool.isEmpty()) {
            throw ApiException.badRequest("NO_QUESTIONS", "No published questions match those filters yet.");
        }
        Collections.shuffle(pool);
        int take = Math.min(request.questionCount(), pool.size());
        List<UUID> chosen = pool.subList(0, take);
        Instant now = clock.instant();
        int duration = request.durationSeconds() == null || request.durationSeconds() < 30
                ? Math.max(60, take * 60)
                : request.durationSeconds();
        PracticeSessionEntity session = new PracticeSessionEntity();
        session.setUserId(userId);
        session.setExamId(request.examId());
        session.setSubjectId(request.subjectId());
        session.setChapterId(request.chapterId());
        session.setTopicId(request.topicId());
        session.setDifficulty(blankToNull(request.difficulty()));
        session.setQuestionCount(take);
        session.setDurationSeconds(duration);
        session.setStartAt(now);
        session.setMustSubmitBy(now.plusSeconds(duration));
        session.setStatus(IN_PROGRESS);
        session.setCreatedAt(now);
        sessions.save(session);
        int order = 1;
        for (UUID questionId : chosen) {
            PracticeSessionItemEntity item = new PracticeSessionItemEntity();
            item.setSessionId(session.getId());
            item.setQuestionId(questionId);
            item.setItemOrder(order++);
            items.save(item);
        }
        return toSession(session, false);
    }

    @Transactional
    public SessionResponse get(UUID userId, UUID sessionId) {
        PracticeSessionEntity session = requireOwned(userId, sessionId);
        autoCompleteIfExpired(session);
        return toSession(session, COMPLETED.equals(session.getStatus()));
    }

    @Transactional
    public SessionResponse answer(UUID userId, UUID sessionId, AnswerRequest request) {
        PracticeSessionEntity session = requireOwned(userId, sessionId);
        autoCompleteIfExpired(session);
        if (!IN_PROGRESS.equals(session.getStatus())) {
            throw ApiException.badRequest("SESSION_CLOSED", "This practice session is already finished.");
        }
        PracticeSessionItemEntity item = items.findBySessionIdOrderByItemOrderAsc(sessionId).stream()
                .filter(row -> row.getQuestionId().equals(request.questionId()))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("That question is not in this session."));
        if (request.markedForReview() != null) {
            item.setMarkedForReview(request.markedForReview());
            items.save(item);
        }
        if (Boolean.TRUE.equals(request.clear())) {
            answers.findBySessionIdAndQuestionId(sessionId, request.questionId()).ifPresent(answers::delete);
            return toSession(session, false);
        }
        StoredAnswer payload = new StoredAnswer(
                request.optionIds() == null ? List.of() : request.optionIds(),
                request.numericalAnswer()
        );
        PracticeAnswerEntity answer = answers.findBySessionIdAndQuestionId(sessionId, request.questionId())
                .orElseGet(PracticeAnswerEntity::new);
        answer.setSessionId(sessionId);
        answer.setQuestionId(request.questionId());
        answer.setSelectedPayload(writePayload(payload));
        answer.setCorrect(null);
        answer.setAwardedMarks(null);
        answer.setSubmittedAt(clock.instant());
        answers.save(answer);
        return toSession(session, false);
    }

    @Transactional
    public ResultResponse complete(UUID userId, UUID sessionId) {
        PracticeSessionEntity session = requireOwned(userId, sessionId);
        if (COMPLETED.equals(session.getStatus())) {
            return toResult(session);
        }
        grade(session);
        return toResult(session);
    }

    @Transactional(readOnly = true)
    public ReviewResponse review(UUID userId, UUID sessionId) {
        PracticeSessionEntity session = requireOwned(userId, sessionId);
        if (!COMPLETED.equals(session.getStatus())) {
            throw ApiException.forbidden("Review is available after you submit the practice.");
        }
        List<PracticeSessionItemEntity> sessionItems = items.findBySessionIdOrderByItemOrderAsc(sessionId);
        Map<UUID, PracticeAnswerEntity> answerMap = answers.findBySessionId(sessionId).stream()
                .collect(Collectors.toMap(PracticeAnswerEntity::getQuestionId, Function.identity()));
        List<UUID> questionIds = sessionItems.stream().map(PracticeSessionItemEntity::getQuestionId).toList();
        Map<UUID, QuestionEntity> questionMap = questions.findAllById(questionIds).stream()
                .collect(Collectors.toMap(QuestionEntity::getId, Function.identity()));
        Map<UUID, List<QuestionOptionEntity>> optionMap = options.findByQuestionIdInOrderByOptionOrderAsc(questionIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionOptionEntity::getQuestionId));
        List<ReviewItem> reviewItems = new ArrayList<>();
        for (PracticeSessionItemEntity item : sessionItems) {
            QuestionEntity question = questionMap.get(item.getQuestionId());
            List<QuestionOptionEntity> optionList = optionMap.getOrDefault(item.getQuestionId(), List.of());
            StudentQuestion student = StudentQuestionMapper.toStudent(question, optionList);
            StoredAnswer stored = readPayload(answerMap.get(item.getQuestionId()));
            List<UUID> correctIds = optionList.stream()
                    .filter(QuestionOptionEntity::isCorrect)
                    .map(QuestionOptionEntity::getId)
                    .toList();
            QuestionExplanationEntity explanation = explanations.findByQuestionId(question.getId()).orElse(null);
            List<Step> stepDtos = steps.findByQuestionIdOrderByStepOrderAsc(question.getId()).stream()
                    .map(step -> new Step(step.getStepOrder(), step.getTitle(), step.getBody()))
                    .toList();
            PracticeAnswerEntity answer = answerMap.get(item.getQuestionId());
            reviewItems.add(new ReviewItem(
                    item.getItemOrder(),
                    student,
                    stored.optionIds(),
                    correctIds,
                    stored.numerical(),
                    question.getNumericalAnswer(),
                    answer != null && Boolean.TRUE.equals(answer.getCorrect()),
                    answer == null ? BigDecimal.ZERO : defaultZero(answer.getAwardedMarks()),
                    explanation == null ? null : explanation.getSimpleText(),
                    explanation == null ? null : explanation.getDetailedText(),
                    explanation == null ? null : explanation.getWhyOthersWrong(),
                    explanation == null ? null : explanation.getExamTip(),
                    stepDtos,
                    question.getDifficulty()
            ));
        }
        return new ReviewResponse(session.getId(), toResult(session), reviewItems);
    }

    private List<UUID> publishedIds(StartRequest request) {
        return questions.findPublishedIds(
                request.examId(),
                request.subjectId(),
                request.chapterId(),
                request.topicId(),
                blankToNull(request.difficulty())
        );
    }

    private void autoCompleteIfExpired(PracticeSessionEntity session) {
        if (IN_PROGRESS.equals(session.getStatus()) && clock.instant().isAfter(session.getMustSubmitBy())) {
            grade(session);
        }
    }

    private void grade(PracticeSessionEntity session) {
        Instant now = clock.instant();
        List<PracticeSessionItemEntity> sessionItems = items.findBySessionIdOrderByItemOrderAsc(session.getId());
        Map<UUID, PracticeAnswerEntity> answerMap = answers.findBySessionId(session.getId()).stream()
                .collect(Collectors.toMap(PracticeAnswerEntity::getQuestionId, Function.identity()));
        List<UUID> questionIds = sessionItems.stream().map(PracticeSessionItemEntity::getQuestionId).toList();
        Map<UUID, QuestionEntity> questionMap = questions.findAllById(questionIds).stream()
                .collect(Collectors.toMap(QuestionEntity::getId, Function.identity()));
        Map<UUID, List<QuestionOptionEntity>> optionMap = options.findByQuestionIdInOrderByOptionOrderAsc(questionIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionOptionEntity::getQuestionId));
        BigDecimal score = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        int correct = 0;
        int wrong = 0;
        int skipped = 0;
        for (PracticeSessionItemEntity item : sessionItems) {
            QuestionEntity question = questionMap.get(item.getQuestionId());
            total = total.add(question.getMarks());
            Set<UUID> correctIds = optionMap.getOrDefault(item.getQuestionId(), List.of()).stream()
                    .filter(QuestionOptionEntity::isCorrect)
                    .map(QuestionOptionEntity::getId)
                    .collect(Collectors.toCollection(HashSet::new));
            StoredAnswer stored = readPayload(answerMap.get(item.getQuestionId()));
            ScoringCalculator.Score result = ScoringCalculator.score(
                    question.getQuestionType().getCode(),
                    new HashSet<>(stored.optionIds()),
                    correctIds,
                    stored.numerical(),
                    question.getNumericalAnswer(),
                    question.getMarks(),
                    question.getNegativeMarks()
            );
            PracticeAnswerEntity answer = answerMap.get(item.getQuestionId());
            if (answer == null) {
                answer = new PracticeAnswerEntity();
                answer.setSessionId(session.getId());
                answer.setQuestionId(item.getQuestionId());
                answer.setSelectedPayload(writePayload(stored));
                answer.setSubmittedAt(now);
            }
            answer.setCorrect(result.correct());
            answer.setAwardedMarks(result.awardedMarks());
            answers.save(answer);
            score = score.add(result.awardedMarks());
            if (!result.answered()) {
                skipped++;
            } else if (result.correct()) {
                correct++;
            } else {
                wrong++;
            }
        }
        session.setStatus(COMPLETED);
        session.setSubmittedAt(now);
        session.setScore(score);
        session.setTotalMarks(total);
        session.setCorrectCount(correct);
        session.setWrongCount(wrong);
        session.setSkippedCount(skipped);
        long taken = Duration.between(session.getStartAt(), now).getSeconds();
        session.setTimeTakenSeconds((int) Math.min(taken, session.getDurationSeconds()));
        sessions.save(session);
    }

    private SessionResponse toSession(PracticeSessionEntity session, boolean includeKeysIgnored) {
        List<PracticeSessionItemEntity> sessionItems = items.findBySessionIdOrderByItemOrderAsc(session.getId());
        Map<UUID, PracticeAnswerEntity> answerMap = answers.findBySessionId(session.getId()).stream()
                .collect(Collectors.toMap(PracticeAnswerEntity::getQuestionId, Function.identity()));
        List<UUID> questionIds = sessionItems.stream().map(PracticeSessionItemEntity::getQuestionId).toList();
        Map<UUID, QuestionEntity> questionMap = questions.findAllById(questionIds).stream()
                .collect(Collectors.toMap(QuestionEntity::getId, Function.identity()));
        Map<UUID, List<QuestionOptionEntity>> optionMap = options.findByQuestionIdInOrderByOptionOrderAsc(questionIds)
                .stream()
                .collect(Collectors.groupingBy(QuestionOptionEntity::getQuestionId));
        List<SessionItem> dtos = new ArrayList<>();
        for (PracticeSessionItemEntity item : sessionItems) {
            QuestionEntity question = questionMap.get(item.getQuestionId());
            StudentQuestion student = StudentQuestionMapper.toStudent(
                    question, optionMap.getOrDefault(item.getQuestionId(), List.of()));
            StoredAnswer stored = readPayload(answerMap.get(item.getQuestionId()));
            dtos.add(new SessionItem(
                    item.getItemOrder(),
                    student,
                    stored.optionIds(),
                    stored.numerical(),
                    item.isMarkedForReview()
            ));
        }
        long remaining = Math.max(0, Duration.between(clock.instant(), session.getMustSubmitBy()).getSeconds());
        if (COMPLETED.equals(session.getStatus())) {
            remaining = 0;
        }
        return new SessionResponse(session.getId(), session.getStatus(), remaining, session.getQuestionCount(), dtos);
    }

    private ResultResponse toResult(PracticeSessionEntity session) {
        int count = Math.max(1, session.getQuestionCount());
        int correct = session.getCorrectCount() == null ? 0 : session.getCorrectCount();
        int answered = count - (session.getSkippedCount() == null ? 0 : session.getSkippedCount());
        double accuracy = answered == 0 ? 0 : (correct * 100.0) / answered;
        int time = session.getTimeTakenSeconds() == null ? 0 : session.getTimeTakenSeconds();
        return new ResultResponse(
                session.getId(),
                defaultZero(session.getScore()),
                defaultZero(session.getTotalMarks()),
                correct,
                session.getWrongCount() == null ? 0 : session.getWrongCount(),
                session.getSkippedCount() == null ? 0 : session.getSkippedCount(),
                time,
                BigDecimal.valueOf(accuracy).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                (double) time / count
        );
    }

    private PracticeSessionEntity requireOwned(UUID userId, UUID sessionId) {
        PracticeSessionEntity session = sessions.findById(sessionId)
                .orElseThrow(() -> ApiException.notFound("That practice session was not found."));
        if (!session.getUserId().equals(userId)) {
            throw ApiException.forbidden("You cannot open another student's practice session.");
        }
        return session;
    }

    private String writePayload(StoredAnswer payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private StoredAnswer readPayload(PracticeAnswerEntity answer) {
        if (answer == null || answer.getSelectedPayload() == null || answer.getSelectedPayload().isBlank()) {
            return new StoredAnswer(List.of(), null);
        }
        try {
            StoredAnswer parsed = objectMapper.readValue(answer.getSelectedPayload(), StoredAnswer.class);
            return parsed == null ? new StoredAnswer(List.of(), null) : parsed;
        } catch (JsonProcessingException ex) {
            return new StoredAnswer(List.of(), null);
        }
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    public record StoredAnswer(List<UUID> optionIds, String numerical) {
        public StoredAnswer {
            optionIds = optionIds == null ? List.of() : optionIds;
        }
    }
}
