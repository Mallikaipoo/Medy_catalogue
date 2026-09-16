# Database table catalog

PostgreSQL 16. Flyway is the only schema path (`backend/src/main/resources/db/migration`). Time zone stored as `TIMESTAMPTZ` (UTC). Student-facing “today” for streaks uses `Asia/Kolkata` via an injected `Clock`.

Phase 2 ships identity + exam enrollment tables (`V1__identity.sql`). Remaining tables are created in later phase migrations. Do not drop student tables casually.

## Identity and access (Phase 2)

| Table | Purpose |
| --- | --- |
| `users` | Student/admin account profile |
| `user_identities` | `email`, `google`, `apple`, `otp`, `guest` — password hash only for email |
| `refresh_tokens` | Hashed refresh tokens, device, expiry, revoke |
| `otp_challenges` | Hashed OTP / password-reset codes |
| `roles` | STUDENT, CONTENT_REVIEWER, CONTENT_MANAGER, ADMIN, SUPER_ADMIN |
| `permissions` | Fine-grained codes |
| `role_permissions` | Role to permission |
| `user_roles` | User to role |
| `user_privacy_settings` | Display name, leaderboard visibility, analytics consent |
| `parental_consents` | Under-18 (India DPDP) |
| `account_deletion_requests` | Deletion workflow |
| `user_exam_enrollments` | Multiple exams per account; one primary |
| `exams` | Minimal catalog so the exam picker works (JEE, NEET, NDA seed) |
| `audit_logs` | Actor, action, entity, IP, timestamp |

### `users` columns

`id`, `name`, `email`, `phone`, `profile_photo_url`, `date_of_birth`, `country`, `state`, `city`, `preferred_language`, `account_status`, `created_at`, `updated_at`, `last_login_at`, `deleted_at`

Passwords are never stored on `users`. They live hashed on `user_identities` for provider `email`.

## Catalog (Phase 3)

`exam_patterns`, `exam_pattern_sections`, `marking_schemes`, `subjects`, `exam_subjects`, `chapters`, `topics`, `question_types`, `languages`, `questions`, `question_options`, `question_explanations`, `explanation_steps`, `question_tags`, `question_tag_map`, `content_reviews`, `content_import_jobs`, `content_import_rows`

Question `status`: `DRAFT`, `AI_GENERATED`, `UNDER_REVIEW`, `APPROVED`, `PUBLISHED`, `REJECTED`, `ARCHIVED`.

Students see only `PUBLISHED` (and `APPROVED` only if a later rule promotes it — default student filter is `PUBLISHED`).

## Practice, mocks, daily challenge (Phases 4–6)

`practice_sessions`, `practice_session_items`, `practice_answers`, `mock_tests`, `mock_test_sections`, `mock_test_questions`, `mock_attempts`, `mock_attempt_answers`, `daily_challenges`, `daily_challenge_attempts` (unique `user_id, challenge_id`)

## Gamification (Phase 6)

`xp_ledger` (append-only), `user_gamification`, `achievements`, `user_achievements`, `badges`, `user_badges`, `goals`, `user_goal_progress`, `friendships`, `leaderboard_snapshots`

## Analytics and study (Phases 7–8)

`user_question_attempts`, `user_topic_stats`, `user_chapter_stats`, `user_subject_stats`, `user_daily_activity`, `study_plans`, `study_plan_items`

## AI (Phase 9)

`ai_conversations`, `ai_messages`, `ai_generation_jobs`, optional `similar_question_links`

## Monetization (Phase 10)

`subscription_plans`, `plan_entitlements`, `plan_prices`, `user_subscriptions`, `store_transactions`, `ad_configs`, `feature_flags`

## Ops

`push_tokens`, `notification_templates`, `notification_jobs`, `user_notification_preferences`, `product_events`

## Planned indexes (apply with the owning phase)

- Published questions: `(exam_id, subject_id, chapter_id, topic_id, status)`
- Attempts: `(user_id, created_at)`
- Unique daily challenge attempts: `(user_id, challenge_id)`
- Subscriptions: `(user_id, status)`
- Leaderboard snapshots: `(period, scope)`
