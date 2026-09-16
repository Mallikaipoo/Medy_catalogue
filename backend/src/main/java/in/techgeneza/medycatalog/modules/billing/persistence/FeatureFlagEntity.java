package in.techgeneza.medycatalog.modules.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "feature_flags")
public class FeatureFlagEntity {

    @Id
    @Column(name = "flag_key", length = 64)
    private String flagKey;

    @Column(nullable = false)
    private boolean enabled;

    private String description;
}
