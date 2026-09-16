package in.techgeneza.medycatalog.modules.catalog.api;

import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.ChapterCard;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.ExamSyllabus;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.HomeResponse;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.StudentQuestion;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.SubjectCard;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.TopicCard;
import in.techgeneza.medycatalog.modules.catalog.api.dto.CatalogDtos.TopicNotes;
import in.techgeneza.medycatalog.modules.catalog.application.CatalogService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/home")
    public HomeResponse home(Authentication authentication) {
        return catalogService.home((UUID) authentication.getPrincipal());
    }

    @GetMapping("/exams/{examId}/subjects")
    public List<SubjectCard> subjects(@PathVariable UUID examId) {
        return catalogService.subjects(examId);
    }

    @GetMapping("/subjects/{subjectId}/chapters")
    public List<ChapterCard> chapters(@PathVariable UUID subjectId, @RequestParam UUID examId) {
        return catalogService.chapters(examId, subjectId);
    }

    @GetMapping("/chapters/{chapterId}/topics")
    public List<TopicCard> topics(@PathVariable UUID chapterId) {
        return catalogService.topics(chapterId);
    }

    @GetMapping("/topics/{topicId}")
    public TopicNotes topicNotes(Authentication authentication, @PathVariable UUID topicId) {
        return catalogService.topicNotes((UUID) authentication.getPrincipal(), topicId);
    }

    @GetMapping("/exams/{examId}/syllabus")
    public ExamSyllabus syllabus(@PathVariable UUID examId) {
        return catalogService.syllabus(examId);
    }

    @GetMapping("/questions/{questionId}")
    public StudentQuestion question(@PathVariable UUID questionId) {
        return catalogService.publishedQuestion(questionId);
    }
}
