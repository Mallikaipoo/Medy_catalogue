package in.techgeneza.medycatalog.modules.auth.application;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.ExamSummary;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.ForgotPasswordRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.GuestUpgradeRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.LoginRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.OtpRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.OtpVerifyRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.RefreshRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.RegisterRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.ResetPasswordRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.SocialTokenRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.TokenResponse;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UserProfile;
import in.techgeneza.medycatalog.modules.auth.application.social.SocialProfile;
import in.techgeneza.medycatalog.modules.auth.application.social.SocialTokenVerifier;
import in.techgeneza.medycatalog.modules.auth.domain.AccountStatus;
import in.techgeneza.medycatalog.modules.auth.domain.AuthProvider;
import in.techgeneza.medycatalog.modules.auth.persistence.NotificationPreferenceEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.NotificationPreferenceRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.OtpChallengeEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.OtpChallengeRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.ParentalConsentEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.ParentalConsentRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.PrivacySettingsEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.PrivacySettingsRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.RefreshTokenEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.RefreshTokenRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.RoleEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.RoleRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.UserEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.UserIdentityEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.UserIdentityRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.UserRepository;
import in.techgeneza.medycatalog.modules.auth.persistence.UserRoleEntity;
import in.techgeneza.medycatalog.modules.auth.persistence.UserRoleRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.ExamRepository;
import in.techgeneza.medycatalog.modules.catalog.persistence.UserExamEnrollmentEntity;
import in.techgeneza.medycatalog.modules.catalog.persistence.UserExamEnrollmentRepository;
import in.techgeneza.medycatalog.security.JwtService;
import in.techgeneza.medycatalog.security.MedycatalogProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String OTP_LOGIN = "EMAIL_OTP";
    private static final String OTP_RESET = "PASSWORD_RESET";

    private final UserRepository users;
    private final UserIdentityRepository identities;
    private final RefreshTokenRepository refreshTokens;
    private final OtpChallengeRepository otpChallenges;
    private final RoleRepository roles;
    private final UserRoleRepository userRoles;
    private final PrivacySettingsRepository privacySettings;
    private final NotificationPreferenceRepository notifications;
    private final ParentalConsentRepository parentalConsents;
    private final ExamRepository exams;
    private final UserExamEnrollmentRepository enrollments;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MedycatalogProperties properties;
    private final Clock clock;
    private final SocialTokenVerifier socialTokenVerifier;
    private final LoginRateLimiter rateLimiter;

    public AuthService(
            UserRepository users,
            UserIdentityRepository identities,
            RefreshTokenRepository refreshTokens,
            OtpChallengeRepository otpChallenges,
            RoleRepository roles,
            UserRoleRepository userRoles,
            PrivacySettingsRepository privacySettings,
            NotificationPreferenceRepository notifications,
            ParentalConsentRepository parentalConsents,
            ExamRepository exams,
            UserExamEnrollmentRepository enrollments,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            MedycatalogProperties properties,
            Clock clock,
            SocialTokenVerifier socialTokenVerifier,
            LoginRateLimiter rateLimiter
    ) {
        this.users = users;
        this.identities = identities;
        this.refreshTokens = refreshTokens;
        this.otpChallenges = otpChallenges;
        this.roles = roles;
        this.userRoles = userRoles;
        this.privacySettings = privacySettings;
        this.notifications = notifications;
        this.parentalConsents = parentalConsents;
        this.exams = exams;
        this.enrollments = enrollments;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
        this.clock = clock;
        this.socialTokenVerifier = socialTokenVerifier;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        rateLimiter.check("register:" + email);
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw ApiException.conflict("EMAIL_IN_USE", "An account with this email already exists.");
        }
        UserEntity user = newUser(request.name(), email, request.dateOfBirth());
        users.save(user);
        attachEmailPassword(user, email, request.password());
        finishNewStudent(user);
        maybeParentalConsent(user);
        return issueTokens(user, request.deviceName(), null);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = normalize(request.email());
        rateLimiter.check("login:" + email);
        UserIdentityEntity identity = identities
                .findByProviderAndProviderSubject(AuthProvider.email, email)
                .orElseThrow(() -> ApiException.unauthorized("Email or password is incorrect."));
        if (identity.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), identity.getPasswordHash())) {
            throw ApiException.unauthorized("Email or password is incorrect.");
        }
        UserEntity user = requireActive(identity.getUserId());
        return issueTokens(user, request.deviceName(), null);
    }

    @Transactional
    public TokenResponse guest(String deviceName) {
        UserEntity user = newUser("Guest", null, null);
        users.save(user);
        UserIdentityEntity identity = new UserIdentityEntity();
        identity.setUserId(user.getId());
        identity.setProvider(AuthProvider.guest);
        identity.setProviderSubject("guest:" + user.getId());
        identity.setCreatedAt(clock.instant());
        identities.save(identity);
        finishNewStudent(user);
        return issueTokens(user, deviceName, null);
    }

    @Transactional
    public TokenResponse google(SocialTokenRequest request) {
        SocialProfile profile = socialTokenVerifier.verifyGoogle(request.idToken());
        return socialLogin(AuthProvider.google, profile, request.fullName(), request.deviceName());
    }

    @Transactional
    public TokenResponse apple(SocialTokenRequest request) {
        SocialProfile profile = socialTokenVerifier.verifyApple(request.idToken());
        return socialLogin(AuthProvider.apple, profile, request.fullName(), request.deviceName());
    }

    @Transactional
    public TokenResponse requestOtp(OtpRequest request) {
        String email = normalize(request.email());
        rateLimiter.check("otp:" + email);
        String code = createOtp(email, OTP_LOGIN);
        return new TokenResponse(null, null, "Bearer", 0, null, debugCode(code));
    }

    @Transactional
    public TokenResponse verifyOtp(OtpVerifyRequest request) {
        String email = normalize(request.email());
        consumeOtp(email, OTP_LOGIN, request.code());
        UserEntity user = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            UserEntity created = newUser(email, email, null);
            users.save(created);
            UserIdentityEntity identity = new UserIdentityEntity();
            identity.setUserId(created.getId());
            identity.setProvider(AuthProvider.otp);
            identity.setProviderSubject(email);
            identity.setVerifiedAt(clock.instant());
            identity.setCreatedAt(clock.instant());
            identities.save(identity);
            created.setEmail(email);
            finishNewStudent(created);
            return created;
        });
        if (identities.findByProviderAndProviderSubject(AuthProvider.otp, email).isEmpty()) {
            UserIdentityEntity identity = new UserIdentityEntity();
            identity.setUserId(user.getId());
            identity.setProvider(AuthProvider.otp);
            identity.setProviderSubject(email);
            identity.setVerifiedAt(clock.instant());
            identity.setCreatedAt(clock.instant());
            identities.save(identity);
        }
        return issueTokens(requireActive(user.getId()), request.deviceName(), null);
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String hash = TokenHasher.sha256(request.refreshToken());
        RefreshTokenEntity stored = refreshTokens.findByTokenHash(hash)
                .orElseThrow(() -> ApiException.unauthorized("Please sign in again to continue."));
        Instant now = clock.instant();
        if (stored.getRevokedAt() != null || stored.getExpiresAt().isBefore(now)) {
            stored.setRevokedAt(now);
            refreshTokens.save(stored);
            throw ApiException.unauthorized("Please sign in again to continue.");
        }
        stored.setRevokedAt(now);
        refreshTokens.save(stored);
        UserEntity user = requireActive(stored.getUserId());
        return issueTokens(user, stored.getDeviceName(), null);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        if (request == null || request.refreshToken() == null || request.refreshToken().isBlank()) {
            return;
        }
        refreshTokens.findByTokenHash(TokenHasher.sha256(request.refreshToken()))
                .ifPresent(token -> {
                    token.setRevokedAt(clock.instant());
                    refreshTokens.save(token);
                });
    }

    @Transactional
    public TokenResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalize(request.email());
        rateLimiter.check("forgot:" + email);
        String code = null;
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            code = createOtp(email, OTP_RESET);
        }
        return new TokenResponse(null, null, "Bearer", 0, null, debugCode(code));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = normalize(request.email());
        consumeOtp(email, OTP_RESET, request.code());
        UserEntity user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.badRequest("INVALID_CODE", "This reset code is invalid or expired."));
        UserIdentityEntity identity = identities
                .findByProviderAndProviderSubject(AuthProvider.email, email)
                .orElseGet(() -> {
                    UserIdentityEntity created = new UserIdentityEntity();
                    created.setUserId(user.getId());
                    created.setProvider(AuthProvider.email);
                    created.setProviderSubject(email);
                    created.setCreatedAt(clock.instant());
                    return created;
                });
        identity.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        identity.setVerifiedAt(clock.instant());
        identities.save(identity);
    }

    @Transactional
    public TokenResponse upgradeGuest(UUID userId, GuestUpgradeRequest request) {
        UserEntity user = requireActive(userId);
        if (!identities.existsByUserIdAndProvider(userId, AuthProvider.guest)) {
            throw ApiException.badRequest("NOT_GUEST", "This account is already registered.");
        }
        if (request.email() != null && request.password() != null) {
            String email = normalize(request.email());
            if (users.findByEmailIgnoreCase(email).filter(existing -> !existing.getId().equals(userId)).isPresent()) {
                throw ApiException.conflict("EMAIL_IN_USE", "An account with this email already exists.");
            }
            user.setEmail(email);
            if (user.getName() == null || "Guest".equals(user.getName())) {
                user.setName(email);
            }
            users.save(user);
            attachEmailPassword(user, email, request.password());
            return issueTokens(user, request.deviceName(), null);
        }
        if (request.googleIdToken() != null) {
            SocialProfile profile = socialTokenVerifier.verifyGoogle(request.googleIdToken());
            return attachSocialToUser(user, AuthProvider.google, profile, request.deviceName());
        }
        if (request.appleIdToken() != null) {
            SocialProfile profile = socialTokenVerifier.verifyApple(request.appleIdToken());
            return attachSocialToUser(user, AuthProvider.apple, profile, request.deviceName());
        }
        throw ApiException.badRequest("INVALID_UPGRADE", "Provide email and password, or a Google or Apple token.");
    }

    public UserProfile profileOf(UUID userId) {
        return toProfile(requireActive(userId));
    }

    private TokenResponse socialLogin(
            AuthProvider provider,
            SocialProfile profile,
            String fullName,
            String deviceName
    ) {
        UserIdentityEntity existing = identities
                .findByProviderAndProviderSubject(provider, profile.subject())
                .orElse(null);
        if (existing != null) {
            return issueTokens(requireActive(existing.getUserId()), deviceName, null);
        }
        UserEntity user;
        if (profile.email() != null) {
            user = users.findByEmailIgnoreCase(normalize(profile.email())).orElse(null);
        } else {
            user = null;
        }
        if (user == null) {
            String name = firstNonBlank(fullName, profile.name(), profile.email(), "Student");
            user = newUser(name, profile.email() == null ? null : normalize(profile.email()), null);
            users.save(user);
            finishNewStudent(user);
        }
        UserIdentityEntity identity = new UserIdentityEntity();
        identity.setUserId(user.getId());
        identity.setProvider(provider);
        identity.setProviderSubject(profile.subject());
        identity.setVerifiedAt(clock.instant());
        identity.setCreatedAt(clock.instant());
        identities.save(identity);
        return issueTokens(user, deviceName, null);
    }

    private TokenResponse attachSocialToUser(
            UserEntity user,
            AuthProvider provider,
            SocialProfile profile,
            String deviceName
    ) {
        identities.findByProviderAndProviderSubject(provider, profile.subject()).ifPresent(existing -> {
            if (!existing.getUserId().equals(user.getId())) {
                throw ApiException.conflict("IDENTITY_IN_USE", "This social account is already linked to another user.");
            }
        });
        if (identities.findByProviderAndProviderSubject(provider, profile.subject()).isEmpty()) {
            UserIdentityEntity identity = new UserIdentityEntity();
            identity.setUserId(user.getId());
            identity.setProvider(provider);
            identity.setProviderSubject(profile.subject());
            identity.setVerifiedAt(clock.instant());
            identity.setCreatedAt(clock.instant());
            identities.save(identity);
        }
        if (profile.email() != null && user.getEmail() == null) {
            user.setEmail(normalize(profile.email()));
            users.save(user);
        }
        return issueTokens(user, deviceName, null);
    }

    private void attachEmailPassword(UserEntity user, String email, String password) {
        UserIdentityEntity identity = new UserIdentityEntity();
        identity.setUserId(user.getId());
        identity.setProvider(AuthProvider.email);
        identity.setProviderSubject(email);
        identity.setPasswordHash(passwordEncoder.encode(password));
        identity.setVerifiedAt(clock.instant());
        identity.setCreatedAt(clock.instant());
        identities.save(identity);
    }

    private void finishNewStudent(UserEntity user) {
        RoleEntity student = roles.findByCode(STUDENT_ROLE)
                .orElseThrow(() -> new IllegalStateException("STUDENT role missing from database"));
        UserRoleEntity role = new UserRoleEntity();
        role.setUserId(user.getId());
        role.setRoleId(student.getId());
        userRoles.save(role);
        PrivacySettingsEntity privacy = new PrivacySettingsEntity();
        privacy.setUserId(user.getId());
        privacy.setDisplayName(user.getName());
        privacy.setUpdatedAt(clock.instant());
        privacySettings.save(privacy);
        NotificationPreferenceEntity prefs = new NotificationPreferenceEntity();
        prefs.setUserId(user.getId());
        prefs.setUpdatedAt(clock.instant());
        notifications.save(prefs);
    }

    private void maybeParentalConsent(UserEntity user) {
        if (user.getDateOfBirth() == null) {
            return;
        }
        int age = Period.between(user.getDateOfBirth(), LocalDate.ofInstant(clock.instant(), clock.getZone())).getYears();
        if (age < 18) {
            ParentalConsentEntity consent = new ParentalConsentEntity();
            consent.setUserId(user.getId());
            consent.setStatus("PENDING");
            consent.setCreatedAt(clock.instant());
            parentalConsents.save(consent);
        }
    }

    private UserEntity newUser(String name, String email, LocalDate dob) {
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setDateOfBirth(dob);
        user.setPreferredLanguage("en");
        user.setAccountStatus(AccountStatus.ACTIVE);
        return user;
    }

    private UserEntity requireActive(UUID userId) {
        UserEntity user = users.findById(userId)
                .orElseThrow(() -> ApiException.unauthorized("Please sign in again to continue."));
        if (user.getAccountStatus() != AccountStatus.ACTIVE || user.getDeletedAt() != null) {
            throw ApiException.forbidden("This account is not active.");
        }
        return user;
    }

    private TokenResponse issueTokens(UserEntity user, String deviceName, String debugCode) {
        user.setLastLoginAt(clock.instant());
        users.save(user);
        List<String> roleCodes = userRoles.findByUserId(user.getId()).stream()
                .map(link -> roles.findById(link.getRoleId()).map(RoleEntity::getCode).orElse("STUDENT"))
                .toList();
        String access = jwtService.issueAccessToken(user.getId(), roleCodes);
        String refresh = TokenHasher.randomToken();
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUserId(user.getId());
        entity.setTokenHash(TokenHasher.sha256(refresh));
        entity.setDeviceName(deviceName);
        entity.setExpiresAt(clock.instant().plus(properties.jwt().refreshTtl()));
        entity.setCreatedAt(clock.instant());
        refreshTokens.save(entity);
        return new TokenResponse(
                access,
                refresh,
                "Bearer",
                jwtService.accessExpiresInSeconds(),
                toProfile(user, roleCodes),
                debugCode
        );
    }

    private UserProfile toProfile(UserEntity user) {
        List<String> roleCodes = userRoles.findByUserId(user.getId()).stream()
                .map(link -> roles.findById(link.getRoleId()).map(RoleEntity::getCode).orElse("STUDENT"))
                .toList();
        return toProfile(user, roleCodes);
    }

    private UserProfile toProfile(UserEntity user, List<String> roleCodes) {
        boolean guest = identities.existsByUserIdAndProvider(user.getId(), AuthProvider.guest)
                && !identities.existsByUserIdAndProvider(user.getId(), AuthProvider.email)
                && !identities.existsByUserIdAndProvider(user.getId(), AuthProvider.google)
                && !identities.existsByUserIdAndProvider(user.getId(), AuthProvider.apple);
        List<ExamSummary> examSummaries = enrollments.findByUserId(user.getId()).stream()
                .map(enrollment -> {
                    ExamEntity exam = exams.findById(enrollment.getExamId()).orElse(null);
                    if (exam == null) {
                        return null;
                    }
                    return new ExamSummary(exam.getId(), exam.getCode(), exam.getName(), enrollment.isPrimary());
                })
                .filter(item -> item != null)
                .toList();
        return new UserProfile(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getPreferredLanguage(),
                user.getAccountStatus().name(),
                guest,
                roleCodes,
                examSummaries
        );
    }

    private String createOtp(String destination, String purpose) {
        String code = TokenHasher.sixDigitCode();
        OtpChallengeEntity challenge = new OtpChallengeEntity();
        challenge.setDestination(destination);
        challenge.setPurpose(purpose);
        challenge.setCodeHash(TokenHasher.sha256(code));
        challenge.setExpiresAt(clock.instant().plusSeconds(600));
        challenge.setAttempts(0);
        challenge.setMaxAttempts(5);
        challenge.setCreatedAt(clock.instant());
        otpChallenges.save(challenge);
        return code;
    }

    private void consumeOtp(String destination, String purpose, String code) {
        OtpChallengeEntity challenge = otpChallenges
                .findFirstByDestinationAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(destination, purpose)
                .orElseThrow(() -> ApiException.badRequest("INVALID_CODE", "This code is invalid or expired."));
        Instant now = clock.instant();
        if (challenge.getExpiresAt().isBefore(now) || challenge.getAttempts() >= challenge.getMaxAttempts()) {
            throw ApiException.badRequest("INVALID_CODE", "This code is invalid or expired.");
        }
        challenge.setAttempts(challenge.getAttempts() + 1);
        if (!challenge.getCodeHash().equals(TokenHasher.sha256(code))) {
            otpChallenges.save(challenge);
            throw ApiException.badRequest("INVALID_CODE", "This code is invalid or expired.");
        }
        challenge.setConsumedAt(now);
        otpChallenges.save(challenge);
    }

    private String debugCode(String code) {
        return properties.auth().exposeOtp() ? code : null;
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "Student";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "Student";
    }
}
