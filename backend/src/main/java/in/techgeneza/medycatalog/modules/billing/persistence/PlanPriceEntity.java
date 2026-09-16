package in.techgeneza.medycatalog.modules.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plan_prices")
public class PlanPriceEntity {

    @Id
    private UUID id;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(nullable = false, length = 32)
    private String platform;

    @Column(nullable = false, length = 16)
    private String period;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "store_product_id", nullable = false, length = 128)
    private String storeProductId;

    @Column(nullable = false)
    private boolean highlighted;

    @Column(nullable = false)
    private boolean active = true;
}
