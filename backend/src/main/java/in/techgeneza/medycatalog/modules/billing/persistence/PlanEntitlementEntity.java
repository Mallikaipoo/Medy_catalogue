package in.techgeneza.medycatalog.modules.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "plan_entitlements")
public class PlanEntitlementEntity {

    @Id
    private UUID id;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(name = "value_int")
    private Integer valueInt;

    @Column(name = "value_bool")
    private Boolean valueBool;
}
