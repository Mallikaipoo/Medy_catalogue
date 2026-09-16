package in.techgeneza.medycatalog.modules.billing.api;

import in.techgeneza.medycatalog.common.exception.GlobalExceptionHandler;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.PlanCard;
import in.techgeneza.medycatalog.modules.billing.api.dto.BillingDtos.PriceCard;
import in.techgeneza.medycatalog.modules.billing.application.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptions;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SubscriptionController(subscriptions))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void yearlyPriceIsTheHighlightedDefault() throws Exception {
        when(subscriptions.plans()).thenReturn(List.of(premiumCard()));

        mockMvc.perform(get("/api/v1/subscription/plans").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prices[0].period").value("YEAR"))
                .andExpect(jsonPath("$[0].prices[0].amount").value(1499.0))
                .andExpect(jsonPath("$[0].prices[0].highlighted").value(true));
    }

    private static PlanCard premiumCard() {
        return new PlanCard(
                "PREMIUM",
                "Premium",
                "Yearly billed once",
                true,
                List.of(
                        new PriceCard(
                                "GOOGLE_PLAY",
                                "YEAR",
                                "INR",
                                new BigDecimal("1499.00"),
                                "in.claris.medycatalog.premium.yearly",
                                true,
                                "Best value — billed once a year, fewer interruptions"
                        ),
                        new PriceCard(
                                "GOOGLE_PLAY",
                                "MONTH",
                                "INR",
                                new BigDecimal("199.00"),
                                "in.claris.medycatalog.premium.monthly",
                                false,
                                "Flexible month to month"
                        )
                )
        );
    }
}
