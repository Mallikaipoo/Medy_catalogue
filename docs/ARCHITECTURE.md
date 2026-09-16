# MedyCatalog architecture

Four independently deployable surfaces share one API and one student database.

```text
Flutter Android/iOS          Next.js Admin
         \                      /
          \                    /
           HTTPS  (TLS 1.2+)
                    |
            Load balancer / reverse proxy
                    |
            Spring Boot API  (stateless)
                    |
     +--------------+--------------+--------------+
     |              |              |              |
 PostgreSQL 16    Redis 7     Object storage   Workers
                                  (S3/MinIO)   (async jobs)
```

Store billing (Google Play, App Store) and push (FCM, APNs) call the API, not the app database.

## Surfaces

| Surface | Stack | Responsibility |
| --- | --- | --- |
| Student app | Flutter + Riverpod + Clean Architecture | UI, local cache, IAP handoff, ads SDK |
| API | Spring Boot 3, Java 17/21 | Auth, content, scoring, billing verify, RBAC |
| Admin | Next.js App Router | CMS, review, users, plans, notifications |
| Workers | Spring `@Async` now; separate module later | Import, AI generation, leaderboards, push |

## Non-negotiable rules

1. FileMaker is unrelated. This product is MedyCatalog only.
2. Flutter never talks to PostgreSQL.
3. Secrets stay in environment / secret manager — never in the mobile binary.
4. Scores, XP, streaks, leaderboards, daily rewards, and subscription status are computed or verified on the server.
5. Public question APIs do not include `is_correct` until after submit (or authorized review).
6. AI-generated questions stay `AI_GENERATED` / `DRAFT` until a reviewer sets `APPROVED`, then `PUBLISHED`.
7. Exam patterns and prices are rows in the database, not `if (exam == NEET)` in Flutter.

## Content hierarchy

Exam → Subject → Chapter → Topic → Question.

JEE, NEET, and NDA differ by `exam_patterns` (duration, sections, marking), not by a new mobile build. CUET, SSC, Banking, UPSC, and others are additional `exams` rows.

## Request flow (student)

1. App authenticates and stores access + refresh tokens in secure storage.
2. App calls `/api/v1/...` over HTTPS with `Authorization: Bearer`.
3. On 401, app rotates the refresh token once; on failure, it returns to login.
4. Practice/mock answers are accepted only for an in-progress server session whose `must_submit_by` has not passed.
5. Result payloads are produced by the server from stored keys.

## Package layout

See `backend/` and `mobile/lib/` in this repository. Features stay independent: auth, catalog, practice, mock exams, gamification, leaderboard, performance, study plan, AI tutor, billing, ads, notifications, admin, audit.

## Capacity

Designed for 10,000 users now and 100,000 without a rewrite (pagination, indexes, cache, stateless API). One million users needs read replicas and snapshot leaderboards — planned, not built in Phase 2.
