package in.techgeneza.medycatalog.modules.catalog.api;

import in.techgeneza.medycatalog.modules.catalog.persistence.ExamRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams")
public class ExamController {

    private final ExamRepository exams;

    public ExamController(ExamRepository exams) {
        this.exams = exams;
    }

    @GetMapping
    public List<ExamResponse> list() {
        return exams.findByStatusOrderByNameAsc("ACTIVE").stream()
                .map(exam -> new ExamResponse(exam.getId(), exam.getCode(), exam.getName()))
                .toList();
    }

    public record ExamResponse(UUID id, String code, String name) {
    }
}
