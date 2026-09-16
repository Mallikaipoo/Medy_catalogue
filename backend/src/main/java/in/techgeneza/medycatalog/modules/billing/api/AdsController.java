package in.techgeneza.medycatalog.modules.billing.api;

import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.AdsResponse;
import in.techgeneza.medycatalog.modules.billing.application.SubscriptionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ads")
public class AdsController {

    private final SubscriptionService subscriptions;

    public AdsController(SubscriptionService subscriptions) {
        this.subscriptions = subscriptions;
    }

    @GetMapping("/config")
    public AdsResponse config(Authentication authentication, @RequestParam(defaultValue = "ANDROID") String platform) {
        return subscriptions.ads((UUID) authentication.getPrincipal(), platform);
    }
}
