# Phase 2 report — Authentication

- Date: 2026-09-16
- Goal: Registration, login, Google, Apple, OTP, guest, profile, refresh tokens.

## Completed

- Flyway `V1__identity.sql`: users, identities, refresh tokens, OTP, RBAC, privacy, notifications, parental consent, deletion requests, exam seed (JEE/NEET/NDA), enrollments, audit log.
- Spring Security JWT access tokens (15 min) and hashed rotating refresh tokens (30 days).
- Auth and account REST APIs under `/api/v1`.
- Server-side Google ID token and Apple JWKS verification (requires configured client IDs).
- Flutter feature modules: splash, login, register, guest, exam picker, profile.
- Unit and MockMvc tests for JWT, hashing, register validation, unauthorized mapping.

## Files created or modified

- `backend/src/main/java/in/techgeneza/medycatalog/**`
- `backend/src/main/resources/db/migration/V1__identity.sql`
- `backend/src/test/java/**`
- `mobile/lib/**`, `mobile/test/failure_test.dart`

## Database changes

- Flyway: `V1__identity.sql`
- Tables: identity/RBAC/privacy/exams seed as listed in `docs/DATABASE.md` Phase 2 section

## APIs added

All Phase 2 routes in `docs/API.md` (auth, `/me`, `/exams`, `/legal`).

## Tests added

- `JwtServiceTest`
- `TokenHasherTest`
- `AuthServiceTest` (duplicate email)
- `AuthControllerTest` (validation + created + unauthorized problem details)
- Flutter `failure_test.dart`

## Known issues

- SMTP is not wired; local profile returns `debugCode` when `medycatalog.auth.expose-otp=true`. Production must keep this false and add email in a later ops task.
- Google/Apple sign-in returns 503 until `GOOGLE_OAUTH_CLIENT_ID` / Apple audience are configured.
- Flutter `android/` and `ios/` folders require `flutter create .` because the Flutter SDK was not on PATH.
- Redis auto-config is excluded in Phase 2; rate limiting is in-memory.
- Rate limiter uses `Instant.now()`; inject Clock in a later hardening pass.
- UserEntity `@PrePersist` uses `Instant.now()` rather than the injected Clock.

## How to test

```bash
cd infra
docker compose up -d
cd ../backend
./gradlew test
./gradlew bootRun
```

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/register -H "Content-Type: application/json" -d "{\"name\":\"Ada\",\"email\":\"ada@example.com\",\"password\":\"longenough\"}"
```

Wait for approval before Phase 3 (catalog / questions).
