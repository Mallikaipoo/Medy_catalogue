package in.techgeneza.medycatalog.modules.learning.api;

import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.DoubtRequest;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.DoubtResponse;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.FreePreview;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.LibraryResponse;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.LocaleCard;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.ResumeRequest;
import in.techgeneza.medycatalog.modules.learning.api.dto.LearningDtos.ResumeState;
import in.techgeneza.medycatalog.modules.learning.application.LearningService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning")
public class LearningController {

    private final LearningService learning;

    public LearningController(LearningService learning) {
        this.learning = learning;
    }

    @GetMapping("/locales")
    public List<LocaleCard> locales() {
        return learning.locales();
    }

    @GetMapping("/exams/{examId}/free-preview")
    public FreePreview freePreview(@PathVariable UUID examId, @RequestParam(required = false) String locale) {
        return learning.freePreview(examId, locale);
    }

    @GetMapping("/resume")
    public ResponseEntity<ResumeState> resume(Authentication authentication) {
        ResumeState state = learning.resume((UUID) authentication.getPrincipal());
        if (state == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(state);
    }

    @PutMapping("/resume")
    public ResumeState saveResume(Authentication authentication, @RequestBody ResumeRequest request) {
        return learning.saveResume((UUID) authentication.getPrincipal(), request);
    }

    @PostMapping("/doubts")
    public DoubtResponse ask(Authentication authentication, @RequestBody DoubtRequest request) {
        return learning.ask((UUID) authentication.getPrincipal(), request);
    }

    @GetMapping("/library")
    public LibraryResponse library(Authentication authentication, @RequestParam(required = false) String locale) {
        return learning.library((UUID) authentication.getPrincipal(), locale);
    }
}
