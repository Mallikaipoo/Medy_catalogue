package in.techgeneza.medycatalog.modules.practice.api;

import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.AnswerRequest;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.PreviewResponse;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.ResultResponse;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.ReviewResponse;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.SessionResponse;
import in.techgeneza.medycatalog.modules.practice.api.dto.PracticeDtos.StartRequest;
import in.techgeneza.medycatalog.modules.practice.application.PracticeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @PostMapping("/preview")
    public PreviewResponse preview(@Valid @RequestBody StartRequest request) {
        return practiceService.preview(request);
    }

    @PostMapping("/start")
    public SessionResponse start(Authentication authentication, @Valid @RequestBody StartRequest request) {
        return practiceService.start((UUID) authentication.getPrincipal(), request);
    }

    @GetMapping("/{sessionId}")
    public SessionResponse get(Authentication authentication, @PathVariable UUID sessionId) {
        return practiceService.get((UUID) authentication.getPrincipal(), sessionId);
    }

    @PostMapping("/{sessionId}/answer")
    public SessionResponse answer(
            Authentication authentication,
            @PathVariable UUID sessionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        return practiceService.answer((UUID) authentication.getPrincipal(), sessionId, request);
    }

    @PostMapping("/{sessionId}/complete")
    public ResultResponse complete(Authentication authentication, @PathVariable UUID sessionId) {
        return practiceService.complete((UUID) authentication.getPrincipal(), sessionId);
    }

    @GetMapping("/{sessionId}/review")
    public ReviewResponse review(Authentication authentication, @PathVariable UUID sessionId) {
        return practiceService.review((UUID) authentication.getPrincipal(), sessionId);
    }
}
