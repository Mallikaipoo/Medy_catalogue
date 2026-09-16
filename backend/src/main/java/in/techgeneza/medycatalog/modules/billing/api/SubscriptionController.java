package in.techgeneza.medycatalog.modules.billing.api;

import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.MeResponse;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.PlanCard;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.VerifyRequest;
import in.techgeneza.medycatalog.modules.billing.application.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptions;

    public SubscriptionController(SubscriptionService subscriptions) {
        this.subscriptions = subscriptions;
    }

    @GetMapping("/plans")
    public List<PlanCard> plans() {
        return subscriptions.plans();
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        return subscriptions.me((UUID) authentication.getPrincipal());
    }

    @PostMapping("/verify")
    public MeResponse verify(Authentication authentication, @Valid @RequestBody VerifyRequest request) {
        return subscriptions.verify((UUID) authentication.getPrincipal(), request);
    }

    @PostMapping("/restore")
    public MeResponse restore(Authentication authentication) {
        return subscriptions.restore((UUID) authentication.getPrincipal());
    }

    @PostMapping("/rewarded")
    public MeResponse rewarded(Authentication authentication) {
        return subscriptions.grantRewarded((UUID) authentication.getPrincipal());
    }
}
