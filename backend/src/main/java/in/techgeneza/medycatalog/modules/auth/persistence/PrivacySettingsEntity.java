package in.techgeneza.medycatalog.modules.auth.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_privacy_settings")
public class PrivacySettingsEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "leaderboard_visibility", nullable = false)
    private String leaderboardVisibility = "GLOBAL";

    @Column(name = "analytics_consent", nullable = false)
    private boolean analyticsConsent;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
