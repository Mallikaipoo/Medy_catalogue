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
@Table(name = "user_notification_preferences")
public class NotificationPreferenceEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "daily_challenge", nullable = false)
    private boolean dailyChallenge = true;

    @Column(name = "streak_reminders", nullable = false)
    private boolean streakReminders = true;

    @Column(name = "study_reminders", nullable = false)
    private boolean studyReminders = true;

    @Column(nullable = false)
    private boolean marketing;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
