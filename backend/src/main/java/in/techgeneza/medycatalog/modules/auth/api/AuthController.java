package in.techgeneza.medycatalog.modules.auth.api;

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
import in.techgeneza.medycatalog.modules.auth.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public TokenResponse google(@Valid @RequestBody SocialTokenRequest request) {
        return authService.google(request);
    }

    @PostMapping("/apple")
    public TokenResponse apple(@Valid @RequestBody SocialTokenRequest request) {
        return authService.apple(request);
    }

    @PostMapping("/otp/request")
    public ResponseEntity<TokenResponse> otpRequest(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.accepted().body(authService.requestOtp(request));
    }

    @PostMapping("/otp/verify")
    public TokenResponse otpVerify(@Valid @RequestBody OtpVerifyRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/guest")
    public ResponseEntity<TokenResponse> guest(@RequestBody(required = false) Map<String, String> body) {
        String device = body == null ? null : body.get("deviceName");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.guest(device));
    }

    @PostMapping("/guest/upgrade")
    public TokenResponse upgrade(Authentication authentication, @Valid @RequestBody GuestUpgradeRequest request) {
        return authService.upgradeGuest((UUID) authentication.getPrincipal(), request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<TokenResponse> forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.accepted().body(authService.forgotPassword(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
