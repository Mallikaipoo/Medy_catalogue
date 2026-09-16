package in.techgeneza.medycatalog.modules.legal.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/legal")
public class LegalController {

    @GetMapping("/privacy")
    public Map<String, String> privacy() {
        return Map.of(
                "title", "Privacy Policy",
                "summary", "MedyCatalog collects only the information needed to provide exam preparation. Draft policy — replace before store submission."
        );
    }

    @GetMapping("/terms")
    public Map<String, String> terms() {
        return Map.of(
                "title", "Terms of Service",
                "summary", "MedyCatalog does not guarantee exam ranks or selections. Draft terms — replace before store submission."
        );
    }
}
