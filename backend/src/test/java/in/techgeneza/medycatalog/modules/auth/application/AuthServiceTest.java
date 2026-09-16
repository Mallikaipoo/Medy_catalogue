package in.techgeneza.medycatalog.modules.auth.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.RegisterRequest;
import in.techgeneza.medycatalog.modules.auth.application.social.SocialTokenVerifier;
import in.techgeneza.medycatalog.modules.auth.persistence.NotificationPreferenceRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.OtpChallengeRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.ParentalConsentRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.PrivacySettingsRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.RefreshTokenRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.RoleRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.UserEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.UserIdentityRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.UserRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.UserRoleRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.UserExamEnrollmentRepository;
import in.techgeneza.medycatalog.security.JwtService;
import in.techgeneza.medycatalog.security.MedycatalogProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository users;
    @Mock
    private UserIdentityRepository identities;
    @Mock
    private RefreshTokenRepository refreshTokens;
    @Mock
    private OtpChallengeRepository otpChallenges;
    @Mock
    private RoleRepository roles;
    @Mock
    private UserRoleRepository userRoles;
    @Mock
    private PrivacySettingsRepository privacySettings;
    @Mock
    private NotificationPreferenceRepository notifications;
    @Mock
    private ParentalConsentRepository parentalConsents;
    @Mock
    private ExamRepository exams;
    @Mock
    private UserExamEnrollmentRepository enrollments;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private MedycatalogProperties properties;
    @Mock
    private Clock clock;
    @Mock
    private SocialTokenVerifier socialTokenVerifier;
    @Mock
    private LoginRateLimiter rateLimiter;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.findByEmailIgnoreCase("ada@example.com")).thenReturn(Optional.of(new UserEntity()));
        RegisterRequest request = new RegisterRequest("Ada", "ada@example.com", "longenough", null, null);
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("already exists");
    }
}
