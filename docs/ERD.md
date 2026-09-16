# Entity-relationship diagram

Core student and content model. Full columns: [DATABASE.md](DATABASE.md).

```mermaid
erDiagram
  users ||--o{ user_identities : has
  users ||--o{ user_exam_enrollments : enrolls
  users ||--o{ refresh_tokens : has
  users ||--o{ user_roles : assigned
  roles ||--o{ user_roles : grants
  roles ||--o{ role_permissions : has
  permissions ||--o{ role_permissions : mapped
  users ||--o{ user_privacy_settings : has
  exams ||--o{ user_exam_enrollments : chosen
  exams ||--o{ exam_subjects : contains
  subjects ||--o{ exam_subjects : mapped
  subjects ||--o{ chapters : has
  chapters ||--o{ topics : has
  topics ||--o{ questions : has
  question_types ||--o{ questions : classifies
  questions ||--o{ question_options : has
  questions ||--o{ question_explanations : has
  questions ||--o{ explanation_steps : has
  questions ||--o{ content_reviews : reviewed
  users ||--o{ practice_sessions : starts
  practice_sessions ||--o{ practice_answers : records
  mock_tests ||--o{ mock_attempts : taken
  users ||--o{ user_subscriptions : holds
  users ||--o{ xp_ledger : earns
  users ||--o{ user_topic_stats : tracks
```

## Content hierarchy

```text
Exam
 └── Subject
      └── Chapter
           └── Topic
                └── Question
                     ├── Options (is_correct never in student fetch)
                     ├── Explanations (simple, detailed, why-wrong, tip)
                     └── Steps (formula → substitution → calculation → answer)
```

Question types are rows in `question_types` (`SINGLE_MCQ`, `MULTI_MCQ`, `NUMERICAL`, `TRUE_FALSE`, plus future codes). The UI renders from type metadata, not hard-coded enums in widgets.
