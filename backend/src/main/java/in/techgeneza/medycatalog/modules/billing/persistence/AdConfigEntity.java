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
@Table(name = "ad_configs")
public class AdConfigEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String placement;

    @Column(nullable = false, length = 32)
    private String platform;

    @Column(name = "unit_id", nullable = false, length = 128)
    private String unitId;

    @Column(nullable = false)
    private boolean enabled = true;
}
