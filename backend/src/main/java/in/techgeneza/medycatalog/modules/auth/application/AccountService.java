package in.techgeneza.medycatalog.modules.auth.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.NotificationPreferencesResponse;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.PrivacySettingsResponse;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UpdateExamsRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UpdateNotificationsRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UpdatePrivacyRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UpdateProfileRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UserProfile;
import in.techgeneza.medycatalog.modules.auth.domain.AccountStatus;
import in.techgeneza.medycatalog.modules.auth.persistence.AccountDeletionRequestEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.AccountDeletionRequestRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.NotificationPreferenceEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.NotificationPreferenceRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.PrivacySettingsEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.PrivacySettingsRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.UserEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.UserRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.UserExamEnrollmentEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.UserExamEnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

@Service
public class AccountService {

    private final UserRepository users;
    private final AuthService authService;
    private final PrivacySettingsRepository privacySettings;
    private final NotificationPreferenceRepository notifications;
    private final AccountDeletionRequestRepository deletions;
    private final ExamRepository exams;
    private final UserExamEnrollmentRepository enrollments;
    private final Clock clock;

    public AccountService(
            UserRepository users,
            AuthService authService,
            PrivacySettingsRepository privacySettings,
            NotificationPreferenceRepository notifications,
            AccountDeletionRequestRepository deletions,
            ExamRepository exams,
            UserExamEnrollmentRepository enrollments,
            Clock clock
    ) {
        this.users = users;
        this.authService = authService;
        this.privacySettings = privacySettings;
        this.notifications = notifications;
        this.deletions = deletions;
        this.exams = exams;
        this.enrollments = enrollments;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public UserProfile me(UUID userId) {
        return authService.profileOf(userId);
    }

    @Transactional
    public UserProfile updateProfile(UUID userId, UpdateProfileRequest request) {
        UserEntity user = users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please sign in again to continue."));
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }
        if (request.country() != null) {
            user.setCountry(request.country());
        }
        if (request.state() != null) {
            user.setState(request.state());
        }
        if (request.city() != null) {
            user.setCity(request.city());
        }
        if (request.preferredLanguage() != null) {
            user.setPreferredLanguage(request.preferredLanguage());
        }
        if (request.profilePhotoUrl() != null) {
            user.setProfilePhotoUrl(request.profilePhotoUrl());
        }
        users.save(user);
        return authService.profileOf(userId);
    }

    @Transactional
    public UserProfile updateExams(UUID userId, UpdateExamsRequest request) {
        if (request.examIds() == null || request.examIds().isEmpty() || request.primaryExamId() == null) {
            throw ApiException.badRequest("INVALID_EXAMS", "Choose at least one examination.");
        }
        if (!request.examIds().contains(request.primaryExamId())) {
            throw ApiException.badRequest("INVALID_EXAMS", "Primary examination must be one of the selected exams.");
        }
        for (UUID examId : request.examIds()) {
            exams.findById(examId)
                    .filter(exam -> "ACTIVE".equals(exam.getStatus()))
                    .orElseThrow(() -> ApiException.notFound("That examination is not available."));
        }
        enrollments.deleteByUserId(userId);
        for (UUID examId : request.examIds()) {
            UserExamEnrollmentEntity enrollment = new UserExamEnrollmentEntity();
            enrollment.setUserId(userId);
            enrollment.setExamId(examId);
            enrollment.setPrimary(examId.equals(request.primaryExamId()));
            enrollment.setEnrolledAt(clock.instant());
            enrollments.save(enrollment);
        }
        return authService.profileOf(userId);
    }

    @Transactional(readOnly = true)
    public PrivacySettingsResponse privacy(UUID userId) {
        PrivacySettingsEntity entity = privacySettings.findById(userId).orElseGet(() -> {
            PrivacySettingsEntity created = new PrivacySettingsEntity();
            created.setUserId(userId);
            return created;
        });
        return new PrivacySettingsResponse(
                entity.getDisplayName(),
                entity.getLeaderboardVisibility(),
                entity.isAnalyticsConsent());
    }

    @Transactional
    public PrivacySettingsResponse updatePrivacy(UUID userId, UpdatePrivacyRequest request) {
        PrivacySettingsEntity entity = privacySettings.findById(userId).orElseGet(() -> {
            PrivacySettingsEntity created = new PrivacySettingsEntity();
            created.setUserId(userId);
            return created;
        });
        if (request.displayName() != null) {
            entity.setDisplayName(request.displayName());
        }
        if (request.leaderboardVisibility() != null) {
            entity.setLeaderboardVisibility(request.leaderboardVisibility());
        }
        if (request.analyticsConsent() != null) {
            entity.setAnalyticsConsent(request.analyticsConsent());
        }
        entity.setUpdatedAt(clock.instant());
        privacySettings.save(entity);
        return privacy(userId);
    }

    @Transactional(readOnly = true)
    public NotificationPreferencesResponse notifications(UUID userId) {
        NotificationPreferenceEntity entity = notifications.findById(userId).orElseGet(() -> {
            NotificationPreferenceEntity created = new NotificationPreferenceEntity();
            created.setUserId(userId);
            return created;
        });
        return new NotificationPreferencesResponse(
                entity.isDailyChallenge(),
                entity.isStreakReminders(),
                entity.isStudyReminders(),
                entity.isMarketing());
    }

    @Transactional
    public NotificationPreferencesResponse updateNotifications(UUID userId, UpdateNotificationsRequest request) {
        NotificationPreferenceEntity entity = notifications.findById(userId).orElseGet(() -> {
            NotificationPreferenceEntity created = new NotificationPreferenceEntity();
            created.setUserId(userId);
            return created;
        });
        if (request.dailyChallenge() != null) {
            entity.setDailyChallenge(request.dailyChallenge());
        }
        if (request.streakReminders() != null) {
            entity.setStreakReminders(request.streakReminders());
        }
        if (request.studyReminders() != null) {
            entity.setStudyReminders(request.studyReminders());
        }
        if (request.marketing() != null) {
            entity.setMarketing(request.marketing());
        }
        entity.setUpdatedAt(clock.instant());
        notifications.save(entity);
        return notifications(userId);
    }

    @Transactional
    public void requestDeletion(UUID userId) {
        UserEntity user = users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please sign in again to continue."));
        user.setAccountStatus(AccountStatus.PENDING_DELETION);
        users.save(user);
        AccountDeletionRequestEntity request = new AccountDeletionRequestEntity();
        request.setUserId(userId);
        request.setRequestedAt(clock.instant());
        request.setPurgeAfter(clock.instant().plus(Duration.ofDays(30)));
        request.setStatus("PENDING");
        deletions.save(request);
    }
}
