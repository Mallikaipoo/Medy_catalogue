package in.techgeneza.medycatalog.modules.billing.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.techgeneza.medycatalog.common.exception.ApiException;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.WebhookRequest;
import in.techgeneza.medycatalog.modules.billing.application.SubscriptionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/webhooks")
public class StoreWebhookController {

    private final SubscriptionService subscriptions;
    private final ObjectMapper objectMapper;

    public StoreWebhookController(SubscriptionService subscriptions, ObjectMapper objectMapper) {
        this.subscriptions = subscriptions;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/google-play", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> googlePlay(
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
            @RequestBody byte[] body
    ) {
        String raw = new String(body, StandardCharsets.UTF_8);
        subscriptions.handleWebhook("GOOGLE_PLAY", signature, raw, read(raw));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/app-store", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> appStore(
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature,
            @RequestBody byte[] body
    ) {
        String raw = new String(body, StandardCharsets.UTF_8);
        subscriptions.handleWebhook("APP_STORE", signature, raw, read(raw));
        return ResponseEntity.ok().build();
    }

    private WebhookRequest read(String rawBody) {
        try {
            WebhookRequest request = objectMapper.readValue(rawBody, WebhookRequest.class);
            if (request == null || request.purchaseToken() == null || request.purchaseToken().isBlank()) {
                throw ApiException.badRequest("INVALID_WEBHOOK", "Webhook payload was incomplete.");
            }
            return request;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ApiException.badRequest("INVALID_WEBHOOK", "Webhook payload was incomplete.");
        }
    }
}
