package in.techgeneza.medycatalog.modules.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "Please enter your name.") String name,
            @Email(message = "Please enter a valid email address.")
            @NotBlank(message = "Please enter your email address.") String email,
            @Size(min = 8, message = "Password must be at least 8 characters.") String password,
            LocalDate dateOfBirth,
            String deviceName
    ) {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank(message = "Please enter your password.") String password,
            String deviceName
    ) {
    }

    public record SocialTokenRequest(
            @NotBlank String idToken,
            String fullName,
            String deviceName
    ) {
    }

    public record OtpRequest(
            @Email @NotBlank String email
    ) {
    }

    public record OtpVerifyRequest(
            @Email @NotBlank String email,
            @NotBlank String code,
            String deviceName
    ) {
    }

    public record GuestUpgradeRequest(
            String email,
            String password,
            String googleIdToken,
            String appleIdToken,
            String deviceName
    ) {
    }

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {
    }

    public record ForgotPasswordRequest(
            @Email @NotBlank String email
    ) {
    }

    public record ResetPasswordRequest(
            @Email @NotBlank String email,
            @NotBlank String code,
            @Size(min = 8, message = "Password must be at least 8 characters.") String newPassword
    ) {
    }

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            UserProfile user,
            String debugCode
    ) {
        public TokenResponse(
                String accessToken,
                String refreshToken,
                long expiresIn,
                UserProfile user
        ) {
            this(accessToken, refreshToken, "Bearer", expiresIn, user, null);
        }
    }

    public record UserProfile(
            UUID id,
            String name,
            String email,
            String phone,
            String preferredLanguage,
            String accountStatus,
            boolean guest,
            List<String> roles,
            List<ExamSummary> exams
    ) {
    }

    public record ExamSummary(UUID id, String code, String name, boolean primary) {
    }

    public record UpdateProfileRequest(
            String name,
            String phone,
            LocalDate dateOfBirth,
            String country,
            String state,
            String city,
            String preferredLanguage,
            String profilePhotoUrl
    ) {
    }

    public record UpdateExamsRequest(
            List<UUID> examIds,
            UUID primaryExamId
    ) {
    }

    public record PrivacySettingsResponse(
            String displayName,
            String leaderboardVisibility,
            boolean analyticsConsent
    ) {
    }

    public record UpdatePrivacyRequest(
            String displayName,
            String leaderboardVisibility,
            Boolean analyticsConsent
    ) {
    }

    public record NotificationPreferencesResponse(
            boolean dailyChallenge,
            boolean streakReminders,
            boolean studyReminders,
            boolean marketing
    ) {
    }

    public record UpdateNotificationsRequest(
            Boolean dailyChallenge,
            Boolean streakReminders,
            Boolean studyReminders,
            Boolean marketing
    ) {
    }
}
