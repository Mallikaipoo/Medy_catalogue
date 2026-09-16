# REST API (`/api/v1`)

JSON over HTTPS. Versioned prefix is mandatory. Problem Details (`application/problem+json`) for errors. Student-facing messages are friendly; stack traces stay in logs.

Student question payloads **must not** include `isCorrect` until after submit.

Admin routes live under `/api/v1/admin/**` and require roles enforced on the server.

Machine-readable contract: [openapi.yaml](openapi.yaml). Springdoc also serves `/v3/api-docs` and `/swagger-ui.html` from the running API.

## Auth (Phase 2 — implemented)

| Method | Path | Auth |
| --- | --- | --- |
| POST | `/auth/register` | public |
| POST | `/auth/login` | public |
| POST | `/auth/google` | public |
| POST | `/auth/apple` | public |
| POST | `/auth/otp/request` | public |
| POST | `/auth/otp/verify` | public |
| POST | `/auth/guest` | public |
| POST | `/auth/guest/upgrade` | student |
| POST | `/auth/refresh` | public (refresh token body) |
| POST | `/auth/logout` | student |
| POST | `/auth/password/forgot` | public |
| POST | `/auth/password/reset` | public |

## Account (Phase 2 — implemented)

| Method | Path | Auth |
| --- | --- | --- |
| GET | `/me` | student |
| PATCH | `/me` | student |
| PATCH | `/me/exams` | student |
| GET/PATCH | `/me/privacy` | student |
| GET/PATCH | `/me/notifications` | student (preferences stub until Phase 12 push) |
| DELETE | `/me` | student (deletion request) |

## Catalog (Phase 3)

`GET /exams`, `GET /exams/{id}/subjects`, `GET /subjects/{id}/chapters`, `GET /chapters/{id}/topics`, `GET /topics/{id}/progress`

Phase 2 already exposes `GET /exams` as a thin list so the exam picker can work.

## Practice (Phase 4)

`POST /practice/preview`, `POST /practice/start`, `GET /practice/{id}`, `POST /practice/{id}/answer`, `POST /practice/{id}/complete`, `GET /practice/{id}/review`

## Questions (Phase 3–4)

`GET /questions/{id}` (no key), `POST /questions/{id}/similar`

## Results / performance (Phases 4, 7)

`GET /results/{attemptId}`, `GET /performance`, `GET /performance/weak-topics`

## Daily challenge (Phase 6)

`GET /daily-challenge`, `POST /daily-challenge/start`, `POST /daily-challenge/complete`

## Mocks (Phase 5)

`GET /mock-tests`, `POST /mock-tests/{id}/start`, `GET /mock-attempts/{id}`, `POST /mock-attempts/{id}/answer`, `POST /mock-attempts/{id}/submit`

## Gamification / leaderboard (Phases 6, 8)

`GET /gamification/me`, `GET /leaderboard?scope=&period=`

## Study plan (Phase 7)

`GET /study-plan`, `PUT /study-plan`

## AI (Phase 9)

`POST /ai-tutor/conversations`, `POST /ai-tutor/conversations/{id}/messages`

## Subscription (Phase 10)

`GET /subscription/plans`, `GET /subscription/me`, `POST /subscription/verify`, `POST /subscription/restore`

## Legal

`GET /legal/privacy`, `GET /legal/terms`

## Admin (Phase 11)

CRUD exams/subjects/chapters/topics/questions; `POST /admin/questions/{id}/review`; import jobs; mock publish; users suspend; plans/prices; notifications; AI generation jobs; dashboard metrics.

## Webhooks (Phase 10)

`POST /webhooks/google-play`, `POST /webhooks/app-store` — signed store callbacks, not student JWT.

## Error example

```json
{
  "type": "about:blank",
  "title": "INVALID_CREDENTIALS",
  "status": 401,
  "detail": "Email or password is incorrect."
}
```
