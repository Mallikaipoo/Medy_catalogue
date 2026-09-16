package in.techgeneza.medycatalog.modules.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_practice_credits")
public class UserPracticeCreditEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "extra_starts", nullable = false)
    private int extraStarts;

    @Column(name = "rewarded_on")
    private LocalDate rewardedOn;

    @Column(name = "rewarded_today", nullable = false)
    private int rewardedToday;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
