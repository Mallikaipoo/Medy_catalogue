package in.techgeneza.medycatalog.modules.auth.api;

import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.common.exception.GlobalExceptionHandler;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.RegisterRequest;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.TokenResponse;
import in.techgeneza.medycatalog.modules.auth.api.dto.AuthDtos.UserProfile;
import in.techgeneza.medycatalog.modules.auth.application.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setValidator(validator)
                .build();
    }

    @Test
    void registerRejectsShortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\",\"email\":\"ada@example.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"));
    }

    @Test
    void registerReturnsCreatedTokens() throws Exception {
        UUID id = UUID.randomUUID();
        UserProfile profile = new UserProfile(
                id, "Ada", "ada@example.com", null, "en", "ACTIVE", false, List.of("STUDENT"), List.of());
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new TokenResponse("access", "refresh", "Bearer", 900, profile, null));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ada\",\"email\":\"ada@example.com\",\"password\":\"longenough\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.user.email").value("ada@example.com"));
    }

    @Test
    void loginMapsUnauthorizedToFriendlyProblem() throws Exception {
        when(authService.login(any())).thenThrow(ApiException.unauthorized("Email or password is incorrect."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ada@example.com\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.detail").value("Email or password is incorrect."));
    }
}
