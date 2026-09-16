package in.techgeneza.medycatalog.modules.auth.api;

import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.NotificationPreferencesResponse;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.PrivacySettingsResponse;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UpdateExamsRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UpdateNotificationsRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UpdatePrivacyRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UpdateProfileRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UserProfile;
import in.techgeneza.medycatalog.modules.auth.application.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public UserProfile me(Authentication authentication) {
        return accountService.me(userId(authentication));
    }

    @PatchMapping("/me")
    public UserProfile update(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return accountService.updateProfile(userId(authentication), request);
    }

    @PatchMapping("/me/exams")
    public UserProfile exams(Authentication authentication, @Valid @RequestBody UpdateExamsRequest request) {
        return accountService.updateExams(userId(authentication), request);
    }

    @GetMapping("/me/privacy")
    public PrivacySettingsResponse privacy(Authentication authentication) {
        return accountService.privacy(userId(authentication));
    }

    @PatchMapping("/me/privacy")
    public PrivacySettingsResponse updatePrivacy(
            Authentication authentication,
            @Valid @RequestBody UpdatePrivacyRequest request
    ) {
        return accountService.updatePrivacy(userId(authentication), request);
    }

    @GetMapping("/me/notifications")
    public NotificationPreferencesResponse notifications(Authentication authentication) {
        return accountService.notifications(userId(authentication));
    }

    @PatchMapping("/me/notifications")
    public NotificationPreferencesResponse updateNotifications(
            Authentication authentication,
            @Valid @RequestBody UpdateNotificationsRequest request
    ) {
        return accountService.updateNotifications(userId(authentication), request);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> delete(Authentication authentication) {
        accountService.requestDeletion(userId(authentication));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private static UUID userId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }
}
